package com.dp.database;

import android.content.Context;

import androidx.room.Room;

public final class DatabaseProvider {
  private static volatile AppDatabase instance;

  private DatabaseProvider() {}

  public static AppDatabase getInstance(Context appContext) {
    if (instance == null) {
      instance = Room.databaseBuilder(appContext, AppDatabase.class, "user-info-database")
          .build();
    }
    return instance;
  }
}
