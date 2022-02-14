package com.dp.data.viewmodels;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.dp.auth.pkce.CodeChallengeMethod;
import com.dp.auth.pkce.CodeChallengeProvider;
import com.dp.auth.pkce.CodeVerifierProvider;
import com.dp.data.repositories.AuthorizationFlowRepository;
import com.dp.data.repositories.AuthorizationServerRepository;

import java.security.NoSuchAlgorithmException;

public class AuthorizationViewModel extends ViewModel {
  public final String TAG = "AuthenticationViewModel";

  private AuthorizationServerRepository mAuthServerRepository;
  private AuthorizationFlowRepository mAuthFlowRepository;

  public AuthorizationViewModel(
      AuthorizationServerRepository authorizationServerRepository,
      AuthorizationFlowRepository authorizationFlowRepository
  ) {
    mAuthServerRepository = authorizationServerRepository;
    mAuthFlowRepository = authorizationFlowRepository;
  }


  public void handleAuthorizationCode(String authCode) {
    // TODO: redirect call to data repository to retrieve access token
  }


  public Uri getAuthorizationUri() {
    CodeChallengeProvider codeChallengeProvider = new CodeChallengeProvider();
    CodeVerifierProvider codeVerifierProvider = new CodeVerifierProvider();

    String codeVerifier = codeVerifierProvider.generateCodeVerifier();
    String codeChallenge = null;
    try {
      codeChallenge = codeChallengeProvider.generateCodeChallenge(CodeChallengeMethod.S256, codeVerifier);
    } catch (NoSuchAlgorithmException exception) {
      Log.w(TAG, exception.getMessage() + ". Falling back to PLAIN method.");
    }
    if (codeChallenge == null) {
      try {
        codeChallenge = codeChallengeProvider.generateCodeChallenge(CodeChallengeMethod.PLAIN, codeVerifier);
      } catch (NoSuchAlgorithmException exception) {
        Log.wtf(TAG, exception.getMessage() + ". This should not happen.");
      }
    }

    String authorizationEndpoint = mAuthServerRepository.getAuthorizationEndpoint();

    return Uri.parse(authorizationEndpoint);
  }

}
