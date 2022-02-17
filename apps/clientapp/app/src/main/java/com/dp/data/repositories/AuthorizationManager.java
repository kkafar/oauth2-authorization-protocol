package com.dp.data.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dp.auth.model.TokenResponse;
import com.dp.auth.pkce.CodeChallengeMethod;
import com.dp.auth.pkce.CodeChallengeProvider;
import com.dp.auth.pkce.CodeVerifierProvider;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.pkce.StateProvider;

import java.security.NoSuchAlgorithmException;
import java.util.Set;

public final class AuthorizationManager {
  private final String TAG = "AuthorizationFlowRepository";

  private static volatile AuthorizationManager instance;

  @Nullable
  private AuthorizationRequest mAuthorizationRequest = null;

  @Nullable
  private TokenResponse mTokenResponse = null;

  @Nullable
  private String mCodeVerifier = null;

  private AuthorizationManager() {
    Log.d(TAG, "CTOR");
  }

  public static AuthorizationManager getInstance() {
    if (instance == null) {
      instance = new AuthorizationManager();
    }
    return instance;
  }

  @Nullable
  public AuthorizationRequest getLatestAuthorizationRequest() {
    return mAuthorizationRequest;
  }

  @Nullable
  public String getLatestCodeVerifier() {
    return mCodeVerifier;
  }

  @Nullable
  public AuthorizationRequest consumeAuthorizationRequest() {
    AuthorizationRequest request = mAuthorizationRequest;
    mAuthorizationRequest = null;
    mCodeVerifier = null;
    return request;
  }

  public AuthorizationRequest createNewAuthorizationRequest(
      @NonNull String authServerAuthority,
      @NonNull String clientId,
      String redirectUri,
      Set<String> scopes
  ) {
    String responseType = "code";

    String codeVerifier = new CodeVerifierProvider().generateCodeVerifier();

    Log.d(TAG, "CODE VERIFER IN BASE64: " + codeVerifier);

    CodeChallengeProvider codeChallengeProvider = new CodeChallengeProvider();

    String codeChallenge = null;
    CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256;

    try {
      codeChallenge = codeChallengeProvider.generateCodeChallenge(
          codeChallengeMethod, codeVerifier);
    } catch (NoSuchAlgorithmException exception) {
      Log.w(TAG, exception.getMessage() + " Falling back to PLAIN method.");
    }

    if (codeChallenge == null) {
      codeChallengeMethod = CodeChallengeMethod.PLAIN;
      try {
        codeChallenge = codeChallengeProvider.generateCodeChallenge(
            codeChallengeMethod, codeVerifier);
      } catch (NoSuchAlgorithmException exception) {
        Log.wtf(TAG, exception.getMessage() + " This should not happen.");
      }
    }

    assert codeChallenge != null;

    String state = new StateProvider().generate();

    mAuthorizationRequest = new AuthorizationRequest(
        authServerAuthority,
        clientId,
        responseType,
        codeChallenge,
        codeChallengeMethod,
        redirectUri,
        state,
        scopes
    );
    mCodeVerifier = codeVerifier;

    return mAuthorizationRequest;
  }

  public void setTokenResponse(TokenResponse tokenResponse) {
    mTokenResponse = tokenResponse;
  }

  @Nullable
  public TokenResponse getLatestTokenResponse() {
    return mTokenResponse;
  }

  public String getLatestToken() {
    return mTokenResponse.getAccessToken();
  }
}
