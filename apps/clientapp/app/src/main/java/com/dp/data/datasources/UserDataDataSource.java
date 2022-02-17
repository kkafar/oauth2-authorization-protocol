package com.dp.data.datasources;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dp.auth.AuthorizationServerEndpointName;
import com.dp.auth.model.TokenResponse;
import com.dp.ui.userdata.UserDataState;
import com.google.gson.Gson;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class UserDataDataSource {
  public final String TAG = "UserDataDataSource";

  private ResourceServerDataSource mResourceServerDataSource;
  private Gson gson;

  private UserDataState mUserDataState;

  public UserDataDataSource(ResourceServerDataSource resourceServerDataSource) {
    mResourceServerDataSource = resourceServerDataSource;
    gson = new Gson();
  }

  public UserDataState fetchUserDataFromServer(String token, Set<String> scopes) {
    Thread connectionExecutor = new Thread(() -> {
      try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpGet httpGetRequest = new HttpGet(mResourceServerDataSource.getAddress());
        httpGetRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        httpGetRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        httpGetRequest.addHeader("Requested-Data", "username");

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGetRequest)) {
          Log.d(TAG, "Resource SERVER RESPONSE FOR TOKEN REQUEST");
          Log.d(TAG, httpResponse.toString());
          Log.d(TAG, Arrays.toString(httpResponse.getHeaders()));
          Log.d(TAG, Long.toString(httpResponse.getEntity().getContentLength()));
          byte[] bytes = new byte[(int)(httpResponse.getEntity().getContentLength())];
          httpResponse.getEntity().getContent().read(bytes);
          Log.d(TAG, new String(bytes));
          mUserDataState = gson.fromJson(new String(bytes), UserDataState.class);
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
    });
  connectionExecutor.start();

  try {
    connectionExecutor.join(5000);
  } catch (Exception ignore) {

  }

  return mUserDataState;
  }

  private String scopesToString(@NonNull Set<String> scopes) {
    StringBuilder builder = new StringBuilder();
    for (String scope : scopes) {
      builder.append(scope).append(' ');
    }
    return builder.toString();
  }
}
