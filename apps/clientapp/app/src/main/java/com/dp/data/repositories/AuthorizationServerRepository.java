package com.dp.data.repositories;

import com.dp.data.datasources.AuthorizationServerDataSource;

public class AuthorizationServerRepository {
  private static volatile AuthorizationServerRepository instance;

  private AuthorizationServerDataSource mDataSource;


  private AuthorizationServerRepository(AuthorizationServerDataSource dataSource) {
    mDataSource = dataSource;
  }

  public static AuthorizationServerRepository getInstance(AuthorizationServerDataSource dataSource) {
    if (instance == null) {
      instance = new AuthorizationServerRepository(dataSource);
    }
    return instance;
  }

  public String getAuthServerAddress() {
    return mDataSource.getAuthorizationServerAddress();
  }

  public String getAuthServerAuthority() {
    return mDataSource.getAuthority();
  }

  public String getAuthority() {
    return mDataSource.getAuthorizationServerAddress()
        .substring(mDataSource.getAuthorizationServerAddress().lastIndexOf('/') + 1);
  }
}
