package com.kkafara.fresh.oauth.pkce;

import android.util.Log;

import androidx.annotation.NonNull;

public class CodeChallengePlainGenerator implements PkceFlowDataGenerator {
  public final String TAG = "CodeChallengePlainGenerator";

  private final String mCodeVerifier;

  public CodeChallengePlainGenerator(@NonNull String codeVerifier) {
    mCodeVerifier = codeVerifier;
  }

  @Override
  public String generate() {
    Log.d(TAG, "generate");
    Log.d(TAG, mCodeVerifier);
    return mCodeVerifier;
  }
}
