package com.dp.data.repositories;

public class AuthorizationFlowRepository {
  private static volatile AuthorizationFlowRepository instance;

  private AuthorizationFlowRepository() {}

  public static AuthorizationFlowRepository getInstance() {
    if (instance == null) {
      instance = new AuthorizationFlowRepository();
    }
    return instance;
  }
}
