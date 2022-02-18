package com.dp.data.datasources;

public class ResourceServerDataSource {
  private final String mHttpsAddress;
  private final String mAuthority;

  public ResourceServerDataSource() {
    mAuthority = "5ce7-185-233-24-186.ngrok.io";
    mHttpsAddress = "https://" + mAuthority;
  }

  public String getAddress() { return mHttpsAddress; }

  public String getAuthority() { return mAuthority; }
}
