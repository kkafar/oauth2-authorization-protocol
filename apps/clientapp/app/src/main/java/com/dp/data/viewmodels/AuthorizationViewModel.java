package com.dp.data.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dp.auth.AuthorizationResponseError;
import com.dp.auth.AuthorizationServerEndpointName;
import com.dp.auth.exceptions.InvalidAuthorizationResponseException;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.model.AuthorizationResponse;
import com.dp.auth.model.TokenRequest;
import com.dp.auth.model.TokenResponse;
import com.dp.data.repositories.AuthorizationManager;
import com.dp.data.repositories.AuthorizationServerRepository;
import com.dp.ui.UserAuthState;
import com.google.gson.Gson;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuthorizationViewModel extends ViewModel {
  public final String TAG = "AuthenticationViewModel";

  private MutableLiveData<UserAuthState> mUserState = new MutableLiveData<>();

  private final AuthorizationServerRepository mAuthServerRepository;
  private final AuthorizationManager mAuthFlowRepository;
  private final Gson gson;

  public AuthorizationViewModel(
      AuthorizationServerRepository authorizationServerRepository,
      AuthorizationManager authorizationManager
  ) {
    mAuthServerRepository = authorizationServerRepository;
    mAuthFlowRepository = authorizationManager;
    gson = new Gson();
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

  public TokenRequest createNewTokenRequest(AuthorizationResponse serverResponse) {
    AuthorizationRequest baseAuthorizationRequest = getLatestAuthorizationRequest();
    if (baseAuthorizationRequest == null) {
      throw new IllegalStateException("TODO");
    }

    return new TokenRequest(
        "authorization_code",
        serverResponse.mCode,
        baseAuthorizationRequest.mRedirectUri,
        baseAuthorizationRequest.mClientId,
        getLatestCodeVerifier()
    );
  }

  @Nullable
  public AuthorizationRequest getLatestAuthorizationRequest() {
    return mAuthFlowRepository.getLatestAuthorizationRequest();
  }

  @Nullable
  public String getLatestCodeVerifier() {
    return mAuthFlowRepository.getLatestCodeVerifier();
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

  public TokenResponse sendTokenRequest(AuthorizationResponse response) {
    Log.d(TAG, "Sending Token request");
    Thread connectionExecutor = new Thread(() -> {
      try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpPost httpPostRequest = new HttpPost(mAuthServerRepository
            .getAddressForEndpoint(AuthorizationServerEndpointName.TOKEN));

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        parameters.add(new BasicNameValuePair("code", response.mCode));
        parameters.add(new BasicNameValuePair("redirect_uri", mAuthFlowRepository.getLatestAuthorizationRequest().mRedirectUri));
        parameters.add(new BasicNameValuePair("client_id", mAuthFlowRepository.getLatestAuthorizationRequest().mClientId));
        parameters.add(new BasicNameValuePair("code_verifier", mAuthFlowRepository.getLatestCodeVerifier()));

        httpPostRequest.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPostRequest)) {
          Log.d(TAG, "AUTH SERVER RESPONSE FOR TOKEN REQUEST");
          Log.d(TAG, httpResponse.toString());
          Log.d(TAG, Arrays.toString(httpResponse.getHeaders()));
          Log.d(TAG, Long.toString(httpResponse.getEntity().getContentLength()));
          byte[] bytes = new byte[(int)(httpResponse.getEntity().getContentLength())];
          httpResponse.getEntity().getContent().read(bytes);
          Log.d(TAG, new String(bytes));
          TokenResponse tokenResponse = gson.fromJson(new String(bytes), TokenResponse.class);
          mAuthFlowRepository.setTokenResponse(tokenResponse);
        } catch (Exception exception) {
          if (exception.getMessage() != null) {
            Log.e(TAG, exception.getMessage());
          }
          exception.printStackTrace();
        }
      } catch (IOException exception) {
        if (exception.getMessage() != null) {
          Log.e(TAG, exception.getMessage());
        }
        exception.printStackTrace();
      }
    });

    connectionExecutor.start();
    try {
      connectionExecutor.join(5000);
    } catch (InterruptedException ignore) {
    }
    return mAuthFlowRepository.getLatestTokenResponse();
  }
}
