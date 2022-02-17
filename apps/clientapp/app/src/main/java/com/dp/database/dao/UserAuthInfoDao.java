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

  @Query("SELECT * FROM UserAuthInfo WHERE uid == :id")
  UserAuthInfo findById(int id);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertUserAuthInfo(UserAuthInfo userAuthInfo);

  @Delete
  void deleteUserAuthInfo(UserAuthInfo userAuthInfo);

  @Update
  void updateUserAuthInfo(UserAuthInfo userAuthInfo);
}
