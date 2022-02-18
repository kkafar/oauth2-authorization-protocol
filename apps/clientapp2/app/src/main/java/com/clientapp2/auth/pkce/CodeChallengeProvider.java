package com.clientapp2.auth.pkce;

import androidx.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;

public class CodeChallengeProvider {
  public static final String TAG = "CodeChallengeProvider";

  public CodeChallengeProvider() {}

  /**
   *
   * @param method one of: "plain", "s256" {@see https://datatracker.ietf.org/doc/html/rfc7636#section-4.2}
   * @param codeVerifier
   */
  public String generateCodeChallenge(
      @NonNull CodeChallengeMethod method,
      @NonNull String codeVerifier) throws NoSuchAlgorithmException {

    return method == CodeChallengeMethod.S256 ?
        sha256Generator(codeVerifier) : plainGenerator(codeVerifier);
  }

  private String plainGenerator(@NonNull String codeVerifier) {
    return codeVerifier;
  }

  private String sha256Generator(@NonNull String codeVerifier) throws NoSuchAlgorithmException {
    byte[] bytes = codeVerifier.getBytes();
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    messageDigest.update(bytes, 0, bytes.length);
    String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(messageDigest.digest());
    return codeChallenge;
  }
}
