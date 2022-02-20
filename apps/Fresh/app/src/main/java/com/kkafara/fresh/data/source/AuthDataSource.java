package com.kkafara.fresh.data.source;

import com.kkafara.fresh.database.DatabaseInstanceProvider;
import com.kkafara.fresh.database.MainDatabase;

public class AuthDataSource {
  private static volatile AuthDataSource INSTANCE;

  private MainDatabase mDatabase;

  private AuthDataSource() {
    // we assume database has been already created...
    mDatabase = DatabaseInstanceProvider.getInstance(null);
  }

  public static synchronized AuthDataSource getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new AuthDataSource();
    }
    return INSTANCE;
  }
}
