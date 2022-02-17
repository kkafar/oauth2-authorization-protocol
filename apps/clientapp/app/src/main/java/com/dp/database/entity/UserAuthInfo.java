package com.dp.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "UserAuthInfo")
public class UserAuthInfo {
  @PrimaryKey
  public int uid;

  @ColumnInfo(name = "auth_code")
  public String authCode;

  @ColumnInfo(name = "code_verifier")
  public String codeVerifier;

  @ColumnInfo(name = "token")
  public String token;

  @ColumnInfo(name = "refresh_token")
  public String refreshToken;

  @ColumnInfo(name = "token_type")
  public String tokenType;

  @ColumnInfo(name = "token_expires_in")
  public long tokenExpiresIn;

  @ColumnInfo(name = "acquire_time")
  public long acquireTime;

  public UserAuthInfo(
      @NonNull int uid,
      @Nullable String authCode,
      @Nullable String codeVerifier,
      @Nullable String token,
      @Nullable String refreshToken,
      @Nullable String tokenType,
      long tokenExpiresIn,
      long acquireTime
  ) {
    this.uid = uid;
    this.authCode = authCode;
    this.codeVerifier = codeVerifier;
    this.token = token;
    this.refreshToken = refreshToken;
    this.tokenType = tokenType;
    this.tokenExpiresIn = tokenExpiresIn;
    this.acquireTime = acquireTime;
  }
}
