package com.clientapp2.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.clientapp2.data.datasources.UserLoginDataSource;
import com.clientapp2.data.repositories.AuthorizationManager;
import com.clientapp2.data.repositories.UserAuthRepository;

public class UserAuthViewModelFactory implements ViewModelProvider.Factory {
  @NonNull
  @Override
  @SuppressWarnings("unchecked")
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(UserAuthViewModel.class)) {
      return (T) new UserAuthViewModel(
          UserAuthRepository.getInstance(new UserLoginDataSource()),
          AuthorizationManager.getInstance());
    } else {
      throw new IllegalArgumentException("Unknown ViewModel class");
    }
  }
}
