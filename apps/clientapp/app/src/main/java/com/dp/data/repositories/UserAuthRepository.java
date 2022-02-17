package com.dp.data.repositories;

import androidx.annotation.Nullable;

import com.dp.auth.model.AuthorizationRequest;
import com.dp.data.datasources.UserLoginDataSource;

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
