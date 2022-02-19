package com.kkafara.fresh.net;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class HttpUriRequestBaseBuilder {
  private HttpUriRequestBase request = null;
  private List<NameValuePair> params = null;

  public HttpUriRequestBaseBuilder(Method httpMethod, String uri) {
    request = new HttpUriRequestBase(httpMethod.name(), URI.create(uri));
    params = new LinkedList<>();

  }

  public HttpUriRequestBaseBuilder setHeader(String headerName, String value) {
    request.setHeader(headerName, value);
    return this;
  }

  public HttpUriRequestBaseBuilder addParam(NameValuePair pair) {
    params.add(pair);
    return this;
  }

  public HttpUriRequestBaseBuilder addParam(String name, String value) {
    params.add(new BasicNameValuePair(name, value));
    return this;
  }

  public HttpUriRequestBase build() {
    if (!params.isEmpty()) {
      request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
    }
    return request;
  }
}
