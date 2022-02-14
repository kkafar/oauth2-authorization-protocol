package com.dp.data.model;

import androidx.annotation.NonNull;

import com.dp.auth.pkce.CodeChallengeMethod;

import java.util.Set;

public class AuthorizationRequest {
  public final String mEndpoint;
  public final String mRedirectUri;
  public final String mClientId;
  public final String mCodeChallenge;
  public final String mCodeChallengeMethod;
  public final String mResponseType;
  public final String mState;
  public final Set<String> mScopesSet;

  private AuthorizationRequest(
      @NonNull String authServerEndpoint,
      @NonNull String clientId,
      @NonNull String responseType,
      @NonNull String codeChallenge,
      CodeChallengeMethod codeChallengeMethod,
      String redirectUri,
      String state,
      Set<String> scopesSet
  ) {
    mEndpoint = authServerEndpoint;
    mClientId = clientId;
    mResponseType = responseType;
    mRedirectUri = redirectUri;
    mState = state;
    mScopesSet = scopesSet;
    mCodeChallenge = codeChallenge;
    mCodeChallengeMethod = codeChallengeMethod == null ? CodeChallengeMethod.PLAIN.toString() : codeChallenge.toString();
  };
}
