package com.clientapp2.net;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

public interface OAuthHttpUriRequestBaseFactory {
  HttpUriRequestBase create();
}
