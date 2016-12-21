package com.example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyClass {
    public static void main(String[] args){

        MagneticModel mM = new MagneticModel();
        double[] result = mM.getDeclination(0, 55.5853306815137, 12.12890625, 2016.97);
        printArray(result);

    }

    public static void printArray(double[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
    }

 /*
 * get the precise decimal notation year for the magnetic declination calculations
 * @param None
 *
 * @return  decimal_fraction_year
 *
 * @customfunction

    public double getDecimalYear() {
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        int year = now.get(Calendar.YEAR);       // The current year
//        now.getWeekYear();
        Date n1=new Date(year, 0, 1);
        Date d1=new Date(year, 11, 31);
        Date d2=new Date(year-1, 11, 31);

        return year+(t-n1)/(d1-d2);
    }
 */
}


