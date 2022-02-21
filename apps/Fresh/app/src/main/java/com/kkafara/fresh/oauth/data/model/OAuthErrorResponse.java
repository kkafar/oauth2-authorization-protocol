package com.kkafara.fresh.oauth.data.model;

import androidx.annotation.NonNull;
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

  @NonNull
  public String getError() {
    if (this.error != null) {
      return this.error;
    } else {
      throw new IllegalStateException("Attempt to get error while there isn't one!");
    }
  }

  public final boolean hasError() {
    return error != null;
  }
}
