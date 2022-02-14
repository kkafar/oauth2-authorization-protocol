package com.dp.data.datasources;

public class AuthorizationServerDataSource {
  private final String mHttpsAddress;
  private final String mAuthority;

  public AuthorizationServerDataSource() {
    mAuthority = "80ef-89-70-9-88.ngrok.io";
    mHttpsAddress = "https://" + mAuthority;
  }

  public String getAuthorizationServerAddress() {
    return mHttpsAddress;
  }

  public String getAuthority() {
    return mAuthority;
  }
}
