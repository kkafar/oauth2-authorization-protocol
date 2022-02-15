package com.dp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dp.data.viewmodels.AuthorizationViewModel;
import com.dp.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
  public static final String TAG = "LoginFragment";

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
      startActivity(intent);
    });
  }
}
