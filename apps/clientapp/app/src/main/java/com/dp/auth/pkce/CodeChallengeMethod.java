package com.dp.auth.pkce;

public enum CodeChallengeMethod {
  PLAIN("plain"),
  S256("s256");

  private final String asString;

  private CodeChallengeMethod(String methodName) {
    asString = methodName;
  }

  @Override
  public String toString() {
    return asString;
  }
}
