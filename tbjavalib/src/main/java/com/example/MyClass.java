package com.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyClass {

    public static void main(String[] args) throws InterruptedException {

        // IFR Points
       parseCoordinates("555806N 0095940E");

        // Obstacle formats
       parseCoordinates("55 58 06N 009 59 40E");
        parseCoordinates("55 58 06.00N 009 59 40.00E");

        parseCoordinates("55 58 06 00N 009 59 40 00E");
    }



    public static void parseCoordinates(String line) {
        Pattern pattern = Pattern.compile("(.*?)N (.*)E");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String lat = matcher.group(1);
            String lon = matcher.group(2);
            System.out.println(String.format("%s -> lat: %f lon: %f", parseComponent(lat), parseComponent(lon)));
        } else {
            System.out.println("Something went wrong...");
        }
    }

    private static double parseComponent(String component) {
        String[] parts = component.split(" ");
        int dd = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        Double ss = Double.parseDouble(parts[2]);

        return  dd + mm / 60. + ss / 3600.;
    }

}


