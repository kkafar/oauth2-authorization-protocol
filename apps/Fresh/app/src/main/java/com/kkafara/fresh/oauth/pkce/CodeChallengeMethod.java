package com.kkafara.fresh.oauth.pkce;

public class CodeChallengeMethod {
  public final static String PLAIN = "plain";
  public final static String S256 = "s256";

  public static boolean isValidMethod(String method) {
    return method.equals(PLAIN) || method.equals(S256);
  }
}
