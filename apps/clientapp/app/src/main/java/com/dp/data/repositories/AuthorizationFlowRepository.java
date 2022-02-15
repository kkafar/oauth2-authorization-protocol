package com.dp.data.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dp.auth.pkce.CodeChallengeMethod;
import com.dp.auth.pkce.CodeChallengeProvider;
import com.dp.auth.pkce.CodeVerifierProvider;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.pkce.StateProvider;

import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class AuthorizationFlowRepository {
  private final String TAG = "AuthorizationFlowRepository";

  private static volatile AuthorizationFlowRepository instance;

  @Nullable
  private AuthorizationRequest mAuthorizationRequest = null;

  private AuthorizationFlowRepository() {}

  public static AuthorizationFlowRepository getInstance() {
    if (instance == null) {
      instance = new AuthorizationFlowRepository();
    }
    return instance;
  }

  @Nullable
  public AuthorizationRequest getLatestAuthorizationRequest() {
    return mAuthorizationRequest;
  }

  @Nullable
  public AuthorizationRequest consumeAuthorizationRequest() {
    AuthorizationRequest request = mAuthorizationRequest;
    mAuthorizationRequest = null;
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

    return mAuthorizationRequest;
  }
}
