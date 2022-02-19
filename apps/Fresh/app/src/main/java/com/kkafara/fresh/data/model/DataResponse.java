package com.kkafara.fresh.data.model;

import androidx.annotation.Nullable;

public class DataResponse {
  @Nullable
  private String username;

  @Nullable
  private String mail;

  @Nullable
  private String nick;

  @Nullable
  private String error;

  public DataResponse(
      @Nullable String username,
      @Nullable String mail,
      @Nullable String nick,
      @Nullable String error
  ) {
    this.username = username;
    this.mail = mail;
    this.nick = nick;
    this.error = error;
  }

  @Nullable
  public String getUsername() {
    return username;
  }

  @Nullable
  public String getMail() {
    return mail;
  }

  @Nullable
  public String getNick() {
    return nick;
  }

  @Nullable
  public String getError() {
    return error;
  }

  public boolean isError() {
    return error != null;
  }
}
