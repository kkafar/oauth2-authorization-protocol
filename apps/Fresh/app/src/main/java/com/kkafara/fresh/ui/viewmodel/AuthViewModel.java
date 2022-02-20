package com.kkafara.fresh.ui.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kkafara.fresh.R;
import com.kkafara.fresh.data.model.LoginState;
import com.kkafara.fresh.data.model.Result;
import com.kkafara.fresh.data.repository.AuthRepository;
import com.kkafara.fresh.data.util.DataScopeParser;
import com.kkafara.fresh.net.HttpContentTypes;
import com.kkafara.fresh.oauth.data.model.AuthCodeRequest;
import com.kkafara.fresh.oauth.data.model.AuthCodeResponse;

import org.apache.hc.core5.http.HttpHeaders;

import java.util.concurrent.ExecutionException;

public class AuthViewModel extends ViewModel {
  public final String TAG = "AuthViewModel";
//  private MutableLiveData<Result<LoginState, Throwable>> mLoginStateLiveData =
//      new MutableLiveData<>();

  private AuthRepository mAuthRepository;
  private LifecycleOwner mOwner;

  public AuthViewModel(LifecycleOwner owner, AuthRepository authRepository) {
    mOwner = owner;
    mAuthRepository = authRepository;

  }

  public LiveData<Result<LoginState, Throwable>> getLoginStateLiveData() {
    return mAuthRepository.getLoginStateLiveData();
  }

  public void assertUserIsLoggedIn() {
    mAuthRepository.checkIfUserLoggedInAsync();
  }

  public void startAuthorizationCodeFlow(Context appContext, Iterable<String> scopes) {
    Log.d(TAG, "startAuthorizationCodeFlow");

    AuthCodeRequest request = mAuthRepository.createNewAuthorizationCodeRequest(
        appContext.getString(R.string.client_id),
        DataScopeParser.setStringFromStringIterable(scopes)
    );

    Log.d(TAG, request.toUri().toString());
    delegateAuthorizationRequestToCustomTabs(appContext, request.toUri());
  }

  private void delegateAuthorizationRequestToCustomTabs(Context appContext, Uri request) {
    CustomTabsIntent.Builder customTabsIntentBuilder = new CustomTabsIntent.Builder();
    CustomTabsIntent intent = customTabsIntentBuilder.build();
    Bundle headers = new Bundle();
    headers.putString(HttpHeaders.CONTENT_TYPE, HttpContentTypes.APPLICATION_X_WWW_FORM_URLENCODED);
    intent.intent.putExtra(Browser.EXTRA_HEADERS, headers);
    Log.d(TAG, "Launching custom tabs");
    intent.launchUrl(appContext, request);
  }

  public void getAccessTokenByAuthCode(Context appContext, AuthCodeResponse response) {
    Log.d(TAG, "getAccessTokenByAuthCode");
    try {
      mAuthRepository.getAccessTokenByAuthCode(
          appContext.getString(R.string.client_id),
          response
      );
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void logout() {
    mAuthRepository.tokenRevocationFlow();
  }
}
