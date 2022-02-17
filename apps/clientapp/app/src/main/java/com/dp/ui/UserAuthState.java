package com.dp.ui;

import androidx.annotation.NonNull;

public class UserAuthState {
  @NonNull
  private Boolean mIsLoggedIn;

  public UserAuthState(@NonNull Boolean isLoggedIn) {
    mIsLoggedIn = isLoggedIn;
  }

  @NonNull
  public Boolean isLoggedIn() {
    return mIsLoggedIn;
  }
}
