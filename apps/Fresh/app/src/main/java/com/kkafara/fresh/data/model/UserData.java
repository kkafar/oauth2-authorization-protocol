package com.kkafara.fresh.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Optional;

public class UserData {
  @Nullable
  protected final String username;

  @Nullable
  protected final String mail;

  @Nullable
  protected final String nick;

  public UserData(
      @Nullable String username,
      @Nullable String mail,
      @Nullable String nick
  ) {
    this.username = username;
    this.mail = mail;
    this.nick = nick;
  }

  public Optional<String> getUsername() { return Optional.ofNullable(username); }

  public Optional<String> getMail() { return Optional.ofNullable(mail); }

  public Optional<String> getNick() { return Optional.ofNullable(nick); }

  public static UserData fromDataResponse(@NonNull DataResponse dataResponse) {
    return new UserData(dataResponse.getUsername(),
        dataResponse.getMail(),
        dataResponse.getNick());
  }
}
