package com.clientapp3.data.repositories;

import androidx.annotation.NonNull;

import com.clientapp3.auth.AuthorizationServerEndpointName;
import com.clientapp3.data.datasources.AuthorizationServerDataSource;

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
    return mDataSource.getAddress();
  }

  public String getAuthServerAuthority() {
    return mDataSource.getAuthority();
  }

  public String getAuthority() {
    return mDataSource.getAddress()
        .substring(mDataSource.getAddress().lastIndexOf('/') + 1);
  }

  public String getAddressForEndpoint(@NonNull AuthorizationServerEndpointName endpointName) {
    return mDataSource.getAddressForEndpoint(endpointName);
  }
}
