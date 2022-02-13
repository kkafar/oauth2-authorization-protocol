package com.dp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dp.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

  private FragmentFirstBinding binding;

  private CustomTabsIntent.Builder ctiBuilder;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState
  ) {
    ctiBuilder = new CustomTabsIntent.Builder();
    binding = FragmentFirstBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        NavHostFragment.findNavController(FirstFragment.this)
            .navigate(R.id.action_FirstFragment_to_loginFragment);
      }
    });

    binding.httpRequestButton.setOnClickListener((_view) -> {
      CustomTabsIntent intent = ctiBuilder.build();
      intent.launchUrl(requireContext(), Uri.parse("http://3714-89-70-9-88.ngrok.io"));
    });
  }
}
