package com.kkafara.fresh.oauth.pkce;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CodeChallengeGeneratorProvider {
  public final static String TAG = "CodeChallengeGeneratorProvider";

  public static PkceFlowDataGenerator get(
      String codeVerifier,
      String codeChallengeMethod,
      boolean fallbackToPlain) throws NoSuchAlgorithmException {

    Log.d(TAG, "get");

    assert CodeChallengeMethod.isValidMethod(codeChallengeMethod)
        : "Invalid code challenge method";

    if (codeChallengeMethod.equals(CodeChallengeMethod.S256)) {
      if (sha256AlgorithmSupported()) {
        return new CodeChallengeS256Generator(codeVerifier);
      } else if (fallbackToPlain) {
        return new CodeChallengePlainGenerator(codeVerifier);
      } else {
        throw new NoSuchAlgorithmException("SHA-256 algorithm is not supported");
      }
    } else {
      return new CodeChallengePlainGenerator(codeVerifier);
    }
  }

  private static boolean sha256AlgorithmSupported() {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      return true;
    } catch (NoSuchAlgorithmException exception) {
      return false;
    }
  }
}
