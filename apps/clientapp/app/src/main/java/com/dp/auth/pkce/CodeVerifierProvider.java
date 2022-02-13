package com.dp.auth.pkce;

import java.util.Base64;
import java.security.SecureRandom;


public class CodeVerifierProvider {
  private static final char[] ALPHABET = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm0123456789-._~".toCharArray();
  private static final int MAX_LENGTH = 128;
  private static final int MIN_LENGTH = 43;

  public CodeVerifierProvider() {}

  public String generateCodeVerifier() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] codeVerifier = new byte[getLength()];
    secureRandom.nextBytes(codeVerifier);
    return Base64.getUrlEncoder().encodeToString(codeVerifier);
  }

  private static int getLength() {
    return MAX_LENGTH;
  }
}
