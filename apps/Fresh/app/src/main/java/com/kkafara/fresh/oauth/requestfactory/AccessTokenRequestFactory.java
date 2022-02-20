package com.kkafara.fresh.oauth.requestfactory;

import androidx.annotation.NonNull;

import com.kkafara.fresh.net.HttpUriRequestBaseBuilder;
import com.kkafara.fresh.net.OAuthHttpUriRequestBaseFactory;
import com.kkafara.fresh.oauth.data.model.AuthCodeResponse;
import com.kkafara.fresh.oauth.util.OAuthHttpParameter;
import com.kkafara.fresh.servers.AuthServerInfo;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.Method;

public class AccessTokenRequestFactory implements OAuthHttpUriRequestBaseFactory {
  private final AuthCodeResponse mAuthCodeResponse;
  private final String mClientId;
  private final String mCodeVerifier;

  public AccessTokenRequestFactory(
      @NonNull AuthCodeResponse authCodeResponse,
      @NonNull String clientId,
      @NonNull String codeVerifier
  ) {
    mAuthCodeResponse = authCodeResponse;
    mClientId = clientId;
    mCodeVerifier = codeVerifier;
  }

  @Override
  public HttpUriRequestBase create() {
    HttpUriRequestBaseBuilder builder = new HttpUriRequestBaseBuilder(
        Method.POST,
        AuthServerInfo.ENDPOINT_ADDRESS_TOKEN
    );
    return builder
        .addParam(OAuthHttpParameter.GRANT_TYPE, "authorization_code")
        .addParam(OAuthHttpParameter.CODE, mAuthCodeResponse.code)
        .addParam(OAuthHttpParameter.REDIRECT_URI, "fresh://main")
        .addParam(OAuthHttpParameter.CLIENT_ID, mClientId)
        .addParam(OAuthHttpParameter.CODE_VERIFIER, mCodeVerifier)
        .build();
  }
}
