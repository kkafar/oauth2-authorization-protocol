package com.kkafara.fresh.oauth.pkce;

import android.util.Log;

import androidx.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CodeChallengeS256Generator implements PkceFlowDataGenerator {
  public final String TAG = "CodeChallengeS256Generator";

  private String mCodeVerifier = null;

  public CodeChallengeS256Generator(@NonNull String codeVerifier) {
    mCodeVerifier = codeVerifier;
  }

  @Override
  public String generate() {
    Log.d(TAG, "generate");
    byte[] bytes = mCodeVerifier.getBytes();
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException exception) {
      Log.wtf(TAG, "THIS SHOULD NOT HAPPEN");
      exception.printStackTrace();
      return null;
    }
    messageDigest.update(bytes, 0, bytes.length);
    String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(messageDigest.digest());
    Log.d(TAG, codeChallenge);
    return codeChallenge;
  }
}
