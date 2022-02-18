package com.dp.net;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dp.auth.OAuthHttpRequestParameter;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;
import java.util.concurrent.Callable;

public class HttpRequestTask implements Runnable {
  public final String TAG = "HttpRequestTask";
  private final OAuthHttpUriRequestBaseFactory requestFactory;
  private final HttpResponseCallback responseCallback;

  public HttpRequestTask(
      @NonNull OAuthHttpUriRequestBaseFactory requestFactory,
      @Nullable HttpResponseCallback responseCallback
  ) {
    this.requestFactory = requestFactory;
    this.responseCallback = responseCallback;
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

      }
    } catch (IOException exception) {
      if (exception.getMessage() != null) {
        Log.e(TAG, exception.getMessage());
      }
      exception.printStackTrace();
    }
  }
}
