package com.example.tbrams.markerdemo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Time {
    private static Calendar mCalendar;
    private static DateFormat mDateFormat;

    Time() {
        mCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        mDateFormat = new SimpleDateFormat("HH:mm");

    }

    public String getZuluTime() {
        Date currentLocalTime = mCalendar.getTime();
        mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String zuluTime = mDateFormat.format(currentLocalTime);

        return zuluTime;
    }
}
