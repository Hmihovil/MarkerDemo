package com.example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyClass {
    public static void main(String[] args){


        String hello="Hello World....";

        Time mTime = new Time();
        System.out.println(hello+"it is now: "+ mTime.getZuluTime());
    }


    private static String getTime() {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        date.setTimeZone(TimeZone.getTimeZone("GMT"));

        String zuluTime = date.format(currentLocalTime);
        return zuluTime;
    }

}

class Time {
    private static Calendar mCalendar;
    private static DateFormat mDateFormat;

    Time() {
        mCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        mDateFormat = new SimpleDateFormat("HH:mm");
        System.out.println("Time Constructor done");

    }

    public String getZuluTime() {
        Date currentLocalTime = mCalendar.getTime();
        mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String zuluTime = mDateFormat.format(currentLocalTime);

        return zuluTime;
    }
}


