package com.kkafara.fresh.ui.fragment;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.provider.Browser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.kkafara.fresh.R;
import com.kkafara.fresh.data.model.LoginState;
import com.kkafara.fresh.databinding.FragmentActionBinding;
import com.kkafara.fresh.ui.viewmodel.AuthViewModel;
import com.kkafara.fresh.ui.viewmodel.AuthViewModelFactory;

public class ActionFragment extends Fragment {
  public final String TAG = "ActionFragment";

  private FragmentActionBinding mBinding;

  private AuthViewModel mAuthViewModel;

  private final float MIN_ALPHA = 0.2f;
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
    mBinding = FragmentActionBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "onViewCreated");

    mAuthViewModel = new ViewModelProvider(requireActivity(), new AuthViewModelFactory(this))
        .get(AuthViewModel.class);

    Log.d(TAG, "Setting callback for proceedToDataFragmentButton");
    mBinding.proceedToDataFragmentButton.setOnClickListener(_view -> {
      Log.d(TAG, "proceedToDataFragmentButton clicked");
      mAuthViewModel.assertUserIsLoggedIn();
      toggleLoadingMode(true);
    });

    Log.d(TAG, "Register for login result");
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
        LoginState loginState = result.getSuccessValue();
        if (loginState.isLoggedIn()) {
          Log.d(TAG, "User is logged in; navigating to data fragment");
          Navigation.findNavController(requireView()).navigate(R.id.action_actionFragment_to_dataFragment);
        } else {
          Log.d(TAG, "User is NOT logged in; navigating to login fragment");
          Navigation.findNavController(requireView()).navigate(R.id.action_actionFragment_to_loginFragment);
        }
      } else {
        Log.wtf(TAG, "Login process returned result, but without a value! THIS SHOULD NOT HAPPEN");
      }
    });
  }

  private void toggleLoadingMode(boolean enabled) {
    float alpha = enabled ? MIN_ALPHA : DEFAULT_ALPHA;
    mBinding.loginLoadingIndicator.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    mBinding.infoTextView.setAlpha(alpha);
    mBinding.proceedToDataFragmentButton.setAlpha(alpha);
  }
}