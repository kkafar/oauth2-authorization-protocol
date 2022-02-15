package com.dp.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dp.data.datasources.UserLoginDataSource;
import com.dp.data.repositories.UserLoginRepository;

public class UserViewModelFactory implements ViewModelProvider.Factory {
  @NonNull
  @Override
  @SuppressWarnings("unchecked")
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(UserViewModel.class)) {
      return (T) new UserViewModel(UserLoginRepository
          .getInstance(new UserLoginDataSource()));
    } else {
      throw new IllegalArgumentException("Unknown ViewModel class");
    }
  }
}
