package com.dp.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import com.dp.R;
import com.dp.auth.AuthStatus;
import com.dp.auth.AuthorizationResponseError;
import com.dp.auth.OAuthHttpRequestParameter;
import com.dp.auth.exceptions.InvalidAuthorizationResponseException;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.model.AuthorizationResponse;
import com.dp.auth.model.OperationStatus;
import com.dp.auth.model.TokenResponse;
import com.dp.auth.pkce.CodeChallengeMethod;
import com.dp.auth.pkce.CodeChallengeProvider;
import com.dp.auth.pkce.CodeVerifierProvider;
import com.dp.auth.pkce.StateProvider;
import com.dp.data.datasources.AuthorizationServerDataSource;
import com.dp.database.AppDatabase;
import com.dp.database.DatabaseProvider;
import com.dp.database.dao.UserAuthInfoDao;
import com.dp.database.entity.UserAuthInfo;
import com.dp.net.HttpBodyDecoders;
import com.dp.net.HttpContentTypes;
import com.dp.net.HttpRequestTask;
import com.dp.net.HttpUriRequestBaseBuilder;
import com.dp.net.OAuthHttpUriRequestBaseFactory;
import com.google.gson.Gson;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public final class AuthorizationManager {
  private final String TAG = "AuthorizationManager";

  private static volatile AuthorizationManager instance;
  private AuthorizationServerDataSource mAuthorizationServerDataSource;
  private AppDatabase mDatabase;

  @Nullable
  private AuthorizationRequest mAuthorizationRequest = null;

  @Nullable
  private TokenResponse mTokenResponse = null;

  @Nullable
  private String mCodeVerifier = null;

  private AuthStatus mAuthProcessStatus;

  private Gson gson;

  private UserAuthInfoDao mUserAuthInfoDao;

  private ExecutorService executor;

  private AuthorizationManager() {
    Log.d(TAG, "CTOR");
    mAuthorizationServerDataSource = new AuthorizationServerDataSource();
    gson = new Gson();
    mDatabase = DatabaseProvider.getInstance(null);
    mUserAuthInfoDao = mDatabase.userAuthInfoDao();
    mAuthProcessStatus = AuthStatus.UNDEFINED;
    executor = Executors.newFixedThreadPool(2);
  }

  public static AuthorizationManager getInstance() {
    if (instance == null) {
      instance = new AuthorizationManager();
    }
    return instance;
  }

//  @Nullable
//  public AuthorizationRequest getLatestAuthorizationRequest() {
//    return mAuthorizationRequest;
//  }

//  @Nullable
//  public String getLatestCodeVerifier() {
//    return mCodeVerifier;
//  }

  public AuthorizationRequest createNewAuthorizationRequest(
      @NonNull String authServerAuthority,
      @NonNull String clientId,
      String redirectUri,
      Set<String> scopes
  ) {
    String responseType = "code";

    String codeVerifier = new CodeVerifierProvider().generateCodeVerifier();

    Log.d(TAG, "CODE VERIFER IN BASE64: " + codeVerifier);

    CodeChallengeProvider codeChallengeProvider = new CodeChallengeProvider();

    String codeChallenge = null;
    CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256;

    try {
      codeChallenge = codeChallengeProvider.generateCodeChallenge(
          codeChallengeMethod, codeVerifier);
    } catch (NoSuchAlgorithmException exception) {
      Log.w(TAG, exception.getMessage() + " Falling back to PLAIN method.");
    }

    if (codeChallenge == null) {
      codeChallengeMethod = CodeChallengeMethod.PLAIN;
      try {
        codeChallenge = codeChallengeProvider.generateCodeChallenge(
            codeChallengeMethod, codeVerifier);
      } catch (NoSuchAlgorithmException exception) {
        Log.wtf(TAG, exception.getMessage() + " This should not happen.");
      }
    }

    assert codeChallenge != null;

    String state = new StateProvider().generate();

    UserAuthInfo userAuthInfo = new UserAuthInfo(
        0,
        null,
        codeVerifier,
        null,
        null,
        null,
        -1,
        -1
    );

    executor.submit(() -> {
      mUserAuthInfoDao.insertUserAuthInfo(userAuthInfo);
    });

    mAuthorizationRequest = new AuthorizationRequest(
        authServerAuthority,
        clientId,
        responseType,
        codeChallenge,
        codeChallengeMethod,
        redirectUri,
        state,
        scopes
    );
    mCodeVerifier = codeVerifier;

    return mAuthorizationRequest;
  }

//  public void setTokenResponse(TokenResponse tokenResponse) {
//    mTokenResponse = tokenResponse;
//    UserAuthInfoDao dao = mDatabase.userAuthInfoDao();
//    UserAuthInfo oldUserAuthInfo = dao.findById(0);
//    dao.insertUserAuthInfo(new UserAuthInfo(
//        0,
//        oldUserAuthInfo.authCode,
//        oldUserAuthInfo.codeVerifier,
//        tokenResponse.getAccessToken(),
//        tokenResponse.getRefreshToken(),
//        tokenResponse.getTokenType(),
//        tokenResponse.getExpireTime(),
//        System.currentTimeMillis() / 1000
//    ));
//  }

//  @Nullable
//  public TokenResponse getLatestTokenResponse() {
//    return mTokenResponse;
//  }

  @Nullable
  public String getLatestTokenSync() {
    Future<String> tokenFuture =executor.submit(() -> mUserAuthInfoDao.findTokenByUid(0));
    try {
      return tokenFuture.get();
    } catch (InterruptedException | ExecutionException exception) {
      exception.printStackTrace();
    }
    return null;
  }

  public void revokeToken() {
    Log.d(TAG, "revokeToken");
    Future<String> tokenFuture = executor.submit(() -> mUserAuthInfoDao.findTokenByUid(0));
    String token = null;
    try {
      token = tokenFuture.get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
    if (token == null) return;

    executor.submit(new HttpRequestTask(
        new AuthorizationManager.RevocationRequestFactory(token),
        httpResponse -> {
          Log.d(TAG, "AUTH SERVER REVOKE TOKEN");
          Log.d(TAG, httpResponse.toString());
          Log.d(TAG, Arrays.toString(httpResponse.getHeaders()));
          Log.d(TAG, Long.toString(httpResponse.getEntity().getContentLength()));

          UserAuthInfo oldRecord = mUserAuthInfoDao.findById(0);
          if (oldRecord != null) {
            mUserAuthInfoDao.deleteUserAuthInfo(oldRecord);
          }
        }
    ));
  }

  public OperationStatus tryRefreshTokenRequest(String refreshToken) {
    Log.d(TAG, "refreshToken");

    Future guard = executor.submit(new HttpRequestTask(
        new RefreshTokenRequestFactory(refreshToken),
        httpResponse -> {
          Log.d(TAG, "Resource SERVER RESPONSE FOR TOKEN REQUEST");
          TokenResponse tokenResponse = HttpBodyDecoders
              .decodeHttpResponseBody(httpResponse.getEntity(), TokenResponse.class);
          UserAuthInfo oldRecord = mUserAuthInfoDao.findById(0);
          mUserAuthInfoDao.insertUserAuthInfo(new UserAuthInfo(
              0,
              oldRecord.authCode,
              oldRecord.codeVerifier,
              tokenResponse.getAccessToken(),
              oldRecord.refreshToken,
              oldRecord.tokenType,
              tokenResponse.getExpireTime(),
              System.currentTimeMillis() / 1000
          ));
        }
    ));
    try {
      guard.get();
    } catch (InterruptedException | ExecutionException exception) {
      exception.printStackTrace();
    }
    return new OperationStatus(true, null);
  }

  public Future<AuthStatus> authorize(Context appContext) throws ExecutionException, InterruptedException {
    Log.d(TAG, "authorize");
    Future<AuthStatus> future =  executor.submit(() -> {
      UserAuthInfo userAuthInfo = mUserAuthInfoDao.findById(0);

      if (userAuthInfo != null) {
        Log.d(TAG, "user auth info found in database");
        Log.d(TAG, userAuthInfo.toString());
        if (hasValidAccessToken(userAuthInfo)) {
          // what is auth server revoked the token?
          return AuthStatus.COMPLETED_OK;
        } else if (hasRefreshToken(userAuthInfo)) {
          Log.d(TAG, "refresh token detected");
          OperationStatus opStatus = tryRefreshTokenRequest(userAuthInfo.refreshToken);
          if (opStatus.isSuccess()) {
            return AuthStatus.COMPLETED_OK;
          } else {
            Log.d(TAG, "refresh token request failed");
            acquireAuthorizationCode(appContext);
          }
        } else {
          acquireAuthorizationCode(appContext);
        }
      } else {
        Log.d(TAG, "no user auth info found in database");
        acquireAuthorizationCode(appContext);
      }
      return AuthStatus.TOKEN_REQUEST_REQUIRED;
    });
    return future;
  }

  private OperationStatus acquireAuthorizationCode(Context appContext) {
    Log.d(TAG, "acquireAuthorizationCode");
    Set<String> scopes = new HashSet<>();
    Collections.addAll(scopes, appContext.getResources().getStringArray(R.array.auth_required_scopes));
    AuthorizationRequest authorizationRequest = createNewAuthorizationRequest(
        mAuthorizationServerDataSource.getAuthority(),
        appContext.getString(R.string.client_id),
        "auth://callback",
        scopes
    );

    Uri authorizationRequestUri = authorizationRequest.toUri();

    Log.d(TAG, "Authorization request:" + authorizationRequestUri.toString());

    delegateAuthorizationRequestToCustomTabs(appContext, authorizationRequestUri);
    return new OperationStatus(true, null);
  }

  public OperationStatus acquireAccessToken(Context appContext, AuthorizationResponse response) {
    Log.d(TAG, "Sending Token request");
    Future<UserAuthInfo> userAuthInfoFuture = executor.submit(
        () -> mUserAuthInfoDao.findById(0));

    UserAuthInfo userAuthInfoNonFinal = null;
    try {
      userAuthInfoNonFinal = userAuthInfoFuture.get();
    } catch (InterruptedException | ExecutionException exception) {
      exception.printStackTrace();
      return new OperationStatus(false, "Failed to fetch codeVerifier");
    }
    String codeVerifierNonFinal = userAuthInfoNonFinal.codeVerifier;

    UserAuthInfo userAuthInfoFinal = userAuthInfoNonFinal;
    Future future = executor.submit(new HttpRequestTask(
        new AuthorizationManager.AccessTokenRequestFactory(
            response,
            appContext.getString(R.string.client_id),
            codeVerifierNonFinal
        ),
        httpResponse -> {
          Log.d(TAG, "AUTH SERVER RESPONSE FOR TOKEN REQUEST");
          Log.d(TAG, httpResponse.toString());
          Log.d(TAG, Arrays.toString(httpResponse.getHeaders()));
          TokenResponse tokenResponse = HttpBodyDecoders
              .decodeHttpResponseBody(httpResponse.getEntity(), TokenResponse.class);
          if (tokenResponse == null) return;
          UserAuthInfo userAuthInfo = new UserAuthInfo(
              0,
              userAuthInfoFinal.authCode,
              codeVerifierNonFinal,
              tokenResponse.getAccessToken(),
              tokenResponse.getRefreshToken(),
              tokenResponse.getTokenType(),
              tokenResponse.getExpireTime(),
              System.currentTimeMillis() / 1000
          );
          mUserAuthInfoDao.insertUserAuthInfo(userAuthInfo);
        }
    ));

    try {
      future.get();
    } catch (InterruptedException | ExecutionException exception) {
      exception.printStackTrace();
    }

    return new OperationStatus(true, null);
  }

  private void delegateAuthorizationRequestToCustomTabs(Context appContext, Uri request) {
    CustomTabsIntent.Builder customTabsIntentBuilder = new CustomTabsIntent.Builder();
    CustomTabsIntent intent = customTabsIntentBuilder.build();
    Bundle headers = new Bundle();
    headers.putString("content-type", "application/x-www-form-urlencoded");
    intent.intent.putExtra(Browser.EXTRA_HEADERS, headers);
    Log.d(TAG, "Launching custom tabs");
    intent.launchUrl(appContext, request);
  }

  public void validateAuthorizationResponse(AuthorizationResponse authorizationResponse)
      throws InvalidAuthorizationResponseException {

    boolean invalid = false;
    StringBuilder errorMessageBuilder = new StringBuilder();

    if (authorizationResponse == null) {
      throw new InvalidAuthorizationResponseException("Null response");
    }

    if (authorizationResponse.mCode == null) {
      invalid = true;
      errorMessageBuilder.append(AuthorizationResponseError.NO_CODE_GRANT).append('\n');
    }
    if (authorizationResponse.mState == null) {
      invalid = true;
      errorMessageBuilder.append(AuthorizationResponseError.NO_STATE).append('\n');
    }
    assert authorizationResponse.mState != null;

//    if (!authorizationResponse.mState.equals(request.mState)) {
//      invalid = true;
//      errorMessageBuilder.append(AuthorizationResponseError.BAD_STATE).append('\n');
//    }
    if (invalid) {
      throw new InvalidAuthorizationResponseException(errorMessageBuilder.toString());
    }
  }

  public boolean hasValidAccessToken(@NonNull UserAuthInfo userAuthInfo) {
    if (userAuthInfo.token == null) return false;
    long acquireTime = userAuthInfo.acquireTime;
    long currentTime = System.currentTimeMillis() / 1000;
    long expireTime = userAuthInfo.tokenExpiresIn;
    return acquireTime + expireTime > currentTime;
  }

  public boolean hasRefreshToken(@NonNull UserAuthInfo userAuthInfo) {
    return userAuthInfo.refreshToken != null;
  }

  public Future<?> deleteUserAuthInfo() {
    return executor.submit(() -> {
      mUserAuthInfoDao.deleteUserAuthInfo(mUserAuthInfoDao.findById(0));
    });
  }

  private final class RevocationRequestFactory implements OAuthHttpUriRequestBaseFactory {
    private final String mToken;

    public RevocationRequestFactory(@NonNull String token) {
      mToken = token;
    }

    @Override
    public HttpUriRequestBase create() {
      HttpUriRequestBaseBuilder requestBuilder = new HttpUriRequestBaseBuilder(
          Method.POST,
          mAuthorizationServerDataSource.getAddressOfRevocationEndpoint()
      );
      return requestBuilder
          .setHeader(HttpHeaders.CONTENT_TYPE, HttpContentTypes.APPLICATION_X_WWW_FORM_URLENCODED)
          .addParam(OAuthHttpRequestParameter.TOKEN, mToken)
          .build();
    }
  }


  private final class AccessTokenRequestFactory implements OAuthHttpUriRequestBaseFactory {
    private final AuthorizationResponse mAuthorizationResponse;
    private final String mClientId;
    private final String mCodeVerifier;

    public AccessTokenRequestFactory(
        @NonNull AuthorizationResponse authorizationResponse,
        @NonNull String clientId,
        @NonNull String codeVerifier
    ) {
      mAuthorizationResponse = authorizationResponse;
      mClientId = clientId;
      mCodeVerifier = codeVerifier;
    }

    @Override
    public HttpUriRequestBase create() {
      HttpUriRequestBaseBuilder requestBuilder = new HttpUriRequestBaseBuilder(
        Method.POST,
        mAuthorizationServerDataSource.getAddressOfTokenEndpoint()
      );
      return requestBuilder
          .addParam(OAuthHttpRequestParameter.GRANT_TYPE, "authorization_code")
          .addParam(OAuthHttpRequestParameter.CODE, mAuthorizationResponse.mCode)
          .addParam(OAuthHttpRequestParameter.REDIRECT_URI, "auth://callback")
          .addParam(OAuthHttpRequestParameter.CLIENT_ID, mClientId)
          .addParam(OAuthHttpRequestParameter.CODE_VERIFIER, mCodeVerifier)
          .build();
    }
  }

  private final class RefreshTokenRequestFactory implements OAuthHttpUriRequestBaseFactory {
    private final String mRefreshToken;
    public RefreshTokenRequestFactory(
        @NonNull String refreshToken
    ) {
      mRefreshToken = refreshToken;
    }

    @Override
    public HttpUriRequestBase create() {
      HttpUriRequestBaseBuilder requestBuilder = new HttpUriRequestBaseBuilder(
          Method.POST,
          mAuthorizationServerDataSource.getAddressOfTokenEndpoint()
      );
      return requestBuilder
          .setHeader(HttpHeaders.CONTENT_TYPE, HttpContentTypes.APPLICATION_X_WWW_FORM_URLENCODED)
          .addParam(OAuthHttpRequestParameter.GRANT_TYPE, "refresh_token")
          .addParam(OAuthHttpRequestParameter.REFRESH_TOKEN, mRefreshToken)
          .build();
    }
  }

}
