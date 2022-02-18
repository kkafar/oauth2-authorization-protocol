package com.clientapp2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.clientapp2.auth.AuthStatus;
import com.clientapp2.auth.exceptions.InvalidAuthorizationResponseException;
import com.clientapp2.auth.model.AuthorizationResponse;
import com.clientapp2.data.viewmodels.AuthorizationViewModel;
import com.clientapp2.data.viewmodels.AuthorizationViewModelFactory;
import com.clientapp2.databinding.ActivityAuthorizationBinding;

import java.util.concurrent.ExecutionException;

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

    try {
      AuthStatus status =  mAuthViewModel.authorize(this);
      if (status == AuthStatus.COMPLETED_OK) {
        setResult(RESULT_OK);
        finish();
      }
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
      setResult(RESULT_CANCELED, null);
      finish();
    }
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

    mAuthViewModel.acquireAccessToken(this, response);
    setResult(RESULT_OK, null);
    finish();
  }
}
