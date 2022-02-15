package com.dp.ui.init;

import androidx.annotation.NonNull;

public class UserState {
  @NonNull
  private Boolean mIsLoggedIn;

  public UserState(@NonNull Boolean isLoggedIn) {
    mIsLoggedIn = isLoggedIn;
  }

  @NonNull
  public Boolean isLoggedIn() {
    return mIsLoggedIn;
  }
}
