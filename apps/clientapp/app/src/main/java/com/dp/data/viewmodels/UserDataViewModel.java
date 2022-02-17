package com.dp.data.viewmodels;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dp.data.repositories.UserDataRepository;
import com.dp.ui.userdata.UserDataState;

public class UserDataViewModel extends ViewModel {
  private MutableLiveData<UserDataState> mUserDataState = new MutableLiveData<>();

  private UserDataRepository mUserDataRepository;

  public UserDataViewModel(UserDataRepository userDataRepository) {
    mUserDataRepository = userDataRepository;
  }

  public LiveData<UserDataState> getUserDataState() {
    return mUserDataState;
  }


  public void updateUserData() {
    UserDataState newUserDataState = mUserDataRepository.updateUserData();
    mUserDataState.setValue(newUserDataState);
  }
}
