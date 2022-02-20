package com.kkafara.fresh.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kkafara.fresh.data.repository.AuthRepository;
import com.kkafara.fresh.data.source.AuthDataSource;

public class AuthViewModelFactory implements ViewModelProvider.Factory {
  private final LifecycleOwner mOwner;

  public AuthViewModelFactory(LifecycleOwner owner) {
    mOwner = owner;
  }

  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(AuthViewModel.class)) {
      return (T) new AuthViewModel(mOwner, AuthRepository.getInstance(AuthDataSource.getInstance()));
    } else {
      throw new IllegalArgumentException("Unknown ViewModel class");
    }
  }
}
