package com.kkafara.fresh.oauth.data.model;

import androidx.annotation.NonNull;

public class TokenRevocationRequest {
  @NonNull
  private final String mToken;

  public TokenRevocationRequest(@NonNull String token) {
    mToken = token;
  }

  @NonNull
  public String getToken() {
    return mToken;
  }
}
