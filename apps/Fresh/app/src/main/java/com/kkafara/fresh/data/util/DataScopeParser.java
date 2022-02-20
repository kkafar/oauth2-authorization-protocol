package com.kkafara.fresh.data.util;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DataScopeParser {
  public static String stringFromStringArray(@NonNull String[] scopes) {
    StringBuilder builder = new StringBuilder();
    for (String scope : scopes) { builder.append(scope).append(" "); }
    return builder.toString();
  }

  public static String stringFromStringSet(@NonNull Set<String> scopes) {
    StringBuilder builder = new StringBuilder();
    for (String scope : scopes) { builder.append(scope).append(" "); }
    return builder.toString();
  }

  public static String stringFromStringIterable(@NonNull Iterable<String> scopes) {
    StringBuilder builder = new StringBuilder();
    for (String scope : scopes) { builder.append(scope).append(" "); }
    return builder.toString();
  }

  public static Set<String> setStringFromStringArray(@NonNull String[] scopes) {
    Set<String> scopesSet = new HashSet<>();
    Collections.addAll(scopesSet, scopes);
    return scopesSet;
  }

  public static Set<String> setStringFromStringIterable(@NonNull Iterable<String> scopes) {
    Set<String> scopesSet = new HashSet<>();
    for (String scope : scopes) { scopesSet.add(scope); }
    return scopesSet;
  }

}
