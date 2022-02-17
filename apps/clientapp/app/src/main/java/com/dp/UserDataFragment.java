package com.dp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dp.data.viewmodels.UserAuthViewModel;
import com.dp.data.viewmodels.UserAuthViewModelFactory;
import com.dp.databinding.FragmentUserDataBinding;
import com.dp.ui.UserAuthState;

public class UserDataFragment extends Fragment {
  public static final String TAG = "UserDataFragment";

  private FragmentUserDataBinding mBinding;
  private UserAuthViewModel mUserAuthViewModel;

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

    Log.d(TAG, "onViewCreated");

    mUserAuthViewModel = new ViewModelProvider(requireActivity(), new UserAuthViewModelFactory())
        .get(UserAuthViewModel.class);

    mUserAuthViewModel.getUserAuthState().observe(getViewLifecycleOwner(), new Observer<UserAuthState>() {
      @Override
      public void onChanged(UserAuthState userAuthState) {
        if (userAuthState.isLoggedIn()) {
//          mBinding.userNameTextView.setText(userState.);
        }
      }
    });

  }
}
