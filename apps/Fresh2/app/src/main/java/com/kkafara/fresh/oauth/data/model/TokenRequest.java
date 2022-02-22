package com.kkafara.fresh.oauth.data.model;

import androidx.annotation.NonNull;

public class TokenRequest {
  public final String grantType;
  public final String authorizationCode;
  public final String redirectUri;
  public final String clientId;
  public final String codeVerifier;

  public TokenRequest(
      @NonNull String grantType,
      @NonNull String authorizationCode,
      @NonNull String redirectUri,
      @NonNull String clientId,
      @NonNull String codeVerifier
  ) {
    this.grantType = grantType;
    this.authorizationCode = authorizationCode;
    this.redirectUri = redirectUri;
    this.clientId = clientId;
    this.codeVerifier = codeVerifier;
  }
}
