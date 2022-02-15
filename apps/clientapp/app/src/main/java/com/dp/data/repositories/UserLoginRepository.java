package com.dp.data.repositories;

import com.dp.data.datasources.UserLoginDataSource;

public class UserLoginRepository {
  private static volatile UserLoginRepository instance;

  private UserLoginDataSource mDataSource;

  private UserLoginRepository(UserLoginDataSource dataSource) {
    mDataSource = dataSource;
  }

  public static UserLoginRepository getInstance(UserLoginDataSource dataSource) {
    if (instance == null) {
      instance = new UserLoginRepository(dataSource);
    }
    return instance;
  }
}
