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
import com.kkafara.fresh.oauth.data.model.RefreshTokenResponse;
import com.kkafara.fresh.oauth.requestfactory.RefreshTokenRequestFactory;

import java.time.Instant;
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
      new MutableLiveData<>(Result.newSuccess(new LoginState(false)));

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
              Result<RefreshTokenResponse, Throwable> result = tryToObtainAccessTokenWithRefreshTokenWithFuture(authInfo).get();
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

  private Future<Result<RefreshTokenResponse, Throwable>>
  tryToObtainAccessTokenWithRefreshTokenWithFuture(@NonNull AuthInfoRecord authInfoRecord) {
    return tryToObtainAccessTokenWithRefreshTokenWithFuture(authInfoRecord.refreshToken);
  }

  private Future<Result<RefreshTokenResponse, Throwable>>
  tryToObtainAccessTokenWithRefreshTokenWithFuture(@NonNull String refreshToken) {
    return mExecutor.submit(
        (Callable<Result<RefreshTokenResponse, Throwable>>) new HttpRequestTask<Result<RefreshTokenResponse, Throwable>>(
        new RefreshTokenRequestFactory(refreshToken),
        response -> {
          Log.d(TAG, "refresh token request received response from server");
          RefreshTokenResponse refreshTokenResponse = HttpBodyDecoders
              .decodeHttpResponseBody(response.getEntity(), RefreshTokenResponse.class);

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
}
