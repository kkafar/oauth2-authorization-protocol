package com.dp.auth;

public enum AuthorizationServerEndpointName {
  REVOCATION("revoke"),
  INTROSPECTION("introspect"),
  TOKEN("token"),
  AUTHORIZATION("authorize"),
  PING_TEST("ping");


  private final String asString;

  private AuthorizationServerEndpointName(String asString) {
    this.asString = asString;
  }

  @Override
  public String toString() {
    return asString;
  }
}
