package com.dp.data.datasources;

public class ResourceServerDataSource {
  private final String mHttpsAddress;
  private final String mAuthority;

  public ResourceServerDataSource() {
//    mAuthority = "ad01-31-182-161-222.ngrok.io";
    mAuthority = "3293-91-123-181-221.ngrok.io";
    mHttpsAddress = "https://" + mAuthority;
  }

  public String getAddress() { return mHttpsAddress; }

  public String getAuthority() { return mAuthority; }
}
