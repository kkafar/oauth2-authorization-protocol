package com.dp.ui.userdata;

import androidx.annotation.Nullable;

public class UserDataState {
  @Nullable
  private final String username;

  @Nullable
  private final String mail;

  @Nullable final String nick;

  public UserDataState(@Nullable String name, @Nullable String email, @Nullable String nick) {
    this.username = name;
    this.mail = email;
    this.nick = nick;
  }

  public String getName() {
    return username;
  }

  public String getEmail() {
    return mail;
  }

  public String getNick() {
    return nick;
  }

  public boolean hasName() {
    return username != null;
  }

  public boolean hasEmail() {
    return mail != null;
  }

  public boolean hasNick() {
    return nick != null;
  }
}
