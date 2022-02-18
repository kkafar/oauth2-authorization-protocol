package com.clientapp3.auth.exceptions;

public class InvalidAuthorizationResponseException extends Exception {
  public InvalidAuthorizationResponseException(String errorMessage) {
    super(errorMessage);
  }

  public InvalidAuthorizationResponseException(String errorMessage, Throwable e) {
    super(errorMessage, e);
  }
}
