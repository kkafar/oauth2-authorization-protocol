package com.dp.data.datasources;

public class AuthorizationServerDataSource {
  private final String mAuthServerBaseUri = "https://80ef-89-70-9-88.ngrok.io";

  public AuthorizationServerDataSource() {}

  public String getAuthorizationServerAddress() {
    return mAuthServerBaseUri;
  }
}
