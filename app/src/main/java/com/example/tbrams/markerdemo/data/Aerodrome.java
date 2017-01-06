package com.example.tbrams.markerdemo.data;


import com.google.android.gms.maps.model.LatLng;

public class Aerodrome {
    private String name;
    private LatLng position;

    public Aerodrome(String name, String position) {
        this.name = name;
        this.position = convertVFG(position);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }


    // Convert format used in VFG Denmark ADC to internal location form
    // For example "57 05 34.04N 009 50 56.99E"
    private LatLng convertVFG(String input) {
            String[] parts=input.split(" ");
            parts[2]=parts[2].substring(0,5);
            parts[5]=parts[5].substring(0,5);

            Double lat = Double.parseDouble(parts[0])+Double.parseDouble(parts[1])/60+Double.parseDouble(parts[2])/3600;
            Double lon = Double.parseDouble(parts[3])+Double.parseDouble(parts[4])/60+Double.parseDouble(parts[5])/3600;

        return new LatLng(lat, lon);
    }


    @Override
    public String toString() {
        return "Aerodrome{" +
                "name='" + name + '\'' +
                ", position=" + position +
                '}';
    }
}
