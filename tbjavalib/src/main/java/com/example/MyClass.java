package com.example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyClass {

    public static void main(String[] args) throws InterruptedException {

       System.out.println(getZuluTime());
    }


    // This one returns the important part of the timestamp in the format "hh:mm"
    public static String getZuluTime() {
        Calendar mCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date currentLocalTime = mCalendar.getTime();
        mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String zuluTime = mDateFormat.format(currentLocalTime);

        return zuluTime;
    }
}


