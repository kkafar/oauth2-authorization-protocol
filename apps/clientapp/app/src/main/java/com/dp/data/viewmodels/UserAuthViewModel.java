package com.dp.data.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dp.data.repositories.UserLoginRepository;
import com.dp.ui.UserAuthState;

public class UserAuthViewModel extends ViewModel {
  private MutableLiveData<UserAuthState> mUserState = new MutableLiveData<>(new UserAuthState(false));
  private UserLoginRepository mUserLoginRepository;

  UserAuthViewModel(UserLoginRepository userLoginRepository) {
    mUserLoginRepository = userLoginRepository;
  }

  public LiveData<UserAuthState> getUserAuthState() {
    return mUserState;
  }

  public void changeUserAuthState(UserAuthState userAuthState) {
    mUserState.setValue(userAuthState);
  }

  public boolean isUserLoggedIn() {
    return mUserState.getValue().isLoggedIn();
  }
}
