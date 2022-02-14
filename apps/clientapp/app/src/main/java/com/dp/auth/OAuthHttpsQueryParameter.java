package com.dp.auth;

public enum OAuthHttpsQueryParameter {
  RESPONSE_TYPE("response_type"),
  CLIENT_ID("client_id"),
  REDIRECT_URI("redirect_uri"),
  SCOPE("scope"),
  STATE("state"),
  CODE_CHALLENGE("code_challenge"),
  CODE_CHALLENGE_METHOD("code_challenge_method");

  private final String asString;

  private OAuthHttpsQueryParameter(String asString) {
    this.asString = asString;
  }

  @Override
  public String toString() {
    return asString;
  }
}
