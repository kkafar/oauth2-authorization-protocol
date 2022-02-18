package com.dp.ui.userdata;

import androidx.annotation.Nullable;

public class UserDataState {
  @Nullable
  private final String username;

  @Nullable
  private final String mail;

  @Nullable final String nick;

  @Nullable final String error;

  public UserDataState(
      @Nullable String name,
      @Nullable String email,
      @Nullable String nick,
      @Nullable String error) {
    this.username = name;
    this.mail = email;
    this.nick = nick;
    this.error = error;
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

  public String getError() {
    return error;
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    return builder
        .append("nick: ").append(nick).append('\n')
        .append("email: ").append(mail).append('\n')
        .append("name: ").append(username).append('\n')
        .append("error: ").append(error).append('\n').toString();
  }
}
