package com.clientapp3.data.datasources;

import com.clientapp3.data.GeneratedLoginResult;
import com.clientapp3.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

  public GeneratedLoginResult<LoggedInUser> login(String username, String password) {

    try {
      // TODO: handle loggedInUser authentication
      LoggedInUser fakeUser =
          new LoggedInUser(
              java.util.UUID.randomUUID().toString(),
              "Jane Doe");
      return new GeneratedLoginResult.Success<>(fakeUser);
    } catch (Exception e) {
      return new GeneratedLoginResult.Error(new IOException("Error logging in", e));
    }
  }

  public void logout() {
    // TODO: revoke authentication
  }
}