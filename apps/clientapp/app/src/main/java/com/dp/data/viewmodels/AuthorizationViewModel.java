package com.dp.data.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dp.auth.AuthStatus;
import com.dp.auth.exceptions.InvalidAuthorizationResponseException;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.model.AuthorizationResponse;
import com.dp.auth.model.OperationStatus;
import com.dp.data.repositories.AuthorizationManager;
import com.dp.data.repositories.AuthorizationServerRepository;
import com.dp.database.AppDatabase;
import com.dp.database.DatabaseProvider;
import com.dp.database.entity.UserAuthInfo;
import com.dp.ui.UserAuthState;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashSet;
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

  public LiveData<UserAuthState> getUserAuthState() {
    return mUserState;
  }

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
    return mAuthorizationManager.authorize(appContext).get();
  }


  private boolean userHasValidToken(@NonNull UserAuthInfo userAuthInfo) {
    return mAuthorizationManager.hasValidAccessToken(userAuthInfo);
  }

  private boolean userHasRefreshToken(UserAuthInfo userAuthInfo) {
    return mAuthorizationManager.hasRefreshToken(userAuthInfo);
  }
}
