package com.kkafara.fresh.data.source;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.kkafara.fresh.data.model.DataRequest;
import com.kkafara.fresh.data.model.DataResponse;
import com.kkafara.fresh.data.model.Result;
import com.kkafara.fresh.data.source.request.HttpDataRequestFactory;
import com.kkafara.fresh.database.DatabaseInstanceProvider;
import com.kkafara.fresh.database.MainDatabase;
import com.kkafara.fresh.database.dao.AuthInfoDao;
import com.kkafara.fresh.database.entity.AuthInfoRecord;
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

  private MainDatabase mDatabase;

  private AuthInfoDao mAuthInfoDao;

  private DataSource() {
    Log.d(TAG, "ctor");
    mExecutor = Executors.newSingleThreadExecutor();
    mGson = new Gson();
    mAuthInfoDao = DatabaseInstanceProvider.getInstance(null).getAuthInfoDao();
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
    mExecutor.submit(() -> {
      AuthInfoRecord record = mAuthInfoDao.findByUserId(0);
      if (record == null || record.accessToken == null) {
        pushResultToLiveDataStream(Result.newError(new RuntimeException("Null record or token")));
        return;
      }
      new HttpRequestTask<Void>(
          new HttpDataRequestFactory(record.accessToken, request.getRequestedScopes()),
          response -> {
            Log.d(TAG, "Resource server response");
            Log.d(TAG, response.toString());
            Log.d(TAG, Arrays.toString(response.getHeaders()));

            DataResponse dataResponse = HttpBodyDecoders
                .decodeHttpResponseBody(response.getEntity(), DataResponse.class);

            if (dataResponse == null) {
              pushResultToLiveDataStream(Result.newError(new RuntimeException("Gson returned null"))); // todo (better exception type)
            } else if (dataResponse.isError()) {
              pushResultToLiveDataStream(Result.newError(new RuntimeException(dataResponse.getError()))); // todo (better exception type)
            } else {
              pushResultToLiveDataStream(Result.newSuccess(dataResponse));
            }
            return null;
          },
          exception -> {
            pushResultToLiveDataStream(Result.newError(exception));
            return null;
          }
      ).run();

    });
  }
}
