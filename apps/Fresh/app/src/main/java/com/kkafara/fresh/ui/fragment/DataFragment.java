package com.kkafara.fresh.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.kkafara.fresh.R;
import com.kkafara.fresh.data.model.DataResponse;
import com.kkafara.fresh.data.model.UserData;
import com.kkafara.fresh.databinding.FragmentDataBinding;
import com.kkafara.fresh.ui.viewmodel.AuthViewModel;
import com.kkafara.fresh.ui.viewmodel.AuthViewModelFactory;
import com.kkafara.fresh.ui.viewmodel.DataViewModel;
import com.kkafara.fresh.ui.viewmodel.DataViewModelFactory;

import java.util.HashSet;
import java.util.Set;

public class DataFragment extends Fragment {
  public final String TAG = "DataFragment";

  private final int mSnackBarDuration = 2000;

  private FragmentDataBinding mBinding;

  private DataViewModel mDataViewModel;
  private AuthViewModel mAuthViewModel;

  private final float MIN_ALPHA = .2f;
  private final float DEFAULT_ALPHA = 1f;

  private String loadingIndicatorContext = null;

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

    mDataViewModel = new ViewModelProvider(requireActivity(), new DataViewModelFactory())
        .get(DataViewModel.class);

    mAuthViewModel = new ViewModelProvider(requireActivity(), new AuthViewModelFactory(this))
        .get(AuthViewModel.class);

    mAuthViewModel.getLoginStateLiveData().observe(this, result -> {
      Log.d(TAG, "login state data stream observer");

      toggleLoadingMode(false, "login");

      if (result.isError()) {
        Log.d(TAG, "result error from login state data stream");
        // TODO: HANDLE ERROR
      } else if (result.hasSuccessValue()) {
        if (!result.getSuccessValue().isLoggedIn()) {
          // TODO: better navigation
          Navigation.findNavController(requireView()).navigate(R.id.action_dataFragment_to_actionFragment);
        }
      } else {
        Log.d(TAG, "Result w/o login state; THIS SHOULD NOT HAPPEN");
      }
    });

    mDataViewModel.getDataResponseLiveData().observe(this, result -> {
      Log.d(TAG, "data response stream observer");
      toggleLoadingMode(false, "data");
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
        mBinding.nicknameDataTextView.setText(userData.getNick().orElse(getString(R.string.nickname)));
      } else {
        Log.wtf(TAG, "THIS SHOULD NOT HAPPEN");
      }
    });

    mBinding.logoutButton.setOnClickListener(_view -> {
      Log.d(TAG, "logoutButton pressed");
      toggleLoadingMode(true, "login");
      mAuthViewModel.logout();
    });

    mBinding.refreshDataButton.setOnClickListener(_view -> {
      Log.d(TAG, "refreshDataButton pressed");
      toggleLoadingMode(true, "data");
      mDataViewModel.fetchData(getCurrentScopes());
    });

    toggleLoadingMode(true, "data");

    mDataViewModel.fetchData(getCurrentScopes());
  }

  private void toggleLoadingMode(boolean enabled, String context) {
    Log.d(TAG, "toggleLoadingMode: " + enabled);

    if (enabled) loadingIndicatorContext = context;

    if (loadingIndicatorContext.equals(context)) {
      float alpha = enabled ? MIN_ALPHA : DEFAULT_ALPHA;
      mBinding.usernameSwitch.setAlpha(alpha);
      mBinding.usernameDataTextView.setAlpha(alpha);
      mBinding.emailSwitch.setAlpha(alpha);
      mBinding.emailDataTextView.setAlpha(alpha);
      mBinding.nickSwitch.setAlpha(alpha);
      mBinding.nicknameDataTextView.setAlpha(alpha);
      mBinding.logoutButton.setAlpha(alpha);
      mBinding.refreshDataButton.setAlpha(alpha);

      mBinding.logoutButton.setClickable(!enabled);
      mBinding.refreshDataButton.setClickable(!enabled);
      mBinding.usernameSwitch.setClickable(!enabled);
      mBinding.emailSwitch.setClickable(!enabled);
      mBinding.nickSwitch.setClickable(!enabled);

      mBinding.dataFetchProgressBar.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }
  }

  private Set<String> getCurrentScopes() {
    Set<String> scopes = new HashSet<>();
    if (mBinding.usernameSwitch.isChecked()) {
      scopes.add("username");
    }
    if (mBinding.emailSwitch.isChecked()) {
      scopes.add("mail");
    }
    if (mBinding.nickSwitch.isChecked()) {
      scopes.add("nick");
    }
    return scopes;
  }
}
