package com.kkafara.fresh.oauth.data.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kkafara.fresh.oauth.util.OAuthHttpParameter;

public class AuthCodeResponse extends OAuthErrorResponse {
  @Nullable
  public final String state;

  @Nullable
  public final String code;

  public AuthCodeResponse(
      @Nullable String state,
      @Nullable String code,
      @Nullable String error
  ) {
    super(error);
    this.state = state;
    this.code = code;
  }

  @NonNull
  public static AuthCodeResponse fromUri(@NonNull Uri uri) {
    return new AuthCodeResponse(
        uri.getQueryParameter(OAuthHttpParameter.STATE),
        uri.getQueryParameter(OAuthHttpParameter.CODE),
        uri.getQueryParameter("error"));
  }

  @Override
  public boolean isError() {
    return error != null || state == null || code == null;
  }
}
