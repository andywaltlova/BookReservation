package com.example.bookreservations.utils;

import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.bookreservations.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;


public class JsonAPIparser {
    private String apiUrl;
    private RequestQueue requestQueue;
    private Notifications notifications;

    private TextView text_view_order;
    private TextView text_view_signatures;
    private TextView text_view_barcode;
    private TextView text_view_time;
    private TextView text_view_date;
    private TextView empty_view;
    private TextView test_view;

    private String[] last_signature = {""};
    private String[] signature = {""};


    public JsonAPIparser(View root, RequestQueue queue, String apiUrl, Notifications notifications) {
        this.apiUrl = apiUrl;
        this.requestQueue = queue;
        this.notifications = notifications;

        text_view_order = root.findViewById(R.id.text_view_order);
        text_view_signatures = root.findViewById(R.id.text_view_signatures);
        text_view_barcode = root.findViewById(R.id.text_view_barcode);
        text_view_time = root.findViewById(R.id.text_view_time);
        text_view_date = root.findViewById(R.id.text_view_date);
        empty_view = root.findViewById(R.id.empty);
        test_view = root.findViewById(R.id.test_view);
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

    private void clearTextViews() {
        test_view.setText("");
        text_view_order.setText("");
        text_view_signatures.setText("");
        text_view_barcode.setText("");
        text_view_time.setText("");
        text_view_date.setText("");
    }

    private void sendNotification(String last_signature, String new_signature) {
        if (!last_signature.equals(new_signature)) {
            String textMessage = "Signatura: " + parseSignature(new_signature);
            notifications.sendNotification("Nová rezervace", textMessage);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private LocalTime parseTime(String time_str) {
        String time = String.format(Locale.getDefault(), "%04d", Integer.parseInt(time_str));
        time = time.replaceAll("..(?!$)", "$0:");
        return LocalTime.parse(time);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private LocalDate parseDate(String date_str) {
        String date = date_str.replaceAll("^....(?!$)", "$0-");
        date = date.replaceAll("^.......(?!$)", "$0-");
        return LocalDate.parse(date);
    }

    private String colorTime(LocalTime time, long d_minutes) {
        String result;
        if (d_minutes > 10 && d_minutes < 20)
            result = "<font color=#FF8C00>" + time + "</font>" + "\n";
        else if (d_minutes > 0 && d_minutes < 10)
            result = "<font color=#9ACD32>" + time + "</font>" + "\n";
        else
            result = "<font color=#DC143C>" + time + "</font>" + "\n";
        return result;
    }

    private void addResponseDataToViews(int order, String barcode, LocalDate date, String time) {
        text_view_time.setText(Html.fromHtml(time, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        text_view_order.append(String.format(Locale.getDefault(), "%02d", order) + ". \n");
        text_view_signatures.append(parseSignature(signature[0]) + "\n");
        text_view_barcode.append(barcode + "\n");
        text_view_date.append(date.toString() + "\n");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void parseResponse(JSONArray response) {
        String[] timeColumn = {""};
        int order = 1;
        for (int i = response.length() - 1; i >= 0; i--) {
            try {
                JSONArray jsonArray = response.getJSONArray(i);

                LocalDate date = parseDate(jsonArray.getString(0));
                LocalTime time = parseTime(jsonArray.getString(1));
                LocalTime now = LocalTime.now();

                long d_minutes = time.until(now, ChronoUnit.MINUTES);

                timeColumn[0] += colorTime(time, d_minutes);

                String barcode = jsonArray.getString(3);

                if (jsonArray.getString(4).equals("SKLAD")) {
                    signature[0] = jsonArray.getString(6);
                    addResponseDataToViews(order, barcode, date, timeColumn[0]);
                }
                order++;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void jsonParse() {
        clearTextViews();
        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        sendNotification(last_signature[0], signature[0]);

                        // Aktualizovat posledni signaturu tou nejnovejsi - kvuli notifikaci
                        last_signature[0] = signature[0];

                        if (response.length() == 0)
                            empty_view.setText("Nic k vyhledání :)");
                        else
                            empty_view.setText("");
                            parseResponse(response);
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
