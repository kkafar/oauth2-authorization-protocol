package com.dp.database.entity;

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

  @ColumnInfo(name = "token_type")
  public String tokenType;
}
