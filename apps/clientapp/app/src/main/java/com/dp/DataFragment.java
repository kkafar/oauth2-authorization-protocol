package com.dp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.dp.databinding.FragmentDataBinding;

public class DataFragment extends Fragment {
  private FragmentDataBinding mBinding;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState
  ) {
    mBinding = FragmentDataBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(
      @NonNull View view,
      Bundle savedInstanceState
  ) {
    super.onViewCreated(view, savedInstanceState);

    // show loading indicator & call for server data
    // then display the data
  }

}
