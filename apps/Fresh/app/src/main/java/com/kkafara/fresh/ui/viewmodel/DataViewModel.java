package com.kkafara.fresh.ui.viewmodel;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kkafara.fresh.data.model.DataRequest;
import com.kkafara.fresh.data.model.DataResponse;
import com.kkafara.fresh.data.model.LoginState;
import com.kkafara.fresh.data.model.Result;
import com.kkafara.fresh.data.repository.AuthRepository;
import com.kkafara.fresh.data.repository.DataRepository;
import com.kkafara.fresh.data.util.DataScopeParser;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DataViewModel extends ViewModel {
  public final String TAG = "DataViewModel";

  private DataRepository mDataRepository;
  private Activity mOwnerActivity;
  private AuthRepository mAuthRepository;

  private MutableLiveData<Result<DataResponse, Throwable>> mDataResponseLiveData = new MutableLiveData<>();

  public DataViewModel(DataRepository dataRepository, AuthRepository authRepository) {
    mDataRepository = dataRepository;
    mAuthRepository = authRepository;

    mDataRepository.getDataResponseLiveData().observeForever(result -> {
      Log.d(TAG, "DataRepository result observer");
      mDataResponseLiveData.setValue(result);
    });
  }

  public LiveData<Result<DataResponse, Throwable>> getDataResponseLiveData() {
//    return mDataResponseLiveData;
    return mDataRepository.getDataResponseLiveData();
  }

  public void fetchData(Set<String> scopes) {
    LoginState loginState = null;
    try {
      loginState = mAuthRepository.checkIfUserLoggedInAsync().get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
    if (loginState != null && loginState.isLoggedIn()) {
      mDataRepository.fetchData(new DataRequest("NOT NEEDED KURDE", DataScopeParser.stringFromStringSet(scopes)));
    }
  }
}
