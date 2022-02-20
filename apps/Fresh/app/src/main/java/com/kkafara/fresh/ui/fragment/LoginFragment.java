package com.kkafara.fresh.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.kkafara.fresh.R;
import com.kkafara.fresh.databinding.FragmentLoginBinding;
import com.kkafara.fresh.ui.viewmodel.AuthViewModel;
import com.kkafara.fresh.ui.viewmodel.AuthViewModelFactory;

import java.util.Arrays;
import java.util.HashSet;

public class LoginFragment extends Fragment {
  public final String TAG = "LoginFragment";

  private FragmentLoginBinding mBinding;

  private AuthViewModel mAuthViewModel;

  private final float MIN_ALPHA = .2f;
  private final float DEFAULT_ALPHA = 1f;

  private final int mSnackBarDuration = 2000;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView");

    mBinding = FragmentLoginBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "onViewCreated");

    mAuthViewModel = new ViewModelProvider(requireActivity(), new AuthViewModelFactory(this))
        .get(AuthViewModel.class);


    mBinding.loginButton.setOnClickListener(_view -> {
      Log.d(TAG, "loginButton clicked; starting authorization code flow");

      toggleLoadingMode(true);

      String[] requiredScopes = getResources().getStringArray(R.array.valid_scopes);

      mAuthViewModel.startAuthorizationCodeFlow(requireContext(), Arrays.asList(requiredScopes));
    });

    mAuthViewModel.getLoginStateLiveData().observe(this, result -> {
      toggleLoadingMode(false);

      if (result.isError()) {
        Log.d(TAG, "Login process failed");
        String errorMessage = result.getError().getMessage();
        if (errorMessage == null) {
          errorMessage = "Login process failed with unknown error";
        }
        View _view = getView();
        if (_view != null) {
          Snackbar.make(_view, errorMessage, mSnackBarDuration).show();
        }
      } else if (result.hasSuccessValue()) {
        Log.d(TAG, "Login process succeeded");
        if (result.getSuccessValue().isLoggedIn()) {
          Log.d(TAG, "Token obtained");
          Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_dataFragment);
        } else {
          Log.d(TAG, "Token was NOT obtained!!!!");
          View _view = getView();
          if (_view != null) {
            Snackbar.make(_view, "TOKEN WAS NOT OBTAINED!", mSnackBarDuration).show();
          }
        }
      }
    });
  }

  private void toggleLoadingMode(boolean enabled) {
    float alpha = enabled ? MIN_ALPHA : DEFAULT_ALPHA;

    mBinding.loginProgressBar.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    mBinding.loginButton.setAlpha(alpha);
    mBinding.loginButton.setClickable(!enabled);
    mBinding.infoLoginTextView.setAlpha(alpha);
  }
}
