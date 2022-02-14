package com.dp.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.dp.R;
import com.dp.data.model.AuthorizationRequest;
import com.dp.data.repositories.AuthorizationFlowRepository;
import com.dp.data.repositories.AuthorizationServerRepository;

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
    return mAuthFlowRepository.getAuthorizationRequest(
        mAuthServerRepository.getAuthServerAuthority(),
        clientId,
        null,
        null
    );
  }
}
