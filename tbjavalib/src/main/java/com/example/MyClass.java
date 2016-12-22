package com.example;

public class MyClass {
    public static void main(String[] args){

        MagneticModel mM = new MagneticModel();
        mM.setLocation(55.32,	10.57);

        System.out.println("Declination is: "+mM.getDeclination());
        System.out.println("Inclination is: "+mM.getInclination());
        System.out.println("Z Field vector Downwards: "+mM.getFieldVectorDownwards());
        System.out.println("Y Field vector Eastern: "+mM.getFieldVectorEastern());
        System.out.println("X Field vector Northen is: "+mM.getFieldVectorNorthern());
        System.out.println("Field Strength is: "+mM.getFieldStrength());
        System.out.println("Horizontal Field strength is: "+mM.getHorizontalFieldStrength());
        System.out.println("Grivation is: "+mM.getGrivation());

    }
}


