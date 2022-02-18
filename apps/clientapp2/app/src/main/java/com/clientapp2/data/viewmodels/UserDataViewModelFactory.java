package com.clientapp2.data.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.clientapp2.data.datasources.ResourceServerDataSource;
import com.clientapp2.data.datasources.UserDataDataSource;
import com.clientapp2.data.repositories.UserDataRepository;

public class UserDataViewModelFactory implements ViewModelProvider.Factory {
  @NonNull
  @Override
  @SuppressWarnings("unchecked")
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(UserDataViewModel.class)) {
      return (T) new UserDataViewModel(UserDataRepository.getInstance(new UserDataDataSource(new ResourceServerDataSource())));
    } else {
      throw new IllegalArgumentException("Unknown ViewModel class");
    }
  }
}
