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
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.kkafara.fresh.R;
import com.kkafara.fresh.data.model.DataResponse;
import com.kkafara.fresh.data.model.UserData;
import com.kkafara.fresh.databinding.FragmentDataBinding;
import com.kkafara.fresh.data.repository.DataRepository;
import com.kkafara.fresh.ui.viewmodel.DataViewModel;
import com.kkafara.fresh.ui.viewmodel.DataViewModelFactory;

public class DataFragment extends Fragment {
  public final String TAG = "DataFragment";

  private final int mSnackBarDuration = 2000;

  private FragmentDataBinding mBinding;

  private DataViewModel mDataViewModel;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView");

    mBinding = FragmentDataBinding.inflate(inflater, container, false);
    return mBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "onViewCreated");

    Log.d(TAG, "create DataViewModel");
    mDataViewModel = new ViewModelProvider(requireActivity(), new DataViewModelFactory())
        .get(DataViewModel.class);


    Log.d(TAG, "register callback for data fetch result");
    mDataViewModel.getDataResponseLiveData().observe(requireActivity(), result -> {
      mBinding.dataFetchProgressBar.setVisibility(View.INVISIBLE);
//      setAlphaForAllButLoadingIndicator(1);
      if (result.isError()) {
        Log.d(TAG, "Data fetch failed");
        String errorMessage = result.getError().getMessage();
        if (errorMessage == null) {
          errorMessage = "Unknown data fetch error";
        }
        View _view = getView();
        if (_view != null) {
          Snackbar.make(getView(), errorMessage, mSnackBarDuration).show();
        }
      } else if (result.hasSuccessValue()) {
        Log.d(TAG, "Data fetch succeeded");
        DataResponse response = result.getSuccessValue();
        UserData userData = UserData.fromDataResponse(response);
        mBinding.usernameDataTextView.setText(userData.getUsername().orElse(getString(R.string.username)));
        mBinding.emailDataTextView.setText(userData.getMail().orElse(getString(R.string.email)));
        mBinding.emailDataTextView.setText(userData.getNick().orElse(getString(R.string.nickname)));
      } else {
        Log.wtf(TAG, "THIS SHOULD NOT HAPPEN");
      }
    });

    Log.d(TAG, "register callback for logout button");
    mBinding.logoutButton.setOnClickListener(_view -> {
      Navigation.findNavController(requireView()).navigate(R.id.action_dataFragment_to_actionFragment);
    });

    Log.d(TAG, "register callback for refresh button");
    mBinding.refreshDataButton.setOnClickListener(_view -> {
      mDataViewModel.fetchData();
      mBinding.dataFetchProgressBar.setVisibility(View.VISIBLE);
//      setAlphaForAllButLoadingIndicator(0.2f);
    });
  }

  private void setAlphaForAllButLoadingIndicator(float alpha) {
    mBinding.usernameLabelTextView.setAlpha(alpha);
    mBinding.usernameDataTextView.setAlpha(alpha);
    mBinding.emailLabelTextView.setAlpha(alpha);
    mBinding.emailDataTextView.setAlpha(alpha);
    mBinding.nicknameLabelTextView.setAlpha(alpha);
    mBinding.nicknameDataTextView.setAlpha(alpha);
    mBinding.logoutButton.setAlpha(alpha);
    mBinding.refreshDataButton.setAlpha(alpha);
  }
}
