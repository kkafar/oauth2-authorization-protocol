package com.dp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dp.auth.model.TokenResponse;

public class UserAuthState {
  @NonNull
  private Boolean mIsLoggedIn;

  @Nullable
  private String mToken;

  @Nullable
  private int mExpiresIn;

  @Nullable String mTokenType;



  public UserAuthState(@NonNull Boolean isLoggedIn) {
    mIsLoggedIn = isLoggedIn;
    mToken = null;
    mExpiresIn = -1;
    mTokenType = null;
  }

  public UserAuthState(@NonNull Boolean isLoggedIn, @NonNull TokenResponse tokenResponse) {
    mIsLoggedIn = isLoggedIn;
    mToken = tokenResponse.getAccessToken();
    mExpiresIn = tokenResponse.getExpireTime();
    mTokenType = tokenResponse.getTokenType();
  }

  @NonNull
  public Boolean isLoggedIn() {
    return mIsLoggedIn;
  }
}
