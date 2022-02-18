package com.clientapp2.data.datasources;

import androidx.annotation.NonNull;

import com.clientapp2.auth.AuthorizationServerEndpointName;

import org.jetbrains.annotations.Contract;

public class AuthorizationServerDataSource {
  private final String mHttpsAddress;
  private final String mAuthority;

  public AuthorizationServerDataSource() {
    mAuthority = "8e4d-91-123-181-221.ngrok.io";
    mHttpsAddress = "https://" + mAuthority;
  }

  public String getAddress() {
    return mHttpsAddress;
  }

  public String getAuthority() {
    return mAuthority;
  }

  public String getAddressOfRevocationEndpoint() {
    return joinAddresses(mHttpsAddress, AuthorizationServerEndpointName.REVOCATION.toString());
  }

  public String getAddressOfIntrospectionEndpoint() {
    return joinAddresses(mHttpsAddress, AuthorizationServerEndpointName.INTROSPECTION.toString());
  }

  public String getAddressOfTokenEndpoint() {
    return joinAddresses(mHttpsAddress, AuthorizationServerEndpointName.TOKEN.toString());
  }

  public String getAddressOfAuthorizationEndpoint() {
    return joinAddresses(mHttpsAddress, AuthorizationServerEndpointName.AUTHORIZATION.toString());
  }

  public String getAddressOfPingEndpoint() {
    return joinAddresses(mHttpsAddress, AuthorizationServerEndpointName.PING_TEST.toString());
  }

  public String getAddressForEndpoint(@NonNull AuthorizationServerEndpointName endpointName) {
    return joinAddresses(mHttpsAddress, endpointName.toString());
  }

  @NonNull
  @Contract(pure = true)
  private String joinAddresses(@NonNull String first, @NonNull String second) {
    return first + '/' + second;
  }
}
