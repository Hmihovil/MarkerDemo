package com.example;

public class MyClass {

    public static void main(String[] args) throws InterruptedException {

        System.out.println(parseFrom("FL55"));
        System.out.println(parseFrom("GND"));
        System.out.println(parseFrom("2300"));

    }

    /**
     * Parse the different variation of altitude notations.
     *
     * @param input String  For example "FL 500", "GND" or "2300"
     * @return      int     The altitude
     *
     */
    public static int parseFrom(String input) {
        int result=0;
        if (input != null) {
            if (input.toUpperCase().equals("GND")) {
                result=0;
            } else {
                if (input.indexOf("FL")>=0) {
                    result = Integer.parseInt(input.replace("FL",""))*100;
                } else  {
                    result = Integer.parseInt(input);
                }
            }
        }

        return result;
    }

}


