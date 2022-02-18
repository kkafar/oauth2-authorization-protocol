package com.clientapp3.data.viewmodels;


import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.clientapp3.R;
import com.clientapp3.data.repositories.UserDataRepository;
import com.clientapp3.ui.userdata.UserDataState;

public class UserDataViewModel extends ViewModel {
  public final String TAG = "UserDataViewModel";
  private MutableLiveData<UserDataState> mUserDataState = new MutableLiveData<>();

  private UserDataRepository mUserDataRepository;

  public UserDataViewModel(UserDataRepository userDataRepository) {
    mUserDataRepository = userDataRepository;
  }

  public LiveData<UserDataState> getUserDataState() {
    return mUserDataState;
  }


  public void updateUserData(Context appContext) {
    Log.d(TAG, "updateUserData");
    String[] scopes = appContext.getResources().getStringArray(R.array.auth_required_scopes);
    StringBuilder builder = new StringBuilder();
    for (String scope : scopes) {
      builder.append(scope).append(" ");
    }
    UserDataState newUserDataState = mUserDataRepository.updateUserData(builder.toString());
    mUserDataState.setValue(newUserDataState);
  }
}
