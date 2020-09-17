package com.example.bookreservations;

import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.bookreservations.utils.JsonAPIparser;
import com.example.bookreservations.utils.Notifications;
import com.example.bookreservations.utils.Timer;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View root;
    private JsonAPIparser parser;
    private Notifications notifications;
    private String apiUrl;
    private boolean shouldNotify;

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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        notifications = new Notifications(this, shouldNotify);
        parser = new JsonAPIparser(root, requestQueue, apiUrl, notifications);
        new Timer(root, parser);

        if (savedInstanceState == null && apiUrl != null)
            parser.jsonParse();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
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
        if (id == R.id.about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setPreferences(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setPreferences(sharedPreferences);
        switch (key) {
            case "api_url": {
                parser.setApiUrl(apiUrl);
                new Timer(root, parser);
                parser.jsonParse();
            }
            case "notify": {
                notifications.setShouldNotify(shouldNotify);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setPreferences(SharedPreferences sharedPreferences) {
        this.apiUrl = sharedPreferences.getString("api_url", null);
        TextView url_view = findViewById(R.id.empty);
        if (apiUrl == null || apiUrl.equals(""))
            url_view.setText("! API URL address is not set !\n\n(Settings -> API URL)");
        else
            url_view.setText("");
        this.shouldNotify = sharedPreferences.getBoolean("notify", true);
    }

}