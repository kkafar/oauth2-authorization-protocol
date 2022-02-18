package com.clientapp2.net;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.apache.hc.core5.http.HttpEntity;

import java.io.IOException;

public class HttpBodyDecoders {
  public static final String TAG = "HttpBodyDecoders";
  private static final Gson sGson = new Gson();

  public static <T> T decodeHttpResponseBody(@NonNull HttpEntity body, Class<T> klazz) {
    String jsonString = decodeHttpResponseBodyToString(body);
    if (jsonString == null) {
      Log.w(TAG, "failed to decode http response body to string");
      return null;
    }
    T retVal = sGson.fromJson(jsonString, klazz);
    return retVal;
  }

  public static String decodeHttpResponseBodyToString(@NonNull HttpEntity body) {
    long bodyLength = body.getContentLength();
    if (bodyLength <= 0) {
      Log.w(TAG, "Attempt to decode http response boyd of invalid length");
      return null;
    }
    byte[] bytes = new byte[(int)(bodyLength)];
    try {
      int readBytes = body.getContent().read(bytes);
      if (readBytes != bodyLength) {
        Log.w(TAG, "number of read bytes differs from http response body length");
      }
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
    String retval = new String(bytes);
    Log.d(TAG, retval);
    return retval;
  }

}
