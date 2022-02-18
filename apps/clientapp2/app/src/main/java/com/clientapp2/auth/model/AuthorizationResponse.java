package com.clientapp2.auth.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class AuthorizationResponse {
  @Nullable
  public final String mState;

  @Nullable
  public final String mCode;

  public AuthorizationResponse(@Nullable String state, @Nullable String code) {
    mState = state;
    mCode = code;
  }

  @NonNull
  public static AuthorizationResponse fromUri(@NonNull Uri uri) {
    return new AuthorizationResponse(
        uri.getQueryParameter("state"),
        uri.getQueryParameter("code"));
  }
}
