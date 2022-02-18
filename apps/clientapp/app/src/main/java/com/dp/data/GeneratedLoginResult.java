package com.dp.data;

/**
 * A generic class that holds a result success w/ data or an error exception.
 */
public class GeneratedLoginResult<T> {
  // hide the private constructor to limit subclass types (Success, Error)
  private GeneratedLoginResult() {
  }

  @Override
  public String toString() {
    if (this instanceof GeneratedLoginResult.Success) {
      GeneratedLoginResult.Success success = (GeneratedLoginResult.Success) this;
      return "Success[data=" + success.getData().toString() + "]";
    } else if (this instanceof GeneratedLoginResult.Error) {
      GeneratedLoginResult.Error error = (GeneratedLoginResult.Error) this;
      return "Error[exception=" + error.getError().toString() + "]";
    }
    return "";
  }

  // Success sub-class
  public final static class Success<T> extends GeneratedLoginResult {
    private T data;

    public Success(T data) {
      this.data = data;
    }

    public T getData() {
      return this.data;
    }
  }

  // Error sub-class
  public final static class Error extends GeneratedLoginResult {
    private Exception error;

    public Error(Exception error) {
      this.error = error;
    }

    public Exception getError() {
      return this.error;
    }
  }
}