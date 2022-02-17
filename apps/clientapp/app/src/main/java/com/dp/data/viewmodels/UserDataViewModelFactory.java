package com.dp.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dp.data.datasources.UserDataDataSource;
import com.dp.data.datasources.UserLoginDataSource;
import com.dp.data.repositories.AuthorizationManager;
import com.dp.data.repositories.UserAuthRepository;
import com.dp.data.repositories.UserDataRepository;

public class UserDataViewModelFactory implements ViewModelProvider.Factory {
  @NonNull
  @Override
  @SuppressWarnings("unchecked")
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(UserDataViewModel.class)) {
      return (T) new UserDataViewModel(UserDataRepository.getInstance(new UserDataDataSource()));
    } else {
      throw new IllegalArgumentException("Unknown ViewModel class");
    }
  }
}
