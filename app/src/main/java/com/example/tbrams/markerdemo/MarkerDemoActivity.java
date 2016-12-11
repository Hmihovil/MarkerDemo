package com.example.tbrams.markerdemo;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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

public class MarkerDemoActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final int MAX_MARKERS=3;
    private GoogleMap mMap;
    private static List<MarkerObject> markerList = new ArrayList<>();
    private static Polyline polyline;
    public static String currentMarkerId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_demo);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                String msg = marker.getTitle() + " (" +
                        marker.getPosition().latitude + ", " +
                        marker.getPosition().longitude + ")";

                // Get markerID as a global variable - we need this for the edit intent
                currentMarkerId=marker.getId();
                Log.d("TBR","Marker "+currentMarkerId+" clicked at "+msg);

                // returning false here will show the info window automatically - default behavior
                return false;
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                currentMarkerId=marker.getId();
                Log.d("TBR","Marker "+currentMarkerId+" drag started ");

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

                dumpMarkerList();

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


    public static void updatePolyline() {
        List<LatLng> points= new ArrayList<>();
        for (MarkerObject mo: markerList) {
            points.add(mo.getMarker().getPosition());
        }
        polyline.setPoints(points);
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
        if (markerList.size()==MAX_MARKERS) {
            removeAllMarkers();
        }

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

        dumpMarkerList();

        updateLine(lat, lng);
    }

    public void dumpMarkerList() {
        Log.d("TBR","markerList dump:");
        for (int i=0;i<markerList.size();i++) {
            Log.d("TBR","markerList["+i+"]: "+markerList.get(i).getText());
        }
    }


    private void updateLine(double lat, double lng) {

        if (polyline==null) {
            PolylineOptions lineOptions = new PolylineOptions();
            polyline = mMap.addPolyline(lineOptions);
        }

        List<LatLng> points = polyline.getPoints();
        points.add(new LatLng(lat, lng));
        polyline.setPoints(points);
    }


    private void removeAllMarkers() {
        for (MarkerObject mo : markerList) {
            mo.getMarker().remove();
        }
        markerList.clear();

        if (polyline!=null){
            polyline.remove();
            polyline=null;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d("TB","Info window clicked");

        Intent intent = new Intent(this, InfoEditActivity.class);
        startActivity(intent);
        marker.hideInfoWindow();

    }

    public static void DeleteMarker(String mId) {
        for (int i=0;i<markerList.size();i++) {
            if (markerList.get(i).getMarker().getId().equals(mId)) {
                markerList.get(i).getMarker().remove();
                markerList.remove(i);
                Log.d("TBR", "Marker index "+i+ " removed");

                updatePolyline();

                return;
            }
        }

    }

    public static void updateMarker(String mId, String tit, String snp) {
        for (int i=0;i<markerList.size();i++) {
            if (markerList.get(i).getMarker().getId().equals(mId)) {
                markerList.get(i).setText(tit);
                markerList.get(i).setSnippet(snp);
                markerList.get(i).getMarker().setTitle(tit);
                markerList.get(i).getMarker().setSnippet(snp);
                Log.d("TBR", "Marker index "+i+ " updated");

                return;
            }
        }

    }

}
