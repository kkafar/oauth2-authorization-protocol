package com.clientapp3.auth.pkce;

import java.util.Random;

public class StateProvider {
  public final String TAG = "StateProvider";
  private static final char[] ALPHABET = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm0123456789-._~".toCharArray();
  private static final int LENGTH = 32;

  public String generate() {
    Random random = new Random();
    char[] state = new char[LENGTH];
    for (int i = 0; i < LENGTH; ++i) {
      state[i] = ALPHABET[random.nextInt(ALPHABET.length)];
    }
    return new String(state);
  }
}
