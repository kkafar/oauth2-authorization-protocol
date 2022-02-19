package com.dp.data.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dp.data.repositories.AuthorizationManager;
import com.dp.data.repositories.UserAuthRepository;
import com.dp.ui.UserAuthState;

import java.util.concurrent.ExecutionException;

public class UserAuthViewModel extends ViewModel {
  public final String TAG = "UserAuthViewModel";
  private MutableLiveData<UserAuthState> mUserState = new MutableLiveData<>(new UserAuthState(false));
  private UserAuthRepository mUserAuthRepository;
  private AuthorizationManager mAuthorizationManager;

  private String mContext;

  UserAuthViewModel(UserAuthRepository userAuthRepository, AuthorizationManager authorizationManager) {
    mAuthorizationManager = authorizationManager;
    mUserAuthRepository = userAuthRepository;
    mContext = null;
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
    changeUserAuthState(new UserAuthState(false, "revocation"));
    mAuthorizationManager.revokeToken();
  }

  public void deleteUserAuthorizationInfo() {
    try {
      mAuthorizationManager.deleteUserAuthInfo().get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void notifyOnDataFetchFailure(String context) {
    if (mContext == null) {
      mContext = context;
    } else if (mContext.equals("data_fetch_failed")) {
      deleteUserAuthorizationInfo();
      mContext = null;
    }
  }
}
