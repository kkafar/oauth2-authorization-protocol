package com.kkafara.fresh.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kkafara.fresh.data.repository.DataRepository;
import com.kkafara.fresh.data.source.DataSource;

public class AuthViewModelFactory implements ViewModelProvider.Factory {
  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(DataViewModel.class)) {
      return (T) new AuthViewModel();
    } else {
      throw new IllegalArgumentException("Unknown ViewModel class");
    }
  }
}
