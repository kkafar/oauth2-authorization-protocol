package com.dp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dp.data.viewmodels.AuthorizationViewModel;
import com.dp.data.viewmodels.AuthorizationViewModelFactory;
import com.dp.data.viewmodels.UserViewModel;
import com.dp.data.viewmodels.UserViewModelFactory;
import com.dp.databinding.ActivityMainBinding;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
  public final String TAG = "MainActivity";

  private AppBarConfiguration mAppBarConfiguration;
  private ActivityMainBinding mBinding;
  private AuthorizationViewModel mAuthorizationViewModel;
  private UserViewModel mUserViewModel;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    mBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    setSupportActionBar(mBinding.toolbar);

    NavController navController = getNavController();
    mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

    mAuthorizationViewModel = new ViewModelProvider(this, new AuthorizationViewModelFactory())
        .get(AuthorizationViewModel.class);

    mUserViewModel = new ViewModelProvider(this, new UserViewModelFactory())
        .get(UserViewModel.class);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = getNavController();
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
        || super.onSupportNavigateUp();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
  }

  private NavController getNavController() {
    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
    if (!(fragment instanceof NavHostFragment)) {
      throw new IllegalStateException("Activity " + this + " does not have a NavHostFragment");
    }
    return ((NavHostFragment) fragment).getNavController();
  }
}
