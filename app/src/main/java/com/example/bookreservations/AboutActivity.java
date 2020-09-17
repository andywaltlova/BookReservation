package com.example.bookreservations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        String text = "Application is used to monitor requests for books that are in stocks of Central Library of the Faculty of Arts at Masaryk University.\n" +
                "\n" +
                "When a new request for a book appears, the application sends a notification containing the location number of the book, which makes it easier to find it in the library stocks.\n" +
                "\n" +
                "The purpose of this application is to make library work more efficient. Now there is no need to walk around the printer and check if a new request is printed. As a library, we guarantee 20 minutes to process each book request, so it's important for staff to find out about new requests as soon as possible, regardless of where they are in the library.\n" +
                "\n" +
                "The image below illustrates the API response that is used to retrieve current book reservations. For our purposes, the application uses the time, date, barcode and location number fields.";
        TextView about_text = findViewById(R.id.about_text);
        about_text.setText(text);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}