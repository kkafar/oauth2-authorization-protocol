package com.dp.auth.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OperationStatus {
  @NonNull
  private final Boolean mSuccess;

  @Nullable
  private final String mErrorCause;

  public OperationStatus(@NonNull Boolean success, @Nullable String errorCause) {
    mSuccess = success;
    if (success == false) {
      assert errorCause != null;
    }
    mErrorCause = errorCause;
  }

  @NonNull
  public Boolean isSuccess() {
    return mSuccess;
  }

  @Nullable
  public String getCause() {
    return mErrorCause;
  }
}
