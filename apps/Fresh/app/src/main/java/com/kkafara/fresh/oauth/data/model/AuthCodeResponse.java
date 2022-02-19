package com.kkafara.fresh.oauth.data.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kkafara.fresh.oauth.util.OAuthHttpRequestParameter;

public class AuthCodeResponse {
  @Nullable
  public final String state;

  @Nullable
  public final String code;

  public AuthCodeResponse(
      @Nullable String state,
      @Nullable String code
  ) {
    this.state = state;
    this.code = code;
  }

    @NonNull
  public static AuthCodeResponse fromUri(@NonNull Uri uri) {
    return new AuthCodeResponse(
        uri.getQueryParameter(OAuthHttpRequestParameter.STATE),
        uri.getQueryParameter(OAuthHttpRequestParameter.CODE));
  }
}
