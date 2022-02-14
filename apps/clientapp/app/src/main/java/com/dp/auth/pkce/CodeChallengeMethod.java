package com.dp.auth.pkce;

/**
 * see https://datatracker.ietf.org/doc/html/rfc7636#section-4.2
 */
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
