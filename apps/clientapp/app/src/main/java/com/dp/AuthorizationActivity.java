package com.dp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.ViewModelProvider;

import com.dp.data.model.AuthorizationRequest;
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

    mBinding = ActivityAuthorizationBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    mAuthViewModel = new ViewModelProvider(
      this, new AuthorizationViewModelFactory()).get(AuthorizationViewModel.class);

    AuthorizationRequest authorizationRequest = mAuthViewModel.getAuthorizationRequest(getString(R.string.client_id));

    Log.d(TAG, "Authorization request:" + authorizationRequest.toUri().toString());

    CustomTabsIntent.Builder CTIBuilder = new CustomTabsIntent.Builder();
    CustomTabsIntent intent = CTIBuilder.build();
    Bundle headers = new Bundle();
    headers.putString("content-type", "application/x-www-form-urlencoded");
    intent.intent.putExtra(Browser.EXTRA_HEADERS, headers);
    intent.launchUrl(this, authorizationRequest.toUri());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    String code = intent.getData().getQueryParameter("code");
    if (code != null) {
      mAuthViewModel.handleAuthorizationCode(code);
    } else {
      // TODO: handle error
    }
  }
}
