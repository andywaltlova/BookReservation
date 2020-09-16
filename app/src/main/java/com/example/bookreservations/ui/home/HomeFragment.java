package com.example.bookreservations.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.bookreservations.R;
import com.example.bookreservations.utils.JsonAPIparser;
import com.example.bookreservations.utils.Timer;


public class HomeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    String apiUrl;
    JsonAPIparser parser;
    View root;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView my_image = root.findViewById(R.id.FirstImage);
        my_image.setImageResource(R.drawable.logo_knihovna);

        Button refresh_button = root.findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parser.jsonParse();
            }
        });

        setupSharedPreferences();

        RequestQueue mQueue = Volley.newRequestQueue(requireActivity());
        parser = new JsonAPIparser(getActivity(), root, mQueue, apiUrl);
        new Timer(root, parser);

        if (savedInstanceState == null) {
            parser.jsonParse();
        }

        //TODO: Save fragment state
        return root;
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        this.apiUrl = sharedPreferences.getString("api_url", null);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("api_url")) {

            this.apiUrl = sharedPreferences.getString("api_url", null);

            //setTextVisible(sharedPreferences.getBoolean("display_text",true));
            // Should also set view and buttons that timer uses
            RequestQueue mQueue = Volley.newRequestQueue(requireActivity());
            parser = new JsonAPIparser(getActivity(), root, mQueue, apiUrl);
            new Timer(root, parser);

        }
    }
}