package com.dp.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.dp.auth.AuthorizationResponseError;
import com.dp.auth.exceptions.InvalidAuthorizationResponseException;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.model.AuthorizationResponse;
import com.dp.data.repositories.AuthorizationFlowRepository;
import com.dp.data.repositories.AuthorizationServerRepository;

import java.util.Collections;
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


  public AuthorizationRequest createNewAuthorizationRequest(@NonNull String clientId, @NonNull String[] requiredScopes) {
    Set<String> scopes = new HashSet<>();
    Collections.addAll(scopes, requiredScopes);
    return mAuthFlowRepository.createNewAuthorizationRequest(
        mAuthServerRepository.getAuthServerAuthority(),
        clientId,
        "auth://callback",
        scopes
    );
  }

  @Nullable
  public AuthorizationRequest getLatestAuthorizationRequest() {
    return mAuthFlowRepository.getLatestAuthorizationRequest();
  }

  public void validateAuthorizationResponse(AuthorizationResponse authorizationResponse)
      throws InvalidAuthorizationResponseException {

    boolean invalid = false;
    StringBuilder errorMessageBuilder = new StringBuilder();

    if (authorizationResponse == null) {
      throw new InvalidAuthorizationResponseException("Null response");
    }

    AuthorizationRequest request = mAuthFlowRepository.getLatestAuthorizationRequest();
    if (request == null) {
      throw new IllegalStateException("Authorization server response validation method called w/o " +
          "request in AuthorizationFlowRepository instance");
    }

    if (authorizationResponse.mCode == null) {
      invalid = true;
      errorMessageBuilder.append(AuthorizationResponseError.NO_CODE_GRANT).append('\n');
    }
    if (authorizationResponse.mState == null) {
      invalid = true;
      errorMessageBuilder.append(AuthorizationResponseError.NO_STATE).append('\n');
    }
    assert authorizationResponse.mState != null;
    if (!authorizationResponse.mState.equals(request.mState)) {
      invalid = true;
      errorMessageBuilder.append(AuthorizationResponseError.BAD_STATE).append('\n');
    }
    if (invalid) {
      throw new InvalidAuthorizationResponseException(errorMessageBuilder.toString());
    }
  }
}
