package com.clientapp3.data.repositories;

import android.util.Log;

import com.clientapp3.data.datasources.UserDataDataSource;
import com.clientapp3.ui.userdata.UserDataState;

public class UserDataRepository {
  public final String TAG = "UserDataRepository";
  private volatile static UserDataRepository instance;
  private UserDataDataSource mDataSource;

  private AuthorizationManager mAuthorizationManager;

  private UserDataRepository(UserDataDataSource dataSource) {
    mDataSource = dataSource;
    mAuthorizationManager = AuthorizationManager.getInstance();
  }

  public static UserDataRepository getInstance(UserDataDataSource dataSource) {
    if (instance == null) {
      return new UserDataRepository(dataSource);
    }
    return instance;
  }

  public UserDataState updateUserData(String parsedScopes) {
    Log.d(TAG, "updateUserData");
    return mDataSource
        .fetchUserDataFromServer(
            mAuthorizationManager.getLatestTokenSync(),
            parsedScopes);
  }
}
