package com.clientapp3;

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

import com.clientapp3.data.viewmodels.UserAuthViewModel;
import com.clientapp3.data.viewmodels.UserAuthViewModelFactory;
import com.clientapp3.data.viewmodels.UserDataViewModel;
import com.clientapp3.data.viewmodels.UserDataViewModelFactory;
import com.clientapp3.databinding.FragmentUserDataBinding;
import com.clientapp3.ui.UserAuthState;
import com.clientapp3.ui.userdata.UserDataState;

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

    mUserDataViewModel.updateUserData(requireContext());

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
        Log.d(TAG, "onChanged UserDataState: " + userDataState.toString());
        if (userDataState.getError() == null) {
          if (userDataState.getName() != null)
            mBinding.userNameTextView.setText(userDataState.getName());

          if (userDataState.getEmail() != null)
            mBinding.userEmailTextView.setText(userDataState.getEmail());

          if (userDataState.getNick() != null)
            mBinding.userNickTextView.setText(userDataState.getNick());
        } else {
          mUserAuthViewModel.changeUserAuthState(new UserAuthState(false));
        }
      }
    });

    mBinding.fetchDataButton.setOnClickListener(_view -> {
      Log.d(TAG, "fetchDataButton clicked");
      mUserDataViewModel.updateUserData(requireContext());
    });

    mBinding.logoutButton.setOnClickListener(_view -> {
      Log.d(TAG, "logoutButtonClicked");
      mUserAuthViewModel.revokeToken();
    });
  }

}
