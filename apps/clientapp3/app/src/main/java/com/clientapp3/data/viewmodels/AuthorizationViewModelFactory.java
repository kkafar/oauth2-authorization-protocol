package com.clientapp3.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.clientapp3.data.datasources.AuthorizationServerDataSource;
import com.clientapp3.data.repositories.AuthorizationManager;
import com.clientapp3.data.repositories.AuthorizationServerRepository;

public class AuthorizationViewModelFactory implements ViewModelProvider.Factory {
  @NonNull
  @Override
  @SuppressWarnings("unchecked")
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(AuthorizationViewModel.class)) {
      return (T) new AuthorizationViewModel(
          AuthorizationServerRepository.getInstance(new AuthorizationServerDataSource()),
          AuthorizationManager.getInstance());
    } else {
      throw new IllegalArgumentException("Unknown ViewModel class");
    }
  }
}
