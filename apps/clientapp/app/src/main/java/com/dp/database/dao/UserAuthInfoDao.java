package com.dp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dp.database.entity.UserAuthInfo;

import java.util.List;

@Dao
public interface UserAuthInfoDao {
  @Query("SELECT * FROM UserAuthInfo")
  List<UserAuthInfo> getAll();

  @Query("SELECT * FROM UserAuthInfo WHERE uid == :uid")
  UserAuthInfo findById(int uid);

  @Query("SELECT auth_code FROM UserAuthInfo WHERE uid == :uid")
  String findAuthCode(int uid);

  @Query("SELECT code_verifier FROM UserAuthInfo WHERE uid == :uid")
  String findCodeVerifierById(int uid);

  @Query("SELECT token FROM UserAuthInfo WHERE uid == :uid")
  String findTokenByUid(int uid);

  @Query("SELECT refresh_token FROM UserAuthInfo WHERE uid == :uid")
  String findRefreshTokenByUid(int uid);

  @Query("SELECT token_type FROM UserAuthInfo WHERE uid == :uid")
  String findTokenTypeByUid(int uid);

  @Query("SELECT token_expires_in FROM UserAuthInfo WHERE uid == :uid")
  long findTokenExpiresInByUid(int uid);


  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertUserAuthInfo(UserAuthInfo userAuthInfo);

  @Delete
  void deleteUserAuthInfo(UserAuthInfo userAuthInfo);

  @Update
  void updateUserAuthInfo(UserAuthInfo userAuthInfo);
}
