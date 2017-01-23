package com.example;

public class MyClass {
    public static void main(String[] args){
       System.out.println(calcWCA(100., 180., 270., 20.));
    }


    static double calcWCA(double tas, double TT, double windDirection, double windStrength) {
        return Math.toDegrees(Math.atan(windStrength*Math.sin(Math.toRadians(windDirection-TT))/tas));
    }
}


