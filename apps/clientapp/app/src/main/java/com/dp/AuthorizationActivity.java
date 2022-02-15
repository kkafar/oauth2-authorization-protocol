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
import com.dp.data.viewmodels.AuthorizationViewModel;
import com.dp.data.viewmodels.AuthorizationViewModelFactory;
import com.dp.databinding.ActivityAuthorizationBinding;

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

    CustomTabsIntent.Builder customTabsIntentBuilder = new CustomTabsIntent.Builder();
    CustomTabsIntent intent = customTabsIntentBuilder.build();
    Bundle headers = new Bundle();
    headers.putString("content-type", "application/x-www-form-urlencoded");
    intent.intent.putExtra(Browser.EXTRA_HEADERS, headers);
    Log.d(TAG, "Launching custom tabs");
    intent.launchUrl(this, authorizationRequestUri);
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
    }

    Log.d(TAG, "Whole server response: " + intentData);
    Log.d(TAG, "Authorization code grant granted by server: " + response.mCode);
  }
}
