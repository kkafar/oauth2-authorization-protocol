package com.dp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dp.databinding.FragmentLoginBinding;
import com.dp.ui.login.LoginViewModel;
import com.dp.ui.login.LoginViewModelFactory;

public class LoginFragment extends Fragment {
  public static final String TAG = "LoginFragment";

  private FragmentLoginBinding mBinding;
  private LoginViewModel loginViewModel;


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

    loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
        .get(LoginViewModel.class);

    mBinding.loginButton.setOnClickListener(_view -> {
      Log.d(TAG, "Log in button pressed");
      // TODO: delegate to view model?!
      Intent intent = new Intent(getActivity(), AuthorizationActivity.class);
      startActivity(intent);
    });

    mBinding.logoutButton.setOnClickListener(_view -> {
      Log.d(TAG, "Log out button pressed");
    });
  }
}
