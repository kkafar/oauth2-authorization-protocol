package com.kkafara.fresh.data.model;

import androidx.annotation.NonNull;

public class Result<S, E> {
  private S mSuccessValue = null;
  private E mError = null;

  private Result(S successValue, E error) {
    mSuccessValue = successValue;
    mError = error;
  }

  public static <S, E> Result<S, E> newSuccess(S value) {
    return new Result<>(value, null);
  }

  public static <S, E> Result<S, E> newSuccess() {
    return new Result<>(null, null);
  }

  public static <S, E> Result<S, E> newError(E error) {
    return new Result<>(null, error);
  }

  public boolean isError() {
    return mError != null;
  }

  public boolean isSuccess() {
    return mError == null;
  }

  public E getError() {
    if (mError != null) {
      return mError;
    } else {
      throw new IllegalStateException("Attempt to get error from success result");
    }
  }

  public boolean hasSuccessValue() {
    return mSuccessValue != null;
  }

  public S getSuccessValue() {
    return mSuccessValue;
  }

  @NonNull
  @Override
  public String toString() {
    return "Result(value: " + mSuccessValue + ", error: " + mError + ")";
  }
}
