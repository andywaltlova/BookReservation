package com.example.bookreservations;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.bookreservations.utils.JsonAPIparser;
import com.example.bookreservations.utils.Timer;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    String apiUrl;
    JsonAPIparser parser;
    View root;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        root = findViewById(android.R.id.content).getRootView();

        ImageView my_image = findViewById(R.id.FirstImage);
        my_image.setImageResource(R.drawable.logo_knihovna);

        Button refresh_button = findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parser.jsonParse();
            }
        });

        setupSharedPreferences();

        RequestQueue mQueue = Volley.newRequestQueue(this);
        parser = new JsonAPIparser(this, root, mQueue, apiUrl);
        new Timer(root, parser);

        if (savedInstanceState == null) {
            parser.jsonParse();
        }

        setupSharedPreferences();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
            RequestQueue mQueue = Volley.newRequestQueue(this);
            parser = new JsonAPIparser(this, root, mQueue, apiUrl);
            new Timer(root, parser);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}