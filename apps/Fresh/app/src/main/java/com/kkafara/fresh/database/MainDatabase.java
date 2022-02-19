package com.kkafara.fresh.database;

import androidx.room.Database;

import com.kkafara.fresh.database.dao.AuthInfoDao;
import com.kkafara.fresh.database.entity.AuthInfoRecord;

@Database(entities = {AuthInfoRecord.class}, version = 1, exportSchema = true)
public abstract class MainDatabase {
  public abstract AuthInfoDao getAuthInfoDao();
}
