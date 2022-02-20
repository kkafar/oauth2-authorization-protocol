package com.kkafara.fresh.database.dao;

import static com.kkafara.fresh.database.entity.AuthInfoRecordColumns.ACCESS_TOKEN;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kkafara.fresh.database.entity.AuthInfoRecord;

import java.util.List;

@Dao
public interface AuthInfoDao {
  @Query("select * from AuthInfo")
  List<AuthInfoRecord> getAllRecords();

  @Query("select * from AuthInfo where userId == :uid")
  AuthInfoRecord findByUserId(int uid);

  @Query("select " + ACCESS_TOKEN + " from AuthInfo where userId == :uid")
  String findAccessTokenByUserId(int uid);

  @Query("select :columnName from AuthInfo where userId == :uid")
  String findStringDataByIdAndName(int uid, String columnName);

  @Query("select :columnName from AuthInfo where userId == :uid")
  long findLongDataByIdAndName(int uid, String columnName);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAuthInfoRecord(AuthInfoRecord authInfoRecord);

  @Delete
  void deleteAuthInfoRecord(AuthInfoRecord authInfoRecord);
}
