package com.kkafara.fresh.oauth.data.model;

import androidx.annotation.Nullable;

public abstract class OAuthErrorResponse {
  @Nullable
  public final String error;

  public OAuthErrorResponse(@Nullable String error) {
    this.error = error;
  }

  public boolean isError() {
    return this.error != null;
  };

  public String getError() {
    if (this.error != null) {
      return this.error;
    } else {
      throw new IllegalStateException("Attempt to get error while there isn't one!");
    }
  }
}
