package com.dp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.dp.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
  private FragmentLoginBinding mBinding;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState
  ) {
    mBinding = FragmentLoginBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(
      @NonNull View view,
      Bundle savedInstanceState
  ) {
    super.onViewCreated(view, savedInstanceState);

    mBinding.logInButton.setOnClickListener(this::onLoginButtonClicked);
  }

  private void onLoginButtonClicked(View view) {
    /**
     * TODO
     *
     * 1. Check if there is a token saved on a device
     * 2. Check if token is still valid (request to auth server)?
     * 3. If token is valid, process to data fragment
     * 4. If token is not valid, initialize token acquire process
     */

  }
}
