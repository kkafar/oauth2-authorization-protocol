package com.kkafara.fresh.data.source;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kkafara.fresh.data.model.DataRequest;
import com.kkafara.fresh.data.model.DataResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSource {
  private static volatile DataSource INSTANCE = null;

  private ExecutorService mExecutor;

  private DataSource() {
    mExecutor = Executors.newSingleThreadExecutor();
  }

  public static DataSource getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new DataSource();
    }
    return INSTANCE;
  }

  private MutableLiveData<DataResponse> mDataResponseLiveData = new MutableLiveData<>(
      new DataResponse(null, null, null, null)
  );


  public LiveData<DataResponse> getDataResponseLiveData() {
    return mDataResponseLiveData;
  }

  public void fetchData(DataRequest request) {

  }
}
