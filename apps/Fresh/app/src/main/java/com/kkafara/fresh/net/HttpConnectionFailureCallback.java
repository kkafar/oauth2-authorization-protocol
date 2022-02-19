package com.kkafara.fresh.net;

public interface HttpConnectionFailureCallback<T> {
  T invoke(Exception exception);
}
