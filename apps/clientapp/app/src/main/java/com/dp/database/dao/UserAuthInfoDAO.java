package com.dp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.dp.database.entity.UserAuthInfo;

import java.util.List;

@Dao
public interface UserAuthInfoDAO {
  @Query("SELECT * FROM UserAuthInfo")
  List<UserAuthInfo> getAll();

  @Query("SELECT * FROM UserAuthInfo WHERE uid == :id")
  UserAuthInfo findById(int id);

  @Insert
  void insertUserAuthInfo(UserAuthInfo userAuthInfo);

  @Delete
  void deleteUserAuthInfo(UserAuthInfo userAuthInfo);



}
