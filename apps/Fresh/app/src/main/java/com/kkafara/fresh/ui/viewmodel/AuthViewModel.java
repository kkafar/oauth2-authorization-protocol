package com.kkafara.fresh.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kkafara.fresh.data.model.LoginState;
import com.kkafara.fresh.data.model.Result;
import com.kkafara.fresh.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
//  private MutableLiveData<Result<LoginState, Throwable>> mLoginStateLiveData =
//      new MutableLiveData<>();

  private AuthRepository mAuthRepository;

  public AuthViewModel(AuthRepository authRepository) {
    mAuthRepository = authRepository;

//    mAuthRepository.getLoginStateLiveData().observeForever(result -> {
//
//    });
  }

  public LiveData<Result<LoginState, Throwable>> getLoginStateLiveData() {
    return mAuthRepository.getLoginStateLiveData();
  }

  public void authorize() {
  }

  public void assertUserIsLoggedIn() {
    mAuthRepository.checkIfUserLoggedIn();
  }
}
