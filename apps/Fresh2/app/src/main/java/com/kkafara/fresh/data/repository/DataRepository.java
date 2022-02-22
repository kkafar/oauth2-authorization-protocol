package com.kkafara.fresh.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kkafara.fresh.data.model.DataRequest;
import com.kkafara.fresh.data.model.DataResponse;
import com.kkafara.fresh.data.model.LoginState;
import com.kkafara.fresh.data.model.Result;
import com.kkafara.fresh.data.source.DataSource;
import com.kkafara.fresh.data.util.DataScopeParser;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataRepository {
  public final String TAG = "DataRepository";

  private MutableLiveData<Result<DataResponse, Throwable>> mDataRequestResultLiveData = new MutableLiveData<>();

  private DataSource mDataSource;
  private AuthRepository mAuthRepository;
  private ExecutorService mExecutor;

  public DataRepository(@NonNull DataSource dataSource) {
    Log.d(TAG, "ctor");

    mExecutor = Executors.newSingleThreadExecutor();

    mDataSource = dataSource;

    mDataSource.getDataRequestResultLiveData().observeForever(result -> {
      Log.d(TAG, "received value from data source");
      mDataRequestResultLiveData.setValue(result);
    });

    // we assume that authorization repository has been created earlier
    mAuthRepository = AuthRepository.getInstance(null);
  }

  public LiveData<Result<DataResponse, Throwable>> getDataResponseLiveData() {
    return mDataRequestResultLiveData;
  }

  private void pushResultToLiveDataStream(Result<DataResponse, Throwable> result) {
    mDataRequestResultLiveData.postValue(result);
  }

  public void fetchDataAsync(Set<String> scopes) {
    Log.d(TAG, "fetchData");
    mExecutor.submit(() -> {
      // Assure that user is logged in
      LoginState loginState = mAuthRepository.checkIfUserLoggedIn();

      if (!loginState.isLoggedIn()) {
        pushResultToLiveDataStream(Result.newError(new RuntimeException("User is not logged in")));
        return;
      }

      Optional<String> accessToken = mAuthRepository.getAccessToken();
      if (accessToken.isPresent()) {
        mDataSource.fetchData(new DataRequest(accessToken.get(), DataScopeParser.stringFromStringSet(scopes)));
      } else {
        pushResultToLiveDataStream(Result.newError(new RuntimeException("Failed to fetch token from auth repository")));
      }
    });
  }
}
