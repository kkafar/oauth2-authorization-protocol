package com.dp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dp.auth.AuthStatus;
import com.dp.auth.exceptions.InvalidAuthorizationResponseException;
import com.dp.auth.model.AuthorizationResponse;
import com.dp.auth.model.TokenResponse;
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


    Thread executor = new Thread(() -> {
      AuthStatus result =  mAuthViewModel.authorize(this);
      if (result == AuthStatus.COMPLETED_OK) {
        setResult(RESULT_OK);
        finish();
      } else if (result == AuthStatus.TOKEN_REQUEST_REQUIRED) {

      }
    });
    executor.start();
    try {
      executor.join();
    } catch (Exception ignore) {}

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

    TokenResponse tokenResponse = mAuthViewModel.acquireAccessToken(response);
    Intent resultIntent = tokenResponse.toIntent();
    setResult(RESULT_OK, resultIntent);
    finish();
  }
}
