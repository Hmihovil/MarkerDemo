package com.example.tbrams.markerdemo;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.maps.android.SphericalUtil.interpolate;

public class MarkerDemoActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static GoogleMap mMap;
    MarkerLab markerLab = MarkerLab.getMarkerLab(this);
    List<MarkerObject> markerList = markerLab.getMarkers();

    private static List<Marker> midpointList = new ArrayList<>();
    private static List<Integer> previousList = new ArrayList<>();
    private static Polyline polyline;
    public static String currentMarkerId = null;

    @Override
    protected void onResume() {
        super.onResume();
        updatePolyline();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_demo);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_work_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    // Called by getMapAsync when ready
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);

        if (mMap!=null) {
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);

                    TextView tvLocality = (TextView) v.findViewById(R.id.tvLocality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tvLat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tvLng);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.tvSnippet);

                    LatLng latLng = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: " +latLng.latitude);
                    tvLng.setText("Longitude: " +latLng.longitude);
                    tvSnippet.setText(marker.getSnippet());

                    return v;

                }
            });
        }



        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
            @Override
            public void onMapLongClick(LatLng latLng) {
                Geocoder gc = new Geocoder(MarkerDemoActivity.this);
                List<Address> list = null;

                try {
                    list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Address adr = list.get(0);
                MarkerDemoActivity.this.addMarker(adr, latLng.latitude, latLng.longitude);
            }

        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currentMarkerId=marker.getId();
                Log.d("TBR","Marker "+currentMarkerId+" clicked.");

                if (isMidPoint(marker)) {
                    Log.d("TBR", "This is a midpoint");
                    int pp=getMidpointIndex(marker);
                    // Add new marker here ( in a special color?)
                    addMarker(marker.getPosition(), pp);

                    // do not show info window
                    return true;
                } else {
                    // returning false - show the info window
                    return false;
                }

            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                updatePolyline();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d("TBR","Marker "+currentMarkerId+" drag ended");
                updateMarkerInfo(marker);
                updatePolyline();

                // marker.showInfoWindow();
/*
                double distance = SphericalUtil.computeDistanceBetween(marker1.getPosition(), marker2.getPosition());
                double bearing = SphericalUtil.computeHeading(marker1.getPosition(), marker2.getPosition());

                String unit = "m";
                if (distance < 1) {
                    distance *= 1000;
                    unit = "mm";
                } else if (distance > 1000) {
                    distance /= 1000;
                    unit = "km";
                }
                String msg =String.format("%4.3f%s", distance, unit)+" "+String.format("%4.0f%s", bearing, "°");

                Toast.makeText(MarkerDemoActivity.this, msg, Toast.LENGTH_SHORT).show();
 */
            }
        });

        LatLng EKRK = new LatLng(55.59,	12.13);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(EKRK,12));

    }

    /*
     * find new location text for existing marker - used when dragged
     */
    private void updateMarkerInfo(Marker marker) {
        Geocoder gc = new Geocoder(MarkerDemoActivity.this);
        List<Address> list = null;
        LatLng latLng = marker.getPosition();

        try {
            list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Address adr = list.get(0);
        marker.setTitle(adr.getLocality());
        marker.setSnippet(adr.getCountryName());
    }

    private void updateMidpoints() {
        if (markerList.size()<1) return;

        // remove old markers from map and clear the storage
        for (int i=0;i<midpointList.size();i++){
            midpointList.get(i).remove();
        }
        midpointList.clear();
        previousList.clear();

        // Go through all markers and add new non draggable midpoint markers
        for (int i=0; i<markerList.size()-1;i++) {
            LatLng midPt = interpolate(markerList.get(i).getMarker().getPosition(), markerList.get(i+1).getMarker().getPosition(), 0.5);
            Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(midPt)
                            .anchor((float)0.5, (float)0.5)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_circle)));

            midpointList.add(marker);
            previousList.add(i);
        }
    }

    private boolean isMidPoint(Marker marker) {
        return (getMidpointIndex(marker)>=0);
    }

    private static int getMidpointIndex(Marker marker) {
        for (int i=0;i<midpointList.size();i++){
            if (midpointList.get(i).getPosition().equals(marker.getPosition()))
                return i;
        }
        return -1;
    }

    public void updatePolyline() {
        if (polyline==null) return;

        List<LatLng> points= new ArrayList<>();
        for (MarkerObject mo: markerList) {
            points.add(mo.getMarker().getPosition());
        }
        polyline.setPoints(points);

        updateMidpoints();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    public void geoLocate(View v) throws IOException {

        hideSoftKeyboard(v);

        TextView tv = (TextView) findViewById(R.id.editText1);
        String searchString = tv.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list.size() > 0) {
            Address adr = list.get(0);
            String locality = adr.getLocality();
            Toast.makeText(this, "Found: " + locality, Toast.LENGTH_SHORT).show();

            double lat = adr.getLatitude();
            double lng = adr.getLongitude();
            gotoLocation(lat, lng, 15);

            addMarker(adr, lat, lng);
        }
    }


    /*
     * This one is used when adding a marker from text geocoding and
     * from long pressing the map
     */
    private void addMarker(Address adr, double lat, double lng) {

        String text = adr.getLocality();
        MarkerOptions options = new MarkerOptions()
                .title(text)
                .draggable(true)
                .position(new LatLng(lat, lng));

        String country = adr.getCountryName();
        if (country.length() > 0) {
            options.snippet(country);
        }

        Marker marker = mMap.addMarker(options);
        Log.d("TBR", "Marker with id: "+marker.getId()+" added");
        markerList.add(new MarkerObject(marker, text, country));

        updateLine(lat, lng);
        updateMidpoints();
    }

 /*
 * This one is used when adding a marker from midpoint dragging
 *
 */
    private void addMarker(LatLng loc, int afterThis) {

        Geocoder gc = new Geocoder(MarkerDemoActivity.this);
        List<Address> list = null;

        try {
            list = gc.getFromLocation(loc.latitude, loc.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Address adr = list.get(0);


        String text = adr.getLocality();
        MarkerOptions options = new MarkerOptions()
                .title(text)
                .draggable(true)
                .position(loc);

        String country = adr.getCountryName();
        if (country.length() > 0) {
            options.snippet(country);
        }

        Marker marker = mMap.addMarker(options);
        Log.d("TBR", "Marker with id: "+marker.getId()+" added");
        markerList.add(afterThis+1, new MarkerObject(marker, text, country));

        updateLine(loc.latitude, loc.longitude);
        updateMidpoints();
    }



    private void updateLine(double lat, double lng) {

        if (polyline==null) {
            PolylineOptions lineOptions = new PolylineOptions();
            polyline = mMap.addPolyline(lineOptions);
        }

        List<LatLng> points = new ArrayList<>();
        for (int i=0;i<markerList.size();i++){
            points.add(markerList.get(i).getMarker().getPosition());
        }
        polyline.setPoints(points);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

        Intent intent = new Intent(this, InfoEditActivity.class);
        startActivity(intent);
        marker.hideInfoWindow();
    }


}
