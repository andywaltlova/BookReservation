package com.andy.bookreservations.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Class represents one book request or book reservation that is retrieved from API
 */
public class BookRequest {
    private int order;
    private String location_number;
    private String barcode;
    private LocalTime time;
    private String date;

    public BookRequest(String location_number) {
        this.location_number = location_number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookRequest)) return false;
        BookRequest that = (BookRequest) o;
        return getBarcode().equals(that.getBarcode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBarcode());
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s  %s  %-9s  %s", date, time, location_number, barcode);
    }

    /**
     * Return Colored String, important for user to know remaining time to process book request
     * (Color indicates remaining time)
     *
     * @param color String representation of color code
     * @return Span string colored with given color
     */
    public SpannableString getColoredString(String color) {
        SpannableString str = new SpannableString(this.toString() + "\n");
        str.setSpan(new ForegroundColorSpan(Color.parseColor(color)), 0, str.length(), 0);
        return str;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getLocation_number() {
        return location_number;
    }

    public void setLocation_number(String location_number) {
        this.location_number = location_number;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
