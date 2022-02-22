package com.kkafara.fresh.data.source.request;

import androidx.annotation.NonNull;

import com.kkafara.fresh.net.HttpContentTypes;
import com.kkafara.fresh.net.HttpUriRequestBaseBuilder;
import com.kkafara.fresh.net.OAuthHttpUriRequestBaseFactory;
import com.kkafara.fresh.servers.ResourceServerInfo;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.Method;

public class HttpDataRequestFactory implements OAuthHttpUriRequestBaseFactory {
  public final String TAG = "HttpDataRequestFactory";

  private final String REQUESTED_DATA_HEADER = "Requested-Data";

  private final String mToken;

  private final String mScopes;

  public HttpDataRequestFactory(
      @NonNull String token,
      @NonNull String scopes
  ) {
    mToken = token;
    mScopes = scopes;
  }

  @Override
  public HttpUriRequestBase create() {
    HttpUriRequestBaseBuilder builder = new HttpUriRequestBaseBuilder(
        Method.GET,
        ResourceServerInfo.HTTP_ADDRESS
    );
    return builder
        .setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + mToken)
        .setHeader(HttpHeaders.CONTENT_TYPE, HttpContentTypes.APPLICATION_X_WWW_FORM_URLENCODED)
        .setHeader(REQUESTED_DATA_HEADER, mScopes)
        .build();
  }
}
