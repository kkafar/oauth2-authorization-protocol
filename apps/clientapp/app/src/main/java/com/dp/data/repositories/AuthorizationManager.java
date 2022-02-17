package com.dp.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dp.R;
import com.dp.auth.model.OperationStatus;
import com.dp.auth.model.TokenResponse;
import com.dp.auth.pkce.CodeChallengeMethod;
import com.dp.auth.pkce.CodeChallengeProvider;
import com.dp.auth.pkce.CodeVerifierProvider;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.pkce.StateProvider;
import com.dp.data.datasources.AuthorizationServerDataSource;
import com.dp.database.AppDatabase;
import com.dp.database.DatabaseProvider;
import com.dp.database.dao.UserAuthInfoDao;
import com.dp.database.entity.UserAuthInfo;
import com.dp.ui.UserAuthState;
import com.dp.ui.userdata.UserDataState;
import com.google.gson.Gson;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class AuthorizationManager {
  private final String TAG = "AuthorizationFlowRepository";

  private static volatile AuthorizationManager instance;
  private AuthorizationServerDataSource mAuthorizationServerDataSource;
  private AppDatabase mDatabase;

  @Nullable
  private AuthorizationRequest mAuthorizationRequest = null;

  @Nullable
  private TokenResponse mTokenResponse = null;

  @Nullable
  private String mCodeVerifier = null;

  private Gson gson;

  private AuthorizationManager() {
    Log.d(TAG, "CTOR");
    mAuthorizationServerDataSource = new AuthorizationServerDataSource();
    gson = new Gson();
    mDatabase = DatabaseProvider.getInstance(null);
  }

  public static AuthorizationManager getInstance() {
    if (instance == null) {
      instance = new AuthorizationManager();
    }
    return instance;
  }

  @Nullable
  public AuthorizationRequest getLatestAuthorizationRequest() {
    return mAuthorizationRequest;
  }

  @Nullable
  public String getLatestCodeVerifier() {
    return mCodeVerifier;
  }

  @Nullable
  public AuthorizationRequest consumeAuthorizationRequest() {
    AuthorizationRequest request = mAuthorizationRequest;
    mAuthorizationRequest = null;
    mCodeVerifier = null;
    return request;
  }

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

    Thread executor = new Thread(() -> {
      UserAuthInfoDao dao = mDatabase.userAuthInfo();
      dao.insertUserAuthInfo(userAuthInfo);
    });
    executor.start();
    try {
      executor.join();
    } catch (Exception ignore) {}

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

  public void setTokenResponse(TokenResponse tokenResponse) {
    mTokenResponse = tokenResponse;
    UserAuthInfoDao dao = mDatabase.userAuthInfo();
    UserAuthInfo oldUserAuthInfo = dao.findById(0);
    dao.insertUserAuthInfo(new UserAuthInfo(
        0,
        oldUserAuthInfo.authCode,
        oldUserAuthInfo.codeVerifier,
        tokenResponse.getAccessToken(),
        tokenResponse.getRefreshToken(),
        tokenResponse.getTokenType(),
        tokenResponse.getExpireTime(),
        System.currentTimeMillis() / 1000
    ));
  }

  @Nullable
  public TokenResponse getLatestTokenResponse() {
    return mTokenResponse;
  }

  public String getLatestToken() {
    return mTokenResponse.getAccessToken();
  }

  public void revokeToken() {
    String token = getLatestToken();
    if (token == null) {
      return;
    }
    Thread connectionExecutor = new Thread(() -> {
      try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpPost httpPostRequest = new HttpPost(mAuthorizationServerDataSource.getAddressOfRevocationEndpoint());
        httpPostRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("token", token));

        httpPostRequest.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPostRequest)) {
          Log.d(TAG, "AUTH SERVER REVOKE TOKEN");
          Log.d(TAG, httpResponse.toString());
          Log.d(TAG, Arrays.toString(httpResponse.getHeaders()));
          Log.d(TAG, Long.toString(httpResponse.getEntity().getContentLength()));
          byte[] bytes = new byte[(int)(httpResponse.getEntity().getContentLength())];
          httpResponse.getEntity().getContent().read(bytes);
          Log.d(TAG, new String(bytes));
          UserAuthInfoDao dao = mDatabase.userAuthInfo();
          dao.deleteUserAuthInfo(dao.findById(0));
          mAuthorizationRequest = null;
          mCodeVerifier = null;
          mTokenResponse = null;
        } catch (Exception exception) {
          if (exception.getMessage() != null) {
            Log.e(TAG, exception.getMessage());
          }
          exception.printStackTrace();
        }
      } catch (IOException exception) {
        if (exception.getMessage() != null) {
          Log.e(TAG, exception.getMessage());
        }
        exception.printStackTrace();
      }
    });
  connectionExecutor.start();

  try {
    connectionExecutor.join(5000);
  } catch (Exception ignore) {

  }
  }

  public OperationStatus tryRefreshTokenRequest(String refreshToken) {
    Thread connectionExecutor = new Thread(() -> {
      try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpPost httpPostRequest = new HttpPost(mAuthorizationServerDataSource.getAddressOfTokenEndpoint());
        httpPostRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
        parameters.add(new BasicNameValuePair("refresh_token", refreshToken));

        httpPostRequest.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPostRequest)) {
          Log.d(TAG, "Resource SERVER RESPONSE FOR TOKEN REQUEST");
          Log.d(TAG, httpResponse.toString());
          Log.d(TAG, Arrays.toString(httpResponse.getHeaders()));
          Log.d(TAG, Long.toString(httpResponse.getEntity().getContentLength()));
          byte[] bytes = new byte[(int)(httpResponse.getEntity().getContentLength())];
          httpResponse.getEntity().getContent().read(bytes);
          TokenResponse tokenResponse = gson.fromJson(new String(bytes), TokenResponse.class);
          UserAuthInfo old = mDatabase.userAuthInfo().findById(0);
          mDatabase.userAuthInfo().insertUserAuthInfo(new UserAuthInfo(
              old.uid,
              old.authCode,
              old.codeVerifier,
              tokenResponse.getRefreshToken(),
              old.refreshToken,
              old.tokenType,
              tokenResponse.getExpireTime(),
              System.currentTimeMillis() / 1000
          ));
          Log.d(TAG, new String(bytes));
        } catch (Exception exception) {
          if (exception.getMessage() != null) {
            Log.e(TAG, exception.getMessage());
          }
          exception.printStackTrace();
        }
      } catch (IOException exception) {
        if (exception.getMessage() != null) {
          Log.e(TAG, exception.getMessage());
        }
        exception.printStackTrace();
      }
    });

    connectionExecutor.start();

    try {
      connectionExecutor.join(8000);
    } catch (Exception ignore) {}

    return new OperationStatus(true, null);
  }

//  public OperationStatus tryAcquireAuthorizationCode(Context appContext, AuthorizationRequest authorizationRequest) {
//    AuthorizationRequest authorizationRequest =
//        createNewAuthorizationRequest(appContext.getString(R.string.client_id),
//            appContext.getResources().getStringArray(R.array.auth_required_scopes));
//
//    AuthorizationRequest authorizationRequest1 = createNewAuthorizationRequest(
//
//    )
//
//    Uri authorizationRequestUri = authorizationRequest.toUri();
//
//    Log.d(TAG, "Authorization request:" + authorizationRequestUri.toString());
//
//    delegateAuthorizationRequestToCustomTabs(appContext, authorizationRequestUri);
//  }
}
