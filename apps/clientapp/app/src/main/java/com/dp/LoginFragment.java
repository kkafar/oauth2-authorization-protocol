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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.dp.auth.model.TokenResponse;
import com.dp.data.viewmodels.UserAuthViewModel;
import com.dp.data.viewmodels.UserAuthViewModelFactory;
import com.dp.databinding.FragmentLoginBinding;
import com.dp.ui.UserAuthState;

public class LoginFragment extends Fragment {
  public final String TAG = "LoginFragment";

  private final int LOGIN_REQUEST_CODE = 0;

  private FragmentLoginBinding mBinding;
  private UserAuthViewModel mUserAuthViewModel;


  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {
    mBinding = FragmentLoginBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mBinding.loginButton.setOnClickListener(_view -> {
      Log.d(TAG, "Log in button pressed");
      Intent intent = new Intent(getActivity(), AuthorizationActivity.class);
      Log.d(TAG, "Launching intent from activity " + getActivity());
      startActivityForResult(intent, LOGIN_REQUEST_CODE);
    });

    mUserAuthViewModel = new ViewModelProvider(requireActivity(), new UserAuthViewModelFactory())
        .get(UserAuthViewModel.class);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == LOGIN_REQUEST_CODE) {
      handleLoginActivityResult(resultCode, data);
    } else {
      handleUnknownActivityResult(resultCode, data);
    }
  }

  private void handleUnknownActivityResult(int resultCode, @Nullable Intent data) {
    Log.e(TAG, "Unknown activity returned result!");
  }

  private void handleLoginActivityResult(int resultCode, @Nullable Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      Log.d(TAG, "Login activity resulted in success. Currently on activity " + requireActivity().toString());
      mUserAuthViewModel.changeUserAuthState(new UserAuthState(true));

      Log.d(TAG, "Navigating to user data fragment");
      Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_userDataFragment);
    } else if (resultCode == Activity.RESULT_CANCELED) { // login did not succeed
      Log.d(TAG, "Login activity resulted in failure");
      mUserAuthViewModel.changeUserAuthState(new UserAuthState(false));
    }
  }
}
