package com.kkafara.fresh.oauth.pkce;

import android.util.Log;

import androidx.annotation.NonNull;

import java.security.SecureRandom;
import java.util.Base64;

public class CodeVerifierGenerator implements PkceFlowDataGenerator {
  public final String TAG = "CodeVerifierGenerator";

  private final int MAX_LENGTH = 128;
  private final int MIN_LENGTH = 43;

  private final int mLength;

  public CodeVerifierGenerator() {
    mLength = MIN_LENGTH;
  }

  public CodeVerifierGenerator(@NonNull int length) {
    if (isValidLength(length)) {
      mLength = length;
    } else {
      throw new IllegalArgumentException("Code verifier length must be >= 32 && <= 128");
    }
  }

  @Override
  public String generate() {
    Log.d(TAG, "generate");
    SecureRandom secureRandom = new SecureRandom();
    byte[] codeVerifier = new byte[mLength];
    secureRandom.nextBytes(codeVerifier);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
  }

  private boolean isValidLength(int length) {
    return length >= MIN_LENGTH && length <= MAX_LENGTH;
  }
}
