package com.dp.data.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dp.data.repositories.AuthorizationManager;
import com.dp.data.repositories.UserAuthRepository;
import com.dp.ui.UserAuthState;

public class UserAuthViewModel extends ViewModel {
  public final String TAG = "UserAuthViewModel";
  private MutableLiveData<UserAuthState> mUserState = new MutableLiveData<>(new UserAuthState(false));
  private UserAuthRepository mUserAuthRepository;

  UserAuthViewModel(UserAuthRepository userAuthRepository, AuthorizationManager authorizationManager) {
    mUserAuthRepository = userAuthRepository;
  }

  public LiveData<UserAuthState> getUserAuthState() {
    return mUserState;
  }

  public void changeUserAuthState(UserAuthState userAuthState) {
    Log.d(TAG, "Changing user login state to: " + userAuthState.isLoggedIn());
    mUserState.setValue(userAuthState);
  }

  public boolean isUserLoggedIn() {
    verifyUserAuthState();
    return mUserState.getValue().isLoggedIn();
  }

  private void verifyUserAuthState() {
  }
}
