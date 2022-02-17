package com.dp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.ViewModelProvider;

import com.dp.auth.exceptions.InvalidAuthorizationResponseException;
import com.dp.auth.model.AuthorizationRequest;
import com.dp.auth.model.AuthorizationResponse;
import com.dp.auth.model.TokenRequest;
import com.dp.auth.model.TokenResponse;
import com.dp.data.viewmodels.AuthorizationViewModel;
import com.dp.data.viewmodels.AuthorizationViewModelFactory;
import com.dp.databinding.ActivityAuthorizationBinding;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

public class AuthorizationActivity extends AppCompatActivity {
  public final String TAG = "AuthorizationActivity";

  private AuthorizationViewModel mAuthViewModel;
  private ActivityAuthorizationBinding mBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "onCreate");

    mBinding = ActivityAuthorizationBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    mAuthViewModel = new ViewModelProvider(
        this,
        new AuthorizationViewModelFactory()).get(AuthorizationViewModel.class);

    AuthorizationRequest authorizationRequest = mAuthViewModel
        .createNewAuthorizationRequest(getString(R.string.client_id),
            getResources().getStringArray(R.array.auth_required_scopes));

    Uri authorizationRequestUri = authorizationRequest.toUri();

    Log.d(TAG, "Authorization request:" + authorizationRequestUri.toString());

    delegateAuthorizationRequestToCustomTabs(authorizationRequestUri);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Log.d(TAG, "onNewIntent");

    Uri intentData = intent.getData();
    AuthorizationResponse response = AuthorizationResponse.fromUri(intentData);

    try {
      mAuthViewModel.validateAuthorizationResponse(response);
    } catch (InvalidAuthorizationResponseException exception) {
      Log.e(TAG, exception.getMessage());
      exception.printStackTrace();
      setResult(RESULT_CANCELED, null);
      finish();
    }

    Log.d(TAG, "Whole server response: " + intentData);
    Log.d(TAG, "Authorization code grant granted by server: " + response.mCode);

    TokenResponse tokenResponse = mAuthViewModel.sendTokenRequest(response);
    Intent resultIntent = tokenResponse.toIntent();
    setResult(RESULT_OK, resultIntent);
    finish();

  }

  private void delegateAuthorizationRequestToCustomTabs(Uri request) {
    CustomTabsIntent.Builder customTabsIntentBuilder = new CustomTabsIntent.Builder();
    CustomTabsIntent intent = customTabsIntentBuilder.build();
    Bundle headers = new Bundle();
    headers.putString("content-type", "application/x-www-form-urlencoded");
    intent.intent.putExtra(Browser.EXTRA_HEADERS, headers);
    Log.d(TAG, "Launching custom tabs");
    intent.launchUrl(this, request);
  }
}
