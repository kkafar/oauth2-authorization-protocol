package com.kkafara.fresh.servers;

public class AuthServerInfo {
  public static final String AUTHORITY = "8e4d-91-123-181-221.ngrok.io";
  public static final String HTTP_ADDRESS = "https://"+AUTHORITY;

  public static final String ENDPOINT_NAME_TOKEN = "token";
  public static final String ENDPOINT_NAME_REVOKE = "revoke";
  public static final String ENDPOINT_NAME_INTROSPECT = "introspect";
  public static final String ENDPOINT_NAME_AUTHORIZE = "authorize";
  public static final String ENDPOINT_NAME_PING = "ping";

  public static final String ENDPOINT_ADDRESS_TOKEN = HTTP_ADDRESS + "/" + ENDPOINT_NAME_TOKEN;
  public static final String ENDPOINT_ADDRESS_REVOKE = HTTP_ADDRESS + "/" + ENDPOINT_NAME_REVOKE;
  public static final String ENDPOINT_ADDRESS_INTROSPECT = HTTP_ADDRESS + "/" + ENDPOINT_NAME_INTROSPECT;
  public static final String ENDPOINT_ADDRESS_AUTHORIZE = HTTP_ADDRESS + "/" + ENDPOINT_NAME_AUTHORIZE;
  public static final String ENDPOINT_ADDRESS_PING = HTTP_ADDRESS + "/" + ENDPOINT_NAME_PING;
}
