package com.kkafara.fresh.database;

import android.content.Context;

import androidx.room.Room;

public final class DatabaseInstanceProvider {
  private static MainDatabase INSTANCE = null;

  public synchronized static MainDatabase getInstance(Context appContext) {
    if (INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(appContext.getApplicationContext(),
          MainDatabase.class,
          MainDatabaseInfo.NAME)
          .build();
    }
    return INSTANCE;
  }
}
