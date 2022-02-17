package com.dp.data.viewmodels;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dp.ui.userdata.UserDataState;

public class UserDataViewModel {
  private MutableLiveData<UserDataState> mUserDataState = new MutableLiveData<>();

  public UserDataViewModel() {

  }

  public LiveData<UserDataState> getUserDataState() {
    return mUserDataState;
  }




}
