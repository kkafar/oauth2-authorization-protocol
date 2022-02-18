package com.clientapp3.database.entity;

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

  @NonNull
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("UserAuthInfo:\n")
        .append("uid: ").append(uid).append('\n')
        .append("authCode: ").append(authCode).append('\n')
        .append("codeVerifier: ").append(codeVerifier).append('\n')
        .append("token: ").append(token).append('\n')
        .append("refreshToken: ").append(refreshToken).append('\n')
        .append("tokenType: ").append(tokenType).append('\n')
        .append("tokenExpiresIn: ").append(Long.toString(tokenExpiresIn)).append('\n')
        .append("acquireTime: ").append(Long.toString(acquireTime));
    return builder.toString();
  }
}
