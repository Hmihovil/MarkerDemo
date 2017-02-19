package com.example;

public class MyClass {

    public static void main(String[] args) throws InterruptedException {

        System.out.println(parseFrom("FL55"));
        System.out.println(parseFrom("GND"));
        System.out.println(parseFrom("2300"));
        System.out.println(parseFrom("2300ft"));
        System.out.println(parseFrom("2300  ft"));

    }

    /**
     * Parse the different variation of altitude notations.
     *
     * @param input String  For example "FL 500", "GND", "2300" or "2300 ft"
     * @return int     The altitude
     */
    public static int parseFrom(String input) {
        input = input.trim().toUpperCase();
        int result = 0;
        boolean flightLevelUnits = false;

        if (input != null) {
            if (input.toUpperCase().equals("GND")) {
                result = 0;
            } else {
                // First get rid of Feet unit if there at all
                input=input.replace("FT", "").trim();
                if (input.indexOf("FL") >= 0) {
                    flightLevelUnits = true;
                    input = input.replace("FL", "");
                }

                try {
                    result = Integer.parseInt(input);
                    if (flightLevelUnits) result*=100;

                } catch (NumberFormatException e) {
                    result = 0;
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
}

