package com.dp.auth;

public class AuthorizationResponseError {
  public static final String NO_CODE_GRANT = "No code grant in server response";
  public static final String NO_STATE = "No state in server response";
  public static final String INVALID_CODE_GRANT = "Code grant provided by server is invalid";
  public static final String INVALID_STATE = "State returned by server is invalid";
  public static final String BAD_STATE = "State returned from server DOES NOT MATCH state issued to server";
}
