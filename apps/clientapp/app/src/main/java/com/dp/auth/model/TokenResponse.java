package com.dp.auth.model;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class TokenResponse {
  private String access_token;
  private String token_type;
  private int expires_in;

  public TokenResponse(String accessToken,
                       String tokenType,
                       String expireTime) {
    access_token = accessToken;
    token_type = tokenType;
    expires_in = expireTime != null ? Integer.parseInt(expireTime) : null;
  }

  public String getAccessToken() {
    return access_token;
  }

  public String getTokenType() {
    return token_type;
  }

  public int getExpireTime() {
    return expires_in;
  }

  @NonNull
  public Intent toIntent() {
    Intent intent = new Intent();
    intent.putExtra("access_token", access_token);
    intent.putExtra("token_type", token_type);
    intent.putExtra("expires_in", Integer.toString(expires_in));
    return intent;
  }

  public static TokenResponse fromIntent(@NonNull Intent intent) {
    return fromBundle(intent.getExtras());
  }

  public static TokenResponse fromBundle(@NonNull Bundle data) {
    return new TokenResponse(
        data.getString("access_token"),
        data.getString("token_type"),
        data.getString("expires_in")
    );
  }
}
