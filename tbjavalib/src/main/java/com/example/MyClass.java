package com.example;

public class MyClass {
    public static void main(String[] args){
       System.out.println(convertVFG("57 05 34.04N 009 50 56.99E"));
    }

    // For example "57 05 34.04N 009 50 56.99E"
    static String convertVFG(String input) {
        String[] parts=input.split(" ");
        parts[2]=parts[2].substring(0,5);
        parts[5]=parts[5].substring(0,5);
        for (int i = 0; i < parts.length; i++) {
            System.out.println("part "+i+": "+parts[i]);
        }

        Double lat = Double.parseDouble(parts[0])+Double.parseDouble(parts[1])/60+Double.parseDouble(parts[2])/3600;
        Double lon = Double.parseDouble(parts[3])+Double.parseDouble(parts[4])/60+Double.parseDouble(parts[5])/3600;

        return "("+lat+", "+lon+")";
    }
}


