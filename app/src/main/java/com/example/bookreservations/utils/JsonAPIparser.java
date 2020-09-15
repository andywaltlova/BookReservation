package com.example.bookreservations.utils;

import android.app.Activity;
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

import java.time.LocalTime;
import java.util.Locale;

//TODO: Needs serious refactoring
@RequiresApi(api = Build.VERSION_CODES.M)
public class JsonAPIparser {
    private String apiUrl;
    private RequestQueue mQueue;
    private Notifications notifications;

    private TextView text_view_order;
    private TextView text_view_signatures;
    private TextView text_view_barcode;
    private TextView text_view_time;
    private TextView text_view_date;
    private TextView empty_view;
    private TextView test_view;

    private String[] last_signature = {"-"};
    private String[] signature = {"-"};


    public JsonAPIparser(Activity activity, View root, RequestQueue queue, String apiPassword) {
        this.apiUrl = "http://knihomol.phil.muni.cz/cgi-bin/alephsql.exe?psw=" + apiPassword + "&req=requests2&fmt=json";
        this.mQueue = queue;
        this.notifications = new Notifications(activity);

        text_view_order = root.findViewById(R.id.text_view_order);
        text_view_signatures = root.findViewById(R.id.text_view_signatures);
        text_view_barcode = root.findViewById(R.id.text_view_barcode);
        text_view_time = root.findViewById(R.id.text_view_time);
        text_view_date = root.findViewById(R.id.text_view_date);
        empty_view = root.findViewById(R.id.empty);
        test_view = root.findViewById(R.id.test_view);
    }

    // Kvůli češtině v jsonu je třeba nerozpoznaný znak nahradit Č nebo ČA
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

    public void jsonParse() {
        text_view_order.setText("");
        text_view_signatures.setText("");
        text_view_barcode.setText("");
        text_view_time.setText("");
        text_view_date.setText("");


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONArray>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONArray response) {
                String[] text = {""};

                test_view.setText("");

                if (!last_signature[0].equals(signature[0])) {
                    String textMessage = "Signatura: " + parseSignature(signature[0]);
                    notifications.sendNotification("Nová rezervace", textMessage);
                }

                test_view.append("Předposlední: " + parseSignature(last_signature[0]) + "\n");
                last_signature[0] = signature[0];
                int order = 0;

                if (response.length() == 0)
                    empty_view.append("Nic k vyhledání :)" + "\n");
                else {
                    empty_view.setText("");
                    for (int i = response.length() - 1; i >= 0; i--) {
                        try {
                            JSONArray jsonArray = response.getJSONArray(i);

                            String date_raw = jsonArray.getString(0);
                            String date_first_slash = date_raw.replaceAll("^....(?!$)", "$0/");
                            String date = date_first_slash.replaceAll("^.......(?!$)", "$0/");

                            String time_raw = jsonArray.getString(1);
                            int time_int = Integer.parseInt(time_raw);
                            String time_4d = String.format(Locale.getDefault(), "%04d", time_int);
                            String time_colon = time_4d.replaceAll("..(?!$)", "$0:");
                            LocalTime time = LocalTime.parse(time_colon);
                            LocalTime now = LocalTime.now();
                            int time_minutes = time.getMinute();
                            int now_minutes = now.getMinute();
                            int time_hours = time.getHour();
                            int now_hours = now.getHour();
                            int d_hours = Math.abs(now_hours - time_hours);
                            int d_minutes = Math.abs(now_minutes - time_minutes);

                            //String uco = jsonArray.getString(2);
                            String barcode = jsonArray.getString(3);
                            String sbirka = jsonArray.getString(4);
                            //String patro = jsonArray.getString(5);
                            //String popis = jsonArray.getString(7);
                            //String status = jsonArray.getString(8);
                            //String nazev = jsonArray.getString(9);

                            if (sbirka.equals("SKLAD")) {
                                signature[0] = jsonArray.getString(6);

                                //TODO: Color only with today dates
                                if (d_hours == 0) {
                                    if (d_minutes > 10 && d_minutes < 20) {
                                        text[0] += "<font color=#FF8C00>" + time + "</font>" + "\n";
                                    } else if (d_minutes > 0 && d_minutes < 10) {
                                        text[0] += "<font color=#9ACD32>" + time + "</font>" + "\n";
                                    } else {
                                        text[0] += "<font color=#DC143C>" + time + "</font>" + "\n";
                                    }

                                } else {
                                    text[0] += "<font color=#DC143C>" + time + "</font>" + "\n";

                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    text_view_time.setText(Html.fromHtml(text[0], Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                                } else {
                                    text_view_time.setText(Html.fromHtml(text[0]), TextView.BufferType.SPANNABLE);
                                }
                                if (order < 10) {
                                    text_view_order.append("0" + order + ". " + "\n");
                                    //text_view_time.append(time + "\n");
                                } else {
                                    text_view_order.append(order + ". " + "\n");
                                    //text_view_time.append(time + "\n");
                                }
                                text_view_signatures.append(parseSignature(signature[0]) + "\n");
                                text_view_barcode.append(barcode + "\n");
                                text_view_date.append(date + "\n");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        order++;
                    }
                }
                test_view.append("Nejnovější: " + parseSignature(signature[0]));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }
}
