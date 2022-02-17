package com.dp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dp.data.viewmodels.UserAuthViewModel;
import com.dp.data.viewmodels.UserAuthViewModelFactory;
import com.dp.data.viewmodels.UserDataViewModel;
import com.dp.data.viewmodels.UserDataViewModelFactory;
import com.dp.databinding.FragmentUserDataBinding;
import com.dp.ui.UserAuthState;
import com.dp.ui.userdata.UserDataState;

public class UserDataFragment extends Fragment {
  public static final String TAG = "UserDataFragment";

  private FragmentUserDataBinding mBinding;
  private UserAuthViewModel mUserAuthViewModel;
  private UserDataViewModel mUserDataViewModel;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView");

    mBinding = FragmentUserDataBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "onViewCreated on " + requireActivity().toString());


    mUserAuthViewModel = new ViewModelProvider(requireActivity(), new UserAuthViewModelFactory())
        .get(UserAuthViewModel.class);

    mUserDataViewModel = new ViewModelProvider(requireActivity(), new UserDataViewModelFactory())
        .get(UserDataViewModel.class);

    mUserAuthViewModel.getUserAuthState().observe(getViewLifecycleOwner(), new Observer<UserAuthState>() {
      @Override
      public void onChanged(UserAuthState userAuthState) {
        if (!userAuthState.isLoggedIn()) {
          Log.d(TAG, "User is not logged in! Navigating to login fragment");
          mBinding.userNameTextView.setText(R.string.user_name_literal);
          mBinding.userEmailTextView.setText(R.string.user_email_literal);
          Navigation.findNavController(requireView()).navigate(R.id.action_userDataFragment_to_loginFragment);
        }
      }
    });

    mUserDataViewModel.getUserDataState().observe(getViewLifecycleOwner(), new Observer<UserDataState>() {
      @Override
      public void onChanged(UserDataState userDataState) {
          mBinding.userNameTextView.setText(userDataState.getName());
          mBinding.userEmailTextView.setText(userDataState.getEmail());
      }
    });

    mBinding.fetchDataButton.setOnClickListener(_view -> {
      Log.d(TAG, "fetchDataButton clicked");
      mUserDataViewModel.updateUserData();
    });

    mBinding.logoutButton.setOnClickListener(_view -> {
      Log.d(TAG, "logoutButtonClicked");
      // todo: token revocation
      mUserAuthViewModel.changeUserAuthState(new UserAuthState(false));
    });
  }

}
