package com.dp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.ViewModelProvider;

import com.dp.data.viewmodels.AuthorizationViewModel;
import com.dp.data.viewmodels.AuthorizationViewModelFactory;

public class AuthorizationActivity extends AppCompatActivity {
  private AuthorizationViewModel mAuthViewModel = new ViewModelProvider(
      this, new AuthorizationViewModelFactory()).get(AuthorizationViewModel.class);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Uri authenticationUri = mAuthViewModel.getAuthorizationEndpointUri();

    CustomTabsIntent.Builder CTIBuilder = new CustomTabsIntent.Builder();
    CustomTabsIntent intent = CTIBuilder.build();
    intent.launchUrl(this, authenticationUri);
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
