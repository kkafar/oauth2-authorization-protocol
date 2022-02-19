package com.kkafara.fresh.oauth.requestfactory;

import androidx.annotation.NonNull;

import com.kkafara.fresh.net.HttpContentTypes;
import com.kkafara.fresh.net.HttpUriRequestBaseBuilder;
import com.kkafara.fresh.net.OAuthHttpUriRequestBaseFactory;
import com.kkafara.fresh.oauth.util.OAuthHttpRequestParameter;
import com.kkafara.fresh.servers.AuthServerInfo;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.Method;

public class RefreshTokenRequestFactory implements OAuthHttpUriRequestBaseFactory {
  private final String mRefreshToken;

  public RefreshTokenRequestFactory(
      @NonNull String refreshToken
  ) {
    mRefreshToken = refreshToken;
  }

  @Override
  public HttpUriRequestBase create() {
    HttpUriRequestBaseBuilder builder = new HttpUriRequestBaseBuilder(
        Method.POST,
        AuthServerInfo.ENDPOINT_ADDRESS_TOKEN
    );
    return builder
        .setHeader(HttpHeaders.CONTENT_TYPE, HttpContentTypes.APPLICATION_X_WWW_FORM_URLENCODED)
        .addParam(OAuthHttpRequestParameter.GRANT_TYPE, "refresh_token")
        .addParam(OAuthHttpRequestParameter.REFRESH_TOKEN, mRefreshToken)
        .build();
  }
}
