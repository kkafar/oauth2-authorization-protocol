package com.kkafara.fresh.oauth.data.model;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TokenResponse extends OAuthErrorResponse {
  @NonNull
  private final String access_token;

  @NonNull
  private final String token_type;

  @Nullable
  private final String refresh_token;

  private final int expires_in;

  public TokenResponse(@NonNull String accessToken,
                       @NonNull String tokenType,
                       @Nullable String refreshToken,
                       @Nullable String expireTime,
                       @Nullable String error) {
    super(error);
    access_token = accessToken;
    token_type = tokenType;
    refresh_token = refreshToken;
    expires_in = expireTime != null ? Integer.parseInt(expireTime) : null;
  }

  public String getAccessToken() {
    return access_token;
  }

  public String getRefreshToken() {
    return refresh_token;
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
    intent.putExtra("refresh_token", refresh_token);
    intent.putExtra("expires_in", Integer.toString(expires_in));
    intent.putExtra("error", error);
    return intent;
  }

  public static TokenResponse fromIntent(@NonNull Intent intent) {
    return fromBundle(intent.getExtras());
  }

  public static TokenResponse fromBundle(@NonNull Bundle data) {
    return new TokenResponse(
        data.getString("access_token"),
        data.getString("token_type"),
        data.getString("refresh_token"),
        data.getString("expires_in"),
        data.getString("error")
    );
  }

  @Override
  public boolean isError() {
    return access_token == null || token_type == null || error != null;
  }
}
