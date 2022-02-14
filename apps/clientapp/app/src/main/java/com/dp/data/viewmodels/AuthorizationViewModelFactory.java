package com.dp.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dp.data.datasources.AuthorizationServerDataSource;
import com.dp.data.repositories.AuthorizationServerRepository;

public class AuthorizationViewModelFactory implements ViewModelProvider.Factory {
  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(AuthorizationViewModel.class)) {
      return (T) new AuthorizationViewModel(AuthorizationServerRepository.getInstance(new AuthorizationServerDataSource()));
    } else {
      throw new IllegalArgumentException("Unknown ViewModel class");
    }
  }
}
