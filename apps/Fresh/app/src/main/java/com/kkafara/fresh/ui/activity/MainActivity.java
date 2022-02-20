package com.kkafara.fresh.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.kkafara.fresh.R;

import com.kkafara.fresh.database.DatabaseInstanceProvider;
import com.kkafara.fresh.database.MainDatabase;
import com.kkafara.fresh.databinding.ActivityMainBinding;
import com.kkafara.fresh.oauth.data.model.AuthCodeResponse;
import com.kkafara.fresh.ui.viewmodel.AuthViewModel;
import com.kkafara.fresh.ui.viewmodel.AuthViewModelFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
  public final String TAG = "MainActivity";

  private ActivityMainBinding mBinding;
  private AppBarConfiguration mAppBarConfiguration;
  private MainDatabase mDatabase;
  private AuthViewModel mAuthViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "onCreate");

    mBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    NavController navController = getNavController();

    mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

    // initialize database
    mDatabase = DatabaseInstanceProvider.getInstance(getApplicationContext());

    mAuthViewModel = new ViewModelProvider(this, new AuthViewModelFactory(this))
        .get(AuthViewModel.class);
  }

    @Override
  public boolean onSupportNavigateUp() {
    NavController navController = getNavController();
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
        || super.onSupportNavigateUp();
  }

  private NavController getNavController() {
    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main_content);
    if (!(fragment instanceof NavHostFragment)) {
      throw new IllegalStateException("Activity " + this + " does not have a NavHostFragment");
    }
    return ((NavHostFragment) fragment).getNavController();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Log.d(TAG, "onNewIntent");

    Uri intentData = intent.getData();
    if (intentData != null) {
      AuthCodeResponse response = AuthCodeResponse.fromUri(intentData);

      if (response.isError()) {
        // TODO: HANDLE INVALID RESPONSE
      }

      Log.d(TAG, "Whole server response: " + intentData);
      Log.d(TAG, "Authorization code grant granted by server: " + response.code);

      mAuthViewModel.getAccessTokenByAuthCode(this, response);
    }
  }

  private boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivityManager
         = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null;
  }

  private boolean hasActiveInternetConnection(Context context) {
    if (isNetworkAvailable(context)) {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.e(TAG, "Error checking internet connection", e);
        }
    } else {
        Log.d(TAG, "No network available!");
    }
    return false;
}
}
