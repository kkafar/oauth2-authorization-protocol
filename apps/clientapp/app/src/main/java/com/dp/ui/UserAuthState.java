package com.dp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserAuthState {
  @NonNull
  private Boolean mIsLoggedIn;

  @Nullable
  private String mContext;

  public UserAuthState(@NonNull Boolean isLoggedIn) {
    mIsLoggedIn = isLoggedIn;
  }

  public UserAuthState(@NonNull Boolean isLoggedIn, @Nullable String context) {
    mIsLoggedIn = isLoggedIn;
    mContext = context;
  }

  @NonNull
  public Boolean isLoggedIn() {
    return mIsLoggedIn;
  }

  @Nullable public String getContext() {
    return mContext;
  }
}
