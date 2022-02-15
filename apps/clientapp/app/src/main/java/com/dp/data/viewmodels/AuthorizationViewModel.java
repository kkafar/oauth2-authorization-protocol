package com.dp.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.dp.auth.AuthorizationRequest;
import com.dp.data.repositories.AuthorizationFlowRepository;
import com.dp.data.repositories.AuthorizationServerRepository;

import java.util.HashSet;
import java.util.Set;

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


  public AuthorizationRequest getAuthorizationRequest(@NonNull String clientId) {
    Set<String> scopes = new HashSet<>();
    scopes.add("all");
    return mAuthFlowRepository.getAuthorizationRequest(
        mAuthServerRepository.getAuthServerAuthority(),
        clientId,
        "auth://callback",
        scopes
    );
  }
}
