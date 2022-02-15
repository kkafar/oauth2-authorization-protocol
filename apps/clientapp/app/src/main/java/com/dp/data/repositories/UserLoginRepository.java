package com.dp.data.repositories;

import androidx.annotation.Nullable;

import com.dp.auth.model.AuthorizationRequest;
import com.dp.data.datasources.UserLoginDataSource;

public class UserLoginRepository {
  private static volatile UserLoginRepository instance;

  private UserLoginDataSource mDataSource;

  @Nullable
  private AuthorizationRequest mAuthorizationRequest;

//  @Nullable
//  private TokenRequest mTokenRequest;


  private UserLoginRepository(UserLoginDataSource dataSource) {
    mDataSource = dataSource;
  }

  public static UserLoginRepository getInstance(UserLoginDataSource dataSource) {
    if (instance == null) {
      instance = new UserLoginRepository(dataSource);
    }
    return instance;
  }

  @Nullable
  public AuthorizationRequest getAuthorizationRequest() {
    return mAuthorizationRequest;
  }

  private void setAuthorizationRequest(AuthorizationRequest request) {
    mAuthorizationRequest = request;
  }

  @Nullable
  public AuthorizationRequest consumeAuthorizationRequest() {
    AuthorizationRequest request = mAuthorizationRequest;
    mAuthorizationRequest = null;
    return request;
  }
}
