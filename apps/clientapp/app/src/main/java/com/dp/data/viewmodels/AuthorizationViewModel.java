package com.dp.data.viewmodels;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dp.R;
import com.dp.auth.AuthStatus;
import com.dp.auth.AuthorizationResponseError;
import com.dp.auth.AuthorizationServerEndpointName;
import com.dp.auth.exceptions.InvalidAuthorizationResponseException;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.model.AuthorizationResponse;
import com.dp.auth.model.OperationStatus;
import com.dp.auth.model.TokenRequest;
import com.dp.auth.model.TokenResponse;
import com.dp.data.repositories.AuthorizationManager;
import com.dp.data.repositories.AuthorizationServerRepository;
import com.dp.database.AppDatabase;
import com.dp.database.DatabaseProvider;
import com.dp.database.dao.UserAuthInfoDao;
import com.dp.database.entity.UserAuthInfo;
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
import java.util.concurrent.ExecutionException;

public class AuthorizationViewModel extends ViewModel {
  public final String TAG = "AuthenticationViewModel";

  private MutableLiveData<UserAuthState> mUserState = new MutableLiveData<>();

  private final AuthorizationServerRepository mAuthServerRepository;
  private final AuthorizationManager mAuthorizationManager;
  private final Gson mGson;
  private final AppDatabase mDatabase;

  public AuthorizationViewModel(
      AuthorizationServerRepository authorizationServerRepository,
      AuthorizationManager authorizationManager
  ) {
    mAuthServerRepository = authorizationServerRepository;
    mAuthorizationManager = authorizationManager;
    mGson = new Gson();
    mDatabase = DatabaseProvider.getInstance(null);
  }


  public AuthorizationRequest createNewAuthorizationRequest(@NonNull String clientId, @NonNull String[] requiredScopes) {
    Set<String> scopes = new HashSet<>();
    Collections.addAll(scopes, requiredScopes);
    return mAuthorizationManager.createNewAuthorizationRequest(
        mAuthServerRepository.getAuthServerAuthority(),
        clientId,
        "auth://callback",
        scopes
    );
  }

//  public TokenRequest createNewTokenRequest(AuthorizationResponse serverResponse) {
//    AuthorizationRequest baseAuthorizationRequest = getLatestAuthorizationRequest();
//    if (baseAuthorizationRequest == null) {
//      throw new IllegalStateException("TODO");
//    }
//
//    return new TokenRequest(
//        "authorization_code",
//        serverResponse.mCode,
//        baseAuthorizationRequest.mRedirectUri,
//        baseAuthorizationRequest.mClientId,
//        getLatestCodeVerifier()
//    );
//  }

//  @Nullable
//  public AuthorizationRequest getLatestAuthorizationRequest() {
//    return mAuthorizationManager.getLatestAuthorizationRequest();
//  }
//
//  @Nullable
//  public String getLatestCodeVerifier() {
//    return mAuthorizationManager.getLatestCodeVerifier();
//  }

  public void validateAuthorizationResponse(AuthorizationResponse authorizationResponse)
      throws InvalidAuthorizationResponseException {
    mAuthorizationManager.validateAuthorizationResponse(authorizationResponse);
  }

  public OperationStatus acquireAccessToken(Context appContext, AuthorizationResponse response) {
    Log.d(TAG, "Sending Token request");
    return mAuthorizationManager.acquireAccessToken(appContext, response);
  }

  public AuthStatus authorize(Context appContext) throws ExecutionException, InterruptedException {
    Log.d(TAG, "authorize");
    return mAuthorizationManager.authorize(appContext);
  }


  private boolean userHasValidToken(@NonNull UserAuthInfo userAuthInfo) {
    return mAuthorizationManager.hasValidAccessToken(userAuthInfo);
  }

  private boolean userHasRefreshToken(UserAuthInfo userAuthInfo) {
    return mAuthorizationManager.hasRefreshToken(userAuthInfo);
  }
}
