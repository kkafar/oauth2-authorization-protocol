package com.dp.auth.pkce;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;

@RequiresApi(api = Build.VERSION_CODES.R)
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

    return method == CodeChallengeMethod.S256 ? sha256Generator(codeVerifier) : plainGenerator(codeVerifier);
  }

  private String plainGenerator(String codeVerifier) {
    return codeVerifier;
  }

  private String sha256Generator(String codeVerifier) throws NoSuchAlgorithmException {
    byte[] bytes = codeVerifier.getBytes();
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    messageDigest.update(bytes, 0, bytes.length);
    String codeChallenge = Base64.getUrlEncoder().encodeToString(messageDigest.digest());
    return codeChallenge;
  }
}
