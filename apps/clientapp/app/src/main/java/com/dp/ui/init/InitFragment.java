package com.dp.ui.init;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dp.data.viewmodels.UserViewModel;
import com.dp.data.viewmodels.UserViewModelFactory;
import com.dp.databinding.FragmentInitBinding;

public class InitFragment extends Fragment {
  public static final String TAG = "WelcomeFragment";

  private UserViewModel mViewModel;
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

    mViewModel = new ViewModelProvider(this, new UserViewModelFactory())
        .get(UserViewModel.class);


    mBinding.nextButton.setOnClickListener(_view -> {

    });
  }
}