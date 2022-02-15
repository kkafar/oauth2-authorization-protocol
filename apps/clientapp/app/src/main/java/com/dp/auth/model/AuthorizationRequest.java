package com.dp.auth.model;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.dp.auth.AuthorizationServerEndpointName;
import com.dp.auth.OAuthHttpsQueryParameter;
import com.dp.auth.pkce.CodeChallengeMethod;

import java.util.Set;

public final class AuthorizationRequest {
  public final String mAuthServerAuthority;
  public final String mRedirectUri;
  public final String mClientId;
  public final String mCodeChallenge;
  public final String mCodeChallengeMethod;
  public final String mResponseType;
  public final String mState;
  public final Set<String> mScopesSet;

  public AuthorizationRequest(
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
    mCodeChallengeMethod = codeChallengeMethod == null ? CodeChallengeMethod.PLAIN.toString() : codeChallengeMethod.toString();
  };

  public Uri toUri() {
    Uri.Builder builder = new Uri.Builder();

    builder.scheme("https")
        .authority(mAuthServerAuthority)
        .appendPath(AuthorizationServerEndpointName.AUTHORIZATION.toString())
        .appendQueryParameter(OAuthHttpsQueryParameter.RESPONSE_TYPE.toString(), mResponseType)
        .appendQueryParameter(OAuthHttpsQueryParameter.CLIENT_ID.toString(), mClientId)
        .appendQueryParameter(OAuthHttpsQueryParameter.CODE_CHALLENGE.toString(), mCodeChallenge);

    if (mCodeChallengeMethod != null) {
      builder.appendQueryParameter(OAuthHttpsQueryParameter.CODE_CHALLENGE_METHOD.toString(), mCodeChallengeMethod);
    }
    if (mRedirectUri != null) {
      builder.appendQueryParameter(OAuthHttpsQueryParameter.REDIRECT_URI.toString(), mRedirectUri);
    }
    if (mState != null) {
      builder.appendQueryParameter(OAuthHttpsQueryParameter.STATE.toString(), mState);
    }
    if (mScopesSet != null && !mScopesSet.isEmpty()) {
      StringBuilder scopeSetBuilder = new StringBuilder();
      mScopesSet.forEach(scope -> {
        scopeSetBuilder.append(scope).append(" ");
      });
      scopeSetBuilder.deleteCharAt(scopeSetBuilder.length() - 1);
      builder.appendQueryParameter(OAuthHttpsQueryParameter.SCOPE.toString(), scopeSetBuilder.toString());
    }
    return builder.build();
  }
}
