package com.dp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dp.data.viewmodels.AuthorizationViewModel;
import com.dp.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
  public final String TAG = "LoginFragment";

  private final int LOGIN_REQUEST_CODE = 0;

  private FragmentLoginBinding mBinding;
  private AuthorizationViewModel mAuthViewModel;


  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {
    mBinding = FragmentLoginBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mBinding.loginButton.setOnClickListener(_view -> {
      Log.d(TAG, "Log in button pressed");
      Intent intent = new Intent(getActivity(), AuthorizationActivity.class);
      Log.d(TAG, "Launching intent from activity " + getActivity());
      startActivityForResult(intent, LOGIN_REQUEST_CODE);
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    switch (requestCode) {
      case LOGIN_REQUEST_CODE: {
        handleLoginActivityResult(resultCode, data);
        break;
      }
      default:
        handleUnknownActivityResult(resultCode, data);
    }
  }

  private void handleUnknownActivityResult(int resultCode, @Nullable Intent data) {
    Log.e(TAG, "Unknown activity returned result!");
  }

  private void handleLoginActivityResult(int resultCode, @Nullable Intent data) {
    if (resultCode == Activity.RESULT_OK) {

    } else if (resultCode == Activity.RESULT_CANCELED) { // login did not succeed

    }
  }
}
