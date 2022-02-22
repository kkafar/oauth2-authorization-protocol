package com.kkafara.fresh.data.model;

import androidx.annotation.NonNull;

public class LoginState {
  @NonNull
  private final boolean loggedIn;

  public LoginState(@NonNull boolean loggedIn) {
    this.loggedIn = loggedIn;
  }

  @NonNull
  public boolean isLoggedIn() {
    return loggedIn;
  }
}
