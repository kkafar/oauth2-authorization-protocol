package com.dp.data.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dp.data.repositories.UserLoginRepository;
import com.dp.ui.init.UserState;

public class UserViewModel extends ViewModel {
  private MutableLiveData<UserState> mUserState = new MutableLiveData<>();
  private UserLoginRepository mUserLoginRepository;

  UserViewModel(UserLoginRepository userLoginRepository) {
    mUserLoginRepository = userLoginRepository;
  }

  LiveData<UserState> getUserState() {
    return mUserState;
  }

  public void userStateChanged(UserState userState) {
    mUserState.setValue(new UserState(userState.isLoggedIn()));
  }

//  public void

}