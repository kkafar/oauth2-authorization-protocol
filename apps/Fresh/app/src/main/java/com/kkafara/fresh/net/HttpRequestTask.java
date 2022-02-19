package com.kkafara.fresh.net;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

public class HttpRequestTask implements Runnable {
  public final String TAG = "HttpRequestTask";
  private final OAuthHttpUriRequestBaseFactory requestFactory;
  private final HttpResponseCallback responseCallback;
  private final HttpConnectionFailureCallback connectionFailureCallback;

  public HttpRequestTask(
      @NonNull OAuthHttpUriRequestBaseFactory requestFactory,
      @Nullable HttpResponseCallback responseCallback,
      @Nullable HttpConnectionFailureCallback connectionFailureCallback
  ) {
    this.requestFactory = requestFactory;
    this.responseCallback = responseCallback;
    this.connectionFailureCallback = connectionFailureCallback;
  }

  @Override
  public void run() {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpUriRequestBase request = requestFactory.create();
      try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
        if (responseCallback != null) {
          responseCallback.invoke(httpResponse);
        }
      } catch (Exception exception) {
        if (exception.getMessage() != null) {
          Log.e(TAG, exception.getMessage());
        }
        exception.printStackTrace();
        if (connectionFailureCallback != null) {
          connectionFailureCallback.invoke(exception);
        }
      }
    } catch (IOException exception) {
      if (exception.getMessage() != null) {
        Log.e(TAG, exception.getMessage());
      }
      exception.printStackTrace();
      if (connectionFailureCallback != null) {
        connectionFailureCallback.invoke(exception);
      }
    }
  }
}
