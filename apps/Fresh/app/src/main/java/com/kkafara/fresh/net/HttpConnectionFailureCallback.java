package com.kkafara.fresh.net;

public interface HttpConnectionFailureCallback {
  void invoke(Exception exception);
}
