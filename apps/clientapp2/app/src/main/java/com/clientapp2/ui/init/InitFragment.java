package com.clientapp2.ui.init;


import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clientapp2.R;
import com.clientapp2.data.viewmodels.UserAuthViewModel;
import com.clientapp2.data.viewmodels.UserAuthViewModelFactory;
import com.clientapp2.databinding.FragmentInitBinding;

public class InitFragment extends Fragment {
  public static final String TAG = "WelcomeFragment";

  private UserAuthViewModel mViewModel;
  private FragmentInitBinding mBinding;

  public static InitFragment newInstance() {
    return new InitFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    Log.d(TAG, "onCreateView");

    mBinding = FragmentInitBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Log.d(TAG, "onViewCreated");

    mViewModel = new ViewModelProvider(requireActivity(), new UserAuthViewModelFactory())
        .get(UserAuthViewModel.class);


    mBinding.nextButton.setOnClickListener(_view -> {
      if (mViewModel.isUserLoggedIn()) {
        Log.d(TAG, "User has valid access token. Navigating to data screen.");
        Navigation.findNavController(_view).navigate(R.id.action_initFragment_to_userDataFragment);
      } else {
        Log.d(TAG, "User does NOT have valid access token. Navigating to login screen.");
        Navigation.findNavController(_view).navigate(R.id.action_initFragment_to_loginFragment);
      }
    });
  }
}
