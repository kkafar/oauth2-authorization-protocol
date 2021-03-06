package com.clientapp2.data.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.clientapp2.data.repositories.AuthorizationManager;
import com.clientapp2.data.repositories.UserAuthRepository;
import com.clientapp2.ui.UserAuthState;

public class UserAuthViewModel extends ViewModel {
  public final String TAG = "UserAuthViewModel";
  private MutableLiveData<UserAuthState> mUserState = new MutableLiveData<>(new UserAuthState(false));
  private UserAuthRepository mUserAuthRepository;
  private AuthorizationManager mAuthorizationManager;

  UserAuthViewModel(UserAuthRepository userAuthRepository, AuthorizationManager authorizationManager) {
    mAuthorizationManager = authorizationManager;
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

  public void revokeToken() {
    Log.d(TAG, "revokeToken");
    changeUserAuthState(new UserAuthState(false));
    mAuthorizationManager.revokeToken();
  }
}
