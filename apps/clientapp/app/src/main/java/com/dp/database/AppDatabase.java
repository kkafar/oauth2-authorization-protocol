package com.dp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.dp.database.entity.UserAuthInfo;

@Database(entities = {UserAuthInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
  public abstract UserAuthInfo userAuthInfo();
}
