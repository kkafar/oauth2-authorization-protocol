package com.kkafara.fresh.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.kkafara.fresh.R;

import com.kkafara.fresh.database.DatabaseInstanceProvider;
import com.kkafara.fresh.database.MainDatabase;
import com.kkafara.fresh.databinding.ActivityMainBinding;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
  public final String TAG = "MainActivity";

  private ActivityMainBinding mBinding;
  private AppBarConfiguration mAppBarConfiguration;
  private MainDatabase mDatabase;

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
