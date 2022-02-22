package com.kkafara.fresh.data.model;

import androidx.annotation.NonNull;

public class DataRequest {
  @NonNull
  private String mAccessToken;

  @NonNull
  private String mRequestedScopes;

  /**
   *
   * @param accessToken valid access token granted by authorization server
   * @param requestedScopes string of SPACE SEPARATED data scopes to fetch
   */
  public DataRequest(@NonNull String accessToken, @NonNull String requestedScopes) {
    mAccessToken = accessToken;
    mRequestedScopes = requestedScopes;
  }

  @NonNull
  public String getAccessToken() {
    return mAccessToken;
  }

  @NonNull
  public String getRequestedScopes() {
    return mRequestedScopes;
  }
}
