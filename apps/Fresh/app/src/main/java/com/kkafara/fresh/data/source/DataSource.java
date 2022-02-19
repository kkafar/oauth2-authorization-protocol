package com.kkafara.fresh.data.source;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.kkafara.fresh.data.model.DataRequest;
import com.kkafara.fresh.data.model.DataResponse;
import com.kkafara.fresh.data.model.Result;
import com.kkafara.fresh.data.source.request.HttpDataRequestFactory;
import com.kkafara.fresh.net.HttpBodyDecoders;
import com.kkafara.fresh.net.HttpRequestTask;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSource {
  public final String TAG = "DataSource";

  private static volatile DataSource INSTANCE = null;

  private MutableLiveData<DataResponse> mDataResponseLiveData = new MutableLiveData<>(
      new DataResponse(null, null, null, null)
  );

  private MutableLiveData<Result<DataResponse, Throwable>> mDataRequestResultLiveData =
      new MutableLiveData<>();

  private ExecutorService mExecutor;

  private Gson mGson;

  private DataSource() {
    Log.d(TAG, "ctor");
    mExecutor = Executors.newSingleThreadExecutor();
    mGson = new Gson();
  }

  public static DataSource getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new DataSource();
    }
    return INSTANCE;
  }

//  public LiveData<DataResponse> getDataResponseLiveData() {
//    return mDataResponseLiveData;
//  }

  public LiveData<Result<DataResponse, Throwable>> getDataRequestResultLiveData() {
    return mDataRequestResultLiveData;
  }

  private void pushResultToLiveDataStream(Result<DataResponse, Throwable> result) {
    Log.d(TAG, "pushResultToLiveDataStream");
    mDataRequestResultLiveData.postValue(result);
  }

  public void fetchData(DataRequest request) {
    Log.d(TAG, "fetchData");
//    mExecutor.submit(new HttpRequestTask(
//        new HttpDataRequestFactory(request.getAccessToken(), request.getRequestedScopes()),
//        response -> {
//          Log.d(TAG, "Resource server response");
//          Log.d(TAG, response.toString());
//          Log.d(TAG, Arrays.toString(response.getHeaders()));
//
//          DataResponse dataResponse = HttpBodyDecoders
//              .decodeHttpResponseBody(response.getEntity(), DataResponse.class);
//
//          if (dataResponse == null) {
//            mDataRequestResultLiveData.setValue(
//                Result.newError(new RuntimeException("Gson returned null")) // todo (better exception type)
//            );
//          } else if (dataResponse.isError()) {
//            mDataRequestResultLiveData.setValue(
//                Result.newError(new RuntimeException(dataResponse.getError())) // todo (better exception type)
//            );
//          } else {
//            mDataRequestResultLiveData.setValue(Result.newSuccess(dataResponse));
//          }
//        },
//        exception -> {
//          mDataRequestResultLiveData.setValue(Result.newError(exception));
//        }
//    ));

    // MOCK IMPL
    mExecutor.submit(() -> {
      Log.d(TAG, "fetchData IN EXECUTOR");
      DataResponse response = new DataResponse("Kacper", "student@agh.edu.pl", "hehe", null);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      }
      pushResultToLiveDataStream(Result.newSuccess(response));
    });
  }
}
