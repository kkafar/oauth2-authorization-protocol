package com.kkafara.fresh.oauth.data.model;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.kkafara.fresh.data.util.DataScopeParser;
import com.kkafara.fresh.oauth.pkce.CodeChallengeMethod;
import com.kkafara.fresh.oauth.util.OAuthHttpRequestParameter;
import com.kkafara.fresh.servers.AuthServerInfo;

import java.util.Set;

public class AuthCodeRequest {
  public final String mAuthServerAuthority;
  public final String mRedirectUri;
  public final String mClientId;
  public final String mCodeChallenge;
  public final String mCodeChallengeMethod;
  public final String mResponseType;
  public final String mState;
  public final Set<String> mScopesSet;

  public AuthCodeRequest(
      @NonNull String authServerAuthority,
      @NonNull String clientId,
      @NonNull String responseType,
      @NonNull String codeChallenge,
      CodeChallengeMethod codeChallengeMethod,
      String redirectUri,
      String state,
      Set<String> scopesSet
  ) {
    mAuthServerAuthority = authServerAuthority;
    mClientId = clientId;
    mResponseType = responseType;
    mRedirectUri = redirectUri;
    mState = state;
    mScopesSet = scopesSet;
    mCodeChallenge = codeChallenge;
    mCodeChallengeMethod = codeChallengeMethod == null ? CodeChallengeMethod.PLAIN : codeChallengeMethod.toString();
  };

  public Uri toUri() {
    Uri.Builder builder = new Uri.Builder();

    builder.scheme("https")
        .authority(mAuthServerAuthority)
        .appendPath(AuthServerInfo.ENDPOINT_NAME_AUTHORIZE)
        .appendQueryParameter(OAuthHttpRequestParameter.RESPONSE_TYPE, mResponseType)
        .appendQueryParameter(OAuthHttpRequestParameter.CLIENT_ID, mClientId)
        .appendQueryParameter(OAuthHttpRequestParameter.CODE_CHALLENGE, mCodeChallenge);

    if (mCodeChallengeMethod != null) {
      builder.appendQueryParameter(OAuthHttpRequestParameter.CODE_CHALLENGE_METHOD.toString(), mCodeChallengeMethod);
    }
    if (mRedirectUri != null) {
      builder.appendQueryParameter(OAuthHttpRequestParameter.REDIRECT_URI.toString(), mRedirectUri);
    }
    if (mState != null) {
      builder.appendQueryParameter(OAuthHttpRequestParameter.STATE.toString(), mState);
    }
    if (mScopesSet != null && !mScopesSet.isEmpty()) {
      builder.appendQueryParameter(OAuthHttpRequestParameter.SCOPE, DataScopeParser.stringFromStringIterable(mScopesSet));
    }
    return builder.build();
  }
}
