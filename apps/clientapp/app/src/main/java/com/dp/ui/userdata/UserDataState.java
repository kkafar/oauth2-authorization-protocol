package com.dp.ui.userdata;

import androidx.annotation.Nullable;

public class UserDataState {
  @Nullable
  private final String username;

  @Nullable
  private final String mail;

  public UserDataState(@Nullable String name, @Nullable String email) {
    username = name;
    mail = email;
  }

  public String getName() {
    return username;
  }

  public String getEmail() {
    return mail;
  }

  public boolean hasName() {
    return username != null;
  }

  public boolean hasEmail() {
    return mail != null;
  }
}
