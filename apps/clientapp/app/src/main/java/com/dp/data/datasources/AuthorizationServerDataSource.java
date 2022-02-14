package com.dp.data.datasources;

public class AuthorizationServerDataSource {
  private final String mHttpsAddress;
  private final String mAuthority;

  public AuthorizationServerDataSource() {
    mAuthority = "ab1d-185-233-24-186.ngrok.io";
    mHttpsAddress = "https://" + mAuthority;
  }

  public String getAuthorizationServerAddress() {
    return mHttpsAddress;
  }

  public String getAuthority() {
    return mAuthority;
  }
}
