package com.kkafara.fresh.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.kkafara.fresh.database.dao.AuthInfoDao;
import com.kkafara.fresh.database.entity.AuthInfoRecord;

@Database(entities = {AuthInfoRecord.class}, version = 1, exportSchema = false)
public abstract class MainDatabase extends RoomDatabase {
  public abstract AuthInfoDao getAuthInfoDao();
}
