package com.dp.ui.userdata;

import androidx.annotation.Nullable;

public class UserDataState {
  @Nullable
  private final String mName;

  @Nullable
  private final String mEmail;

  public UserDataState(@Nullable String name, @Nullable String email) {
    mName = name;
    mEmail = email;
  }

  public String getName() {
    return mName;
  }

  public String getEmail() {
    return mEmail;
  }

  public boolean hasName() {
    return mName != null;
  }

  public boolean hasEmail() {
    return mEmail != null;
  }
}
