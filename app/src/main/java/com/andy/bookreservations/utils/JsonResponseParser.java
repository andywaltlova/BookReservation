package com.andy.bookreservations.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.andy.bookreservations.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class JsonResponseParser {
    private String apiUrl;
    private RequestQueue requestQueue;
    private Notifications notifications;

    private TextView message_view;
    private TextView book_view;
    private BookRequest last_request;
    private BookRequest new_request;


    public JsonResponseParser(View root, RequestQueue queue, String apiUrl, Notifications notifications) {
        this.apiUrl = apiUrl;
        this.requestQueue = queue;
        this.notifications = notifications;
        book_view = root.findViewById(R.id.book_view);
        message_view = root.findViewById(R.id.message);
    }

    // Kvůli špatné češtině v API je třeba přepsat neznámé znaky
    private String parseSignature(String s) {
        if (s.contains(":")) {
            int index = s.indexOf(":");
            String[] parts = s.split(":");
            String part2 = parts[1];
            if (index == 1)
                return "Č:" + part2;
            else
                return "ČA:" + part2;
        }
        return s;
    }

    private void shouldSendNotification(BookRequest last_signature, BookRequest new_signature) {
        if (!new_signature.equals(last_signature)) {
            String textMessage = "Location number: " + new_signature.getLocation_number();
            notifications.sendNotification("New request", textMessage);
        }
    }

    private LocalTime parseTime(String time_str) {
        String time = String.format(Locale.getDefault(), "%04d", Integer.parseInt(time_str));
        time = time.replaceAll("..(?!$)", "$0:");
        return LocalTime.parse(time);
    }

    private String parseDate(String date_str) {
        String date = date_str.replaceAll("^....(?!$)", "$0-");
        date = date.replaceAll("^.......(?!$)", "$0-");
        return LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd-MM-yy"));
    }

    private String getColorBasedOnRemainingTime(LocalTime time) {
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Prague"));
        long d_minutes = ChronoUnit.MINUTES.between(time, now);

        if (d_minutes > 10 && d_minutes < 20)
            return "#009688";
        else if (d_minutes > 0 && d_minutes < 10)
            return "#FF9800";
        else
            return "#DC143C";
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void parseResponse(JSONArray response) {
        List<BookRequest> books = new ArrayList<>();

        int order = 1;
        for (int i = response.length() - 1; i >= 0; i--) {
            try {
                JSONArray jsonArray = response.getJSONArray(i);
                BookRequest new_book = new BookRequest(parseSignature(jsonArray.getString(6)));
                new_book.setOrder(order);
                new_book.setBarcode(jsonArray.getString(3));
                new_book.setDate(parseDate(jsonArray.getString(0)));

                LocalTime time = parseTime(jsonArray.getString(1));
                new_book.setTime(time);

                if (jsonArray.getString(4).equals("SKLAD")) {
                    books.add(new_book);
                    new_request = new_book;
                }
                order++;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        book_view.setText("");
        book_view.setTypeface(Typeface.MONOSPACE);
        for (BookRequest book : books) {
            SpannableString book_span = book.getColoredString(getColorBasedOnRemainingTime(book.getTime()));
            book_view.setText(builder.append(book_span), TextView.BufferType.SPANNABLE);
        }
    }

    public void jsonParse() {
        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            message_view.setTextColor(Color.parseColor("#009688"));
                            String thumbs_up_emoji = new String(Character.toChars(0x1F44D));
                            message_view.setText("All work is done\n\n" + thumbs_up_emoji);
                            if (notifications.isDoneNotif())
                                notifications.sendNotification("You're the best!", "All work is done " + thumbs_up_emoji);
                        } else {
                            message_view.setText("");
                            parseResponse(response);
                            shouldSendNotification(last_request, new_request);
                            last_request = new_request;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        requestQueue.add(request);
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
