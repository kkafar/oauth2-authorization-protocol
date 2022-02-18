package com.clientapp2.data.repositories;

import androidx.annotation.Nullable;

import com.clientapp2.auth.model.AuthorizationRequest;
import com.clientapp2.data.datasources.UserLoginDataSource;

public class UserAuthRepository {
  private static volatile UserAuthRepository instance;

  private UserLoginDataSource mDataSource;

  @Nullable
  private AuthorizationRequest mAuthorizationRequest;

//  @Nullable
//  private TokenRequest mTokenRequest;


  private UserAuthRepository(UserLoginDataSource dataSource) {
    mDataSource = dataSource;
  }

  public static UserAuthRepository getInstance(UserLoginDataSource dataSource) {
    if (instance == null) {
      instance = new UserAuthRepository(dataSource);
    }
    return instance;
  }
}
