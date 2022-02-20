package com.kkafara.fresh.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kkafara.fresh.data.model.LoginState;
import com.kkafara.fresh.data.model.Result;
import com.kkafara.fresh.data.source.AuthDataSource;
import com.kkafara.fresh.database.DatabaseInstanceProvider;
import com.kkafara.fresh.database.MainDatabase;
import com.kkafara.fresh.database.dao.AuthInfoDao;
import com.kkafara.fresh.database.entity.AuthInfoRecord;
import com.kkafara.fresh.net.HttpBodyDecoders;
import com.kkafara.fresh.net.HttpRequestTask;
import com.kkafara.fresh.oauth.data.model.AuthCodeRequest;
import com.kkafara.fresh.oauth.data.model.AuthCodeResponse;
import com.kkafara.fresh.oauth.data.model.TokenResponse;
import com.kkafara.fresh.oauth.pkce.CodeChallengeGeneratorProvider;
import com.kkafara.fresh.oauth.pkce.CodeChallengeMethod;
import com.kkafara.fresh.oauth.pkce.CodeChallengeS256Generator;
import com.kkafara.fresh.oauth.pkce.CodeVerifierGenerator;
import com.kkafara.fresh.oauth.pkce.PkceFlowDataGenerator;
import com.kkafara.fresh.oauth.pkce.StateProvider;
import com.kkafara.fresh.oauth.requestfactory.AccessTokenRequestFactory;
import com.kkafara.fresh.oauth.requestfactory.RefreshTokenRequestFactory;
import com.kkafara.fresh.oauth.util.OAuthHttpParameter;
import com.kkafara.fresh.servers.AuthServerInfo;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AuthRepository {
  public final String TAG = "AuthRepository";

  private static volatile AuthRepository INSTANCE = null;

  private AuthDataSource mAuthDataSource;

  private MainDatabase mDatabase;

  private AuthInfoDao mAuthInfoDao;

  private ExecutorService mExecutor;

  private final int EXECUTOR_THREADS = 2;

  private MutableLiveData<Result<LoginState, Throwable>> mLoginStateLiveData =
//      new MutableLiveData<>(Result.newSuccess(new LoginState(false)));
      new MutableLiveData<>();

  private AuthRepository(AuthDataSource dataSource) {
    mAuthDataSource = dataSource;

    // we assume that the database has been created earlier, while initiating MainActivity
    mDatabase = DatabaseInstanceProvider.getInstance(null);
    mAuthInfoDao = mDatabase.getAuthInfoDao();

    mExecutor = Executors.newFixedThreadPool(EXECUTOR_THREADS);
  }

  public static synchronized AuthRepository getInstance(AuthDataSource dataSource) {
    if (INSTANCE == null) {
      INSTANCE = new AuthRepository(dataSource);
    }
    return INSTANCE;
  }

  public LiveData<Result<LoginState, Throwable>> getLoginStateLiveData() {
    return mLoginStateLiveData;
  }

  private void pushResultToLiveDataStream(Result<LoginState, Throwable> result) {
    Log.d(TAG, "pushResultToLiveDataStream");
    mLoginStateLiveData.postValue(result);
  }


  public void checkIfUserLoggedIn() {
    Log.d(TAG, "checkIfUserLoggedIn");
    mExecutor.submit(() -> {
      // first we check if there is anything stored in database
      AuthInfoRecord authInfo = mAuthInfoDao.findByUserId(0);

      if (authInfo == null) {
        pushResultToLiveDataStream(Result.newSuccess(new LoginState(false)));
      } else { // data record is available
        if (authInfo.accessToken != null) {
          // there are 2 main cases:
          // 1: token is valid
          if (isAccessTokenValidTimeWise(authInfo)) {
            // we return true, but be aware, that token could be revoked on server side!
            // we can only verify it on data request (or on additional endpoint)

            // TODO: CHECK IF TOKEN HAS NOT BEEN REVOKED ON SERVER SIDE

            pushResultToLiveDataStream(Result.newSuccess(new LoginState(true)));
          } else if (hasRefreshToken(authInfo)) {
            // 2: token has expired, but there is refresh token
            // try to obtain the access token
            try {
              Result<TokenResponse, Throwable> result = tryToObtainAccessTokenWithRefreshTokenWithFuture(authInfo).get();
              if (result.isError()) {
                pushResultToLiveDataStream(Result.newSuccess(new LoginState(false)));
              } else {
                pushResultToLiveDataStream(Result.newSuccess(new LoginState(true)));
              }
            } catch (ExecutionException | InterruptedException e) {
              // if this process failed, we return that user is not logged in
              pushResultToLiveDataStream(Result.newSuccess(new LoginState(false)));
            }
          } else {
            // token has expired and there is no refresh token
            pushResultToLiveDataStream(Result.newSuccess(new LoginState(false)));
          }
        } else {
          pushResultToLiveDataStream(Result.newSuccess(new LoginState(false)));
        }
      }
    });
  }

  private Future<Result<TokenResponse, Throwable>>
  tryToObtainAccessTokenWithRefreshTokenWithFuture(@NonNull AuthInfoRecord authInfoRecord) {
    return tryToObtainAccessTokenWithRefreshTokenWithFuture(authInfoRecord.refreshToken);
  }

  private Future<Result<TokenResponse, Throwable>>
  tryToObtainAccessTokenWithRefreshTokenWithFuture(@NonNull String refreshToken) {
    return mExecutor.submit(
        (Callable<Result<TokenResponse, Throwable>>) new HttpRequestTask<Result<TokenResponse, Throwable>>(
        new RefreshTokenRequestFactory(refreshToken),
        response -> {
          Log.d(TAG, "refresh token request received response from server");
          TokenResponse refreshTokenResponse = HttpBodyDecoders
              .decodeHttpResponseBody(response.getEntity(), TokenResponse.class);

          if (refreshTokenResponse == null) {
            return Result.newError(new RuntimeException("Response had empty body"));
          } else if (refreshTokenResponse.isError()) {
            return Result.newError(new RuntimeException(refreshTokenResponse.getError()));
          } else {
            return Result.newSuccess(refreshTokenResponse);
          }
        },
        exception -> {
          Log.d(TAG, "refresh token failed due to some connection issue");
          return Result.newError(exception);
        }
    ));
  }

  private boolean hasRefreshToken(AuthInfoRecord authInfoRecord) {
    return authInfoRecord.refreshToken != null;
  }


  private boolean isAccessTokenValidTimeWise(AuthInfoRecord authInfoRecord) {
    return Instant.now().getEpochSecond() < authInfoRecord.tokenGrantTime + authInfoRecord.tokenExpireTime;
  }

  public AuthCodeRequest createNewAuthorizationCodeRequest(
      @NonNull String clientId,
      @NonNull Set<String> scopes
  ) {
    Log.d(TAG, "createNewAuthorizationCodeRequest");

    String responseType = OAuthHttpParameter.CODE;

    String codeVerifier = new CodeVerifierGenerator().generate();

    AuthInfoRecord authInfoRecord = new AuthInfoRecord(
        0,
        null,
        codeVerifier,
        null,
        null,
        null,
        -1,
        -1
    );

    // TODO: possible race condition -> it would be the best to synchronize it by calling
    // guard.get();
    Future<?> guard = mExecutor.submit(() -> mAuthInfoDao.insertAuthInfoRecord(authInfoRecord));

    Log.d(TAG, "code verifier in Base64: " + codeVerifier);

    PkceFlowDataGenerator codeChallengeGenerator;
    String codeChallengeMethod;

    try {
      codeChallengeGenerator = CodeChallengeGeneratorProvider
          .get(codeVerifier, CodeChallengeMethod.S256, false);

      codeChallengeMethod = CodeChallengeMethod.S256;
    } catch (NoSuchAlgorithmException exception) {
      exception.printStackTrace();

      Log.d(TAG, "Falling back to plain method");

      codeChallengeMethod = CodeChallengeMethod.PLAIN;
      try {
        codeChallengeGenerator = CodeChallengeGeneratorProvider.get(codeVerifier, CodeChallengeMethod.PLAIN, false);
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        Log.wtf(TAG, "PLAIN METHOD FAILED. THIS SHOULD NOT HAPPEN");
        throw new RuntimeException("PLAINT METHOD FAILED. THIS SHOULD NOT HAPPEN");
      }
    }

    String codeChallenge = codeChallengeGenerator.generate();
    assert codeChallenge != null : "null code challenge";

    String state = new StateProvider().generate();
    assert state != null : "null state";


    try {
      guard.get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
      Log.wtf(TAG, "inserting to db failed; THIS SHOULD NOT HAPPEN");
    }

    return new AuthCodeRequest(
        AuthServerInfo.AUTHORITY,
        clientId,
        responseType,
        codeChallenge,
        codeChallengeMethod,
        "fresh://main",
        state,
        scopes
    );
  }

  public void getAccessTokenByAuthCode(
      @NonNull String clientId,
      AuthCodeResponse authCodeResponse
  ) throws ExecutionException, InterruptedException {
    AuthInfoRecord authInfoRecord = mExecutor.submit(
        () -> mAuthInfoDao.findByUserId(0)).get();

    mExecutor.submit((Runnable) new HttpRequestTask<Void>(
        new AccessTokenRequestFactory(authCodeResponse, clientId, authInfoRecord.codeVerifier),
        httpResponse -> {
          Log.d(TAG, "AUTH SERVER RESPONSE FOR TOKEN REQUEST");
          Log.d(TAG, httpResponse.toString());
          Log.d(TAG, Arrays.toString(httpResponse.getHeaders()));

          TokenResponse response = HttpBodyDecoders
              .decodeHttpResponseBody(httpResponse.getEntity(), TokenResponse.class);

          if (response == null) {
            pushResultToLiveDataStream(Result.newError(new RuntimeException("null body response")));
          } else if (response.isError()) {
            pushResultToLiveDataStream(Result.newError(new RuntimeException("ERROR: " + response.error)));
          } else {
            AuthInfoRecord newAuthInfoRecord = new AuthInfoRecord(
                0,
                authCodeResponse.code,
                authInfoRecord.codeVerifier,
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getTokenType(),
                response.getExpireTime(),
                Instant.now().getEpochSecond()
            );
            mAuthInfoDao.insertAuthInfoRecord(newAuthInfoRecord);
            pushResultToLiveDataStream(Result.newSuccess(new LoginState(true)));
          }
          return null;
        },
        exception -> {
          pushResultToLiveDataStream(Result.newError(exception));
          return null;
        }
    ));
  }

//    public void validateAuthorizationResponse(AuthCodeResponse authorizationResponse) {
//
//    boolean invalid = false;
//    StringBuilder errorMessageBuilder = new StringBuilder();
//
//    if (authorizationResponse == null) {
//      throw new InvalidAuthorizationResponseException("Null response");
//    }
//
//    if (authorizationResponse.mCode == null) {
//      invalid = true;
//      errorMessageBuilder.append(AuthorizationResponseError.NO_CODE_GRANT).append('\n');
//    }
//    if (authorizationResponse.mState == null) {
//      invalid = true;
//      errorMessageBuilder.append(AuthorizationResponseError.NO_STATE).append('\n');
//    }
//    assert authorizationResponse.mState != null;
//
////    if (!authorizationResponse.mState.equals(request.mState)) {
////      invalid = true;
////      errorMessageBuilder.append(AuthorizationResponseError.BAD_STATE).append('\n');
////    }
//    if (invalid) {
//      throw new InvalidAuthorizationResponseException(errorMessageBuilder.toString());
//    }
//  }
}
