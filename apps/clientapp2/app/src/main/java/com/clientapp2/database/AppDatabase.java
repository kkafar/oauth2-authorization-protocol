package com.clientapp2.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.clientapp2.database.dao.UserAuthInfoDao;
import com.clientapp2.database.entity.UserAuthInfo;

@Database(entities = {UserAuthInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
  public abstract UserAuthInfoDao userAuthInfoDao();
}
