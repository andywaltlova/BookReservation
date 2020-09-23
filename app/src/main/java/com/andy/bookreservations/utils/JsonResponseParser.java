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
import java.time.LocalDateTime;
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
    private TextView messageView;
    private TextView bookView;
    private BookRequest last_request;
    private BookRequest new_request;


    public JsonResponseParser(View root, RequestQueue queue, String apiUrl, Notifications notifications) {
        this.apiUrl = apiUrl;
        this.requestQueue = queue;
        this.notifications = notifications;
        bookView = root.findViewById(R.id.book_view);
        messageView = root.findViewById(R.id.message);
    }

    /**
     * Because of wrong character encoding in API some czech chars
     * in book location number needs to be fixed
     * Currently there is only one czech char (č) in book location number
     *
     * @param s string to be replaced
     * @return string with fixed czech chars
     */
    private String parseLocationNumber(String s) {
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

    /**
     * Sends notification only if new book request was made (i.e last request is different than new)
     *
     * @param lastRequest last book request (from latest response)
     * @param newRequest  new book request from currently processed response
     */
    private void shouldSendNotification(BookRequest lastRequest, BookRequest newRequest) {
        if (!newRequest.equals(lastRequest)) {
            String textMessage = "Location number: " + newRequest.getLocation_number();
            notifications.sendNotification("New request", textMessage);
        }
    }

    private LocalTime parseTime(String time_str) {
        String time = String.format(Locale.getDefault(), "%04d", Integer.parseInt(time_str));
        time = time.replaceAll("..(?!$)", "$0:");
        return LocalTime.parse(time);
    }

    /**
     * Parse date that is missing delimiter between day,month and year (e.g 01122020)
     *
     * @param date_str string representing date
     * @return string representing date in format dd-MM-yy
     */
    private LocalDate parseDate(String date_str) {
        String date = date_str.replaceAll("^....(?!$)", "$0-");
        date = date.replaceAll("^.......(?!$)", "$0-");
        return LocalDate.parse(date);
    }

    /**
     * Returns color code for color based on remaining time to process given book request
     *
     * @param time time when request was made
     * @return string containing color code
     */
    private String getColorBasedOnRemainingTime(LocalTime time, LocalDate date) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Prague"));
        long diffMinutes = ChronoUnit.MINUTES.between(time, now.toLocalTime());
        long diffDays = ChronoUnit.DAYS.between(date, now.toLocalDate());

        if (diffMinutes > 20 || diffDays > 0) {
            return "#DC143C";
        } else if (diffMinutes > 10) {
            return "#FF9800";
        } else
            return "#009688";
    }

    /**
     * Parse data from response and then creates from them instances of BookRequest object.
     *
     * @param response JSONArray containing response from API
     */
    private void parseResponse(JSONArray response) {
        List<BookRequest> books = new ArrayList<>();

        int order = 1;
        for (int i = response.length() - 1; i >= 0; i--) {
            try {
                JSONArray jsonArray = response.getJSONArray(i);
                BookRequest new_book = new BookRequest(parseLocationNumber(jsonArray.getString(6)));
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
        addRequestsToTextView(books);
    }


    private void addRequestsToTextView(List<BookRequest> books) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        bookView.setText("");
        bookView.setTypeface(Typeface.MONOSPACE);
        for (BookRequest book : books) {
            SpannableString book_span = book.getColoredString(getColorBasedOnRemainingTime(book.getTime(), book.getDate()));
            bookView.setText(builder.append(book_span), TextView.BufferType.SPANNABLE);
        }
    }

    /**
     * Checks if response contains at least one request for book from stock.
     *
     * @param response JSONArray responde from API
     * @return True if there is at least one book request, False otherwise
     */
    private boolean isThereMoreRequestsForSKLAD(JSONArray response) {
        if (response.length() == 0)
            return false;
        for (int i = response.length() - 1; i >= 0; i--) {
            try {
                JSONArray jsonArray = response.getJSONArray(i);
                if (jsonArray.getString(4).equals("SKLAD"))
                    return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main function in JsonResponseParser class. Responsible for making request to API.
     */
    public void jsonParse() {
        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (!isThereMoreRequestsForSKLAD(response)) {
                            bookView.setText("");
                            messageView.setTextColor(Color.parseColor("#009688"));
                            String thumbs_up_emoji = new String(Character.toChars(0x1F44D));
                            messageView.setText("All work is done\n\n" + thumbs_up_emoji);
                            System.out.println(notifications.isWorkDoneNotif());
                            if (notifications.isWorkDoneNotif() && !notifications.isWorkDoneNotifSend()) {
                                notifications.sendNotification("You're the best!", "All work is done " + thumbs_up_emoji);
                                notifications.setWorkDoneNotifSend(true);
                            }
                        } else {
                            notifications.setWorkDoneNotifSend(false);
                            messageView.setText("");
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
