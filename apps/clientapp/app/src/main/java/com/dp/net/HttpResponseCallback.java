package com.dp.net;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

public interface HttpResponseCallback {
  void invoke(CloseableHttpResponse response);
}
