package com.dp.data.datasources;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dp.auth.AuthorizationServerEndpointName;
import com.dp.auth.model.TokenResponse;
import com.dp.net.HttpBodyDecoders;
import com.dp.net.HttpContentTypes;
import com.dp.net.HttpRequestTask;
import com.dp.net.HttpUriRequestBaseBuilder;
import com.dp.net.OAuthHttpUriRequestBaseFactory;
import com.dp.ui.userdata.UserDataState;
import com.google.gson.Gson;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserDataDataSource {
  public final String TAG = "UserDataDataSource";

  private ResourceServerDataSource mResourceServerDataSource;
  private Gson gson;

  private UserDataState mUserDataState;
  private ExecutorService executor;

  public UserDataDataSource(ResourceServerDataSource resourceServerDataSource) {
    mResourceServerDataSource = resourceServerDataSource;
    gson = new Gson();
    executor = Executors.newSingleThreadExecutor();
  }

  public UserDataState fetchUserDataFromServer(String token, String scopes) {
    Log.d(TAG, "fetchUserDataFromServer");
    Log.d(TAG, "TOKEN: " + token);
    Log.d(TAG, "SCOPES: " + scopes);

    Future guard = executor.submit(new HttpRequestTask(
        new UserDataRequestFactory(token, scopes),
        httpResponse -> {
          Log.d(TAG, "Resource server response");
          Log.d(TAG, httpResponse.toString());
          Log.d(TAG, Arrays.toString(httpResponse.getHeaders()));
          mUserDataState = HttpBodyDecoders
              .decodeHttpResponseBody(httpResponse.getEntity(), UserDataState.class);
        },
        null
    ));

    try {
      guard.get(); // terminate operation
    } catch (InterruptedException | ExecutionException exception) {
      exception.printStackTrace();
    }
    return mUserDataState;
  }

  private final class UserDataRequestFactory implements OAuthHttpUriRequestBaseFactory {
    private final String mToken;
    private final String mScope;
    public UserDataRequestFactory(@NonNull String token, @NonNull String scope) {
      mToken = token;
      mScope = scope;
    }

    @Override
    public HttpUriRequestBase create() {
      HttpUriRequestBaseBuilder requestBuilder = new HttpUriRequestBaseBuilder(
          Method.GET,
          mResourceServerDataSource.getAddress()
      );
      return requestBuilder
          .setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + mToken)
          .setHeader(HttpHeaders.CONTENT_TYPE, HttpContentTypes.APPLICATION_X_WWW_FORM_URLENCODED)
          .setHeader("Requested-Data", mScope)
          .build();
    }
  }


  private String scopesToString(@NonNull Set<String> scopes) {
    StringBuilder builder = new StringBuilder();
    for (String scope : scopes) {
      builder.append(scope).append(' ');
    }
    return builder.toString();
  }
}
