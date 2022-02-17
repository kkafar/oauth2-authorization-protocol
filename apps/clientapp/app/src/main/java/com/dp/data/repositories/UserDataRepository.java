package com.dp.data.repositories;

import com.dp.data.datasources.UserDataDataSource;

public class UserDataRepository {
  private volatile static UserDataRepository instance;
  private UserDataDataSource mDataSource;

  private UserDataRepository(UserDataDataSource dataSource) {
    mDataSource = dataSource;
  }

  public static UserDataRepository getInstance(UserDataDataSource dataSource) {
    if (instance == null) {
      return new UserDataRepository(dataSource);
    }
    return instance;
  }
}
