package com.kkafara.fresh.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kkafara.fresh.data.model.DataRequest;
import com.kkafara.fresh.data.model.DataResponse;
import com.kkafara.fresh.data.model.Result;
import com.kkafara.fresh.data.source.DataSource;

public class DataRepository {
  public final String TAG = "DataRepository";

  private MutableLiveData<Result<DataResponse, Throwable>> mDataRequestResultLiveData = new MutableLiveData<>();

  private DataSource mDataSource;

  public DataRepository(@NonNull DataSource dataSource) {
    Log.d(TAG, "ctor");
    mDataSource = dataSource;

    mDataSource.getDataRequestResultLiveData().observeForever(result -> {
      Log.d(TAG, "result observer");
      mDataRequestResultLiveData.setValue(result);
    });
  }

   public LiveData<Result<DataResponse, Throwable>> getDataResponseLiveData() {
//    return mDataRequestResultLiveData;
     return mDataSource.getDataRequestResultLiveData();
   }

  public void fetchData(DataRequest request) {
    Log.d(TAG, "fetchData");
    mDataSource.fetchData(request);
  }
}
