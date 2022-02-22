package com.kkafara.fresh.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kkafara.fresh.R;
import com.kkafara.fresh.databinding.FragmentGreetingBinding;

public class GreetingFragment extends Fragment {
  public final String TAG = "GreetingFragment";

  private FragmentGreetingBinding mBinding;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView");
    mBinding = FragmentGreetingBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "onViewCreated");

    mBinding.proceedButton.setOnClickListener(_view -> {
      Log.d(TAG, "proceedButton clicked; navigating to ActionFragment");

      Navigation.findNavController(requireView()).navigate(
          R.id.action_greetingFragment_to_actionFragment);
    });
  }
}
