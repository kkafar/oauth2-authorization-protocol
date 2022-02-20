package com.kkafara.fresh.database.entity;

import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.ACCESS_TOKEN;
import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.AUTHORIZATION_CODE;
import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.CODE_VERIFIER;
import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.REFRESH_TOKEN;
import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.TOKEN_EXPIRE_TIME;
import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.TOKEN_GRANT_TIME;
import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.TOKEN_TYPE;
import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.USER_ID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AuthInfo")
public class AuthInfoRecord {
  @PrimaryKey
  @ColumnInfo(name = USER_ID)
  public int userId;

  @ColumnInfo(name = AUTHORIZATION_CODE)
  public String authorizationCode;

  @ColumnInfo(name = CODE_VERIFIER)
  public String codeVerifier;

  @ColumnInfo(name = ACCESS_TOKEN)
  public String accessToken;

  @ColumnInfo(name = REFRESH_TOKEN)
  public String refreshToken;

  @ColumnInfo(name = TOKEN_TYPE)
  public String tokenType;

  @ColumnInfo(name = TOKEN_EXPIRE_TIME)
  public long tokenExpireTime;

  @ColumnInfo(name = TOKEN_GRANT_TIME)
  public long tokenGrantTime;

  public AuthInfoRecord(
      int userId,
      @Nullable String authorizationCode,
      @Nullable String codeVerifier,
      @Nullable String accessToken,
      @Nullable String refreshToken,
      @Nullable String tokenType,
      long tokenExpireTime,
      long tokenGrantTime
  ) {
    this.userId = userId;
    this.authorizationCode = authorizationCode;
    this.codeVerifier = codeVerifier;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenType = tokenType;
    this.tokenExpireTime = tokenExpireTime;
    this.tokenGrantTime = tokenGrantTime;
  }

  @NonNull
  @Override
  public String toString() {
    return "AuthInfoRecord:" +
        "\nuserId: " + userId +
        "\nauthorizationCode: " + authorizationCode +
        "\ncodeVerifier: " + codeVerifier +
        "\naccessToken: " + accessToken +
        "\nrefreshToken: " + refreshToken +
        "\ntokenType: " + tokenType +
        "\ntokenExpireTime: " + tokenExpireTime +
        "\ntokenGrantTime: " + tokenGrantTime;
  }
}
