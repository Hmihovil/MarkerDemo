package com.example.tbrams.markerdemo;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.NavAids;
import com.example.tbrams.markerdemo.data.Pejling;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;
import static com.google.maps.android.SphericalUtil.interpolate;

public class MarkerDemoActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final int REQUEST_M_ID=1;
    private static final float ZOOM_DETAIL = 14.5f;
    private static final float ZOOM_OVERVIEW = 10.0f;
    private static final float ZOOM_NORMAL = 12.5f;

    private  MarkerObject mUndoDeleteMarker = null;
    private  int mUndoDeleteIndex= -1;

    private static GoogleMap mMap;
    MarkerLab markerLab = MarkerLab.getMarkerLab(this);
    List<MarkerObject> markerList = markerLab.getMarkers();
    NavAids navaids = NavAids.get(this);
    List<NavAid> vorList = navaids.getList();

    private static List<Marker> midpointList = new ArrayList<>();
    private static Polyline polyline;
    private static int currentMarkerIndex=-1;

    @Override
    protected void onResume() {
        super.onResume();
        if (polyline!=null) {
            updatePolyline();
        }
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
                Snackbar.make(view, "This is where we go to the flight plan", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    /*
     * Called by getMapAsync when ready
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        // Disable the navigation toolbar that will otherwise pop up after setting a marker
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Plot VOR navigation aids on the map
        for (int i=0;i<vorList.size();i++) {
            LatLng position = vorList.get(i).getPosition();
            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.ic_vor_blue))
                    .position(position,240f,240f);

            mMap.addGroundOverlay(newarkMap);
        }



        // TODO: consider making the nav aids icons visible at all zooms here, for now just change to
        // hybrid above zoom level 16
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = mMap.getCameraPosition();
                if(cameraPosition.zoom > 16.0) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });


        if (mMap!=null) {

            // This is where the Way Point data is shown and can be edited if clicked
            mMap.setOnInfoWindowClickListener(this);
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

                    tvLat.setText("Lat: " +String.format("%.4f", latLng.latitude));
                    tvLng.setText("Lon: " +String.format("%.4f", latLng.longitude));
                    tvSnippet.setText(marker.getSnippet());

                    return v;

                }
            });
        }


        // Long clicking on the map adds a way point
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
            @Override
            public void onMapLongClick(LatLng latLng) {
                MarkerDemoActivity.this.addMarker(latLng);
            }

        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currentMarkerIndex=getIndexById(marker.getId());

                if (isMidPoint(marker)) {
                    int pp=getMidpointIndex(marker);
                    addMarker(marker.getPosition(), pp);

                    // exit without showing info window
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
                updateMarkerInfo(marker);
                updatePolyline();
                updateNavinfo();
            }
        });

        LatLng EKRK = new LatLng(55.59,	12.13);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(EKRK,12));

    }

    /*
     * find new location text for existing marker - used when dragged
     */
    private void updateMarkerInfo(Marker marker) {

        Address adr = findAddress(marker.getPosition());
        marker.setTitle(adr.getLocality());
        marker.setSnippet(adr.getCountryName());
    }


    /*
     * This function is looking up a location and doing the geocoding.
     * Returning an address
     */
    private Address findAddress(LatLng location) {
        Geocoder gc = new Geocoder(MarkerDemoActivity.this);
        List<Address> list = null;

        try {
            list = gc.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address adr = list.get(0);
        return adr;
    }


    /*
     * Determine if the marker argument is a midpoint marker
     */
    private boolean isMidPoint(Marker marker) {
        return (getMidpointIndex(marker)>=0);
    }


    /*
     * Look up the marker in the midpointList and return the index. If not found return
     * minus one.
     */
    private static int getMidpointIndex(Marker marker) {
        for (int i=0;i<midpointList.size();i++){
            if (midpointList.get(i).getPosition().equals(marker.getPosition()))
                return i;
        }
        return -1;
    }



    /*
     * Find and return the marker index with the marker_id as specified
     * otherwise return minus one
     */
    public int getIndexById(String mid) {
        for (int i=0; i<markerList.size();i++) {
            if (markerList.get(i).getMarker().getId().equals(mid)) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Refresh polyline from marker coordinates
     *
     * This one is called after a new marker has been added, when a marker is dragged and
     * when the activity resumes
     */

    public void updatePolyline() {
        if (polyline==null) {
            PolylineOptions lineOptions = new PolylineOptions();
            polyline = mMap.addPolyline(lineOptions);
        }

        List<LatLng> points = new ArrayList<>();
        for (int i=0;i<markerList.size();i++){
            points.add(markerList.get(i).getMarker().getPosition());
        }
        polyline.setPoints(points);

        updateMidpoints();
    }

    /*
     * Update distance and heading info for all markers
     */
    public void updateNavinfo(){
        if (markerList.size()<2) {
            return;
        }

        for (int i=0;i<markerList.size()-1;i++){
            MarkerObject mFrom =markerList.get(i);
            MarkerObject mTo = markerList.get(i+1);

            double dist=computeDistanceBetween(mFrom.getMarker().getPosition(), mTo.getMarker().getPosition());
            double heading = computeHeading(mFrom.getMarker().getPosition(), mTo.getMarker().getPosition());

            mFrom.setDist(dist);
            mFrom.setTT(heading);
        }
    }

    /*
     *  Clear all physical traces of midpoints from the map, then go through all established
     *  markers again and calculate new locations for midpoints. Then add them to the map and
     *  to our midPointList
     */
    private void updateMidpoints() {
        if (markerList.size()<2) return;

        // remove old markers from map and clear the storage
        for (int i=0;i<midpointList.size();i++){
            midpointList.get(i).remove();
        }
        midpointList.clear();

        // Go through all markers and add new non draggable midpoint markers
        for (int i=0; i<markerList.size()-1;i++) {
            LatLng midPt = interpolate(markerList.get(i).getMarker().getPosition(), markerList.get(i+1).getMarker().getPosition(), 0.5);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(midPt)
                    .anchor((float)0.5, (float)0.5)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_circle)));

            midpointList.add(marker);
        }
    }


    /*
     * Find the three nearest VORs for the marker in question and return a list
     * with the sorted results
     */
    private ArrayList<Pejling> nearestVORs(Marker m) {

        ArrayList<Pejling> plist= new ArrayList<>();

        for (int i=0;i<vorList.size();i++) {
            LatLng position = vorList.get(i).getPosition();
            double dist=computeDistanceBetween(position, m.getPosition());
            double heading = computeHeading(position, m.getPosition());
            plist.add(new Pejling(i, dist, heading));
        }


        Collections.sort(plist);

        ArrayList<Pejling> result = new ArrayList<>();
        for (int i=0; i<3;i++) {
            result.add(plist.get(i));
        }

        return result;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /*
     * This is where we will get to when another activity is terminated. We shou;ld be able
     * to process a markerIndex along with a code indicating how we are supposed to act on it
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_M_ID) {
            int markerIndex = (int) data.getSerializableExtra(InfoEditFragment.EXTRA_MARKER_ID);
            Log.d("TBR:", "onActivityResult: Received markerIndex:"+markerIndex);

            if (resultCode == InfoEditFragment.ACTION_UPDATE || resultCode==InfoEditFragment.ACTION_CANCEL) {
                Marker m=markerList.get(markerIndex).getMarker();
                gotoLocation(m.getPosition(),ZOOM_NORMAL);

            } else if (resultCode == InfoEditFragment.ACTION_DELETE ) {
                Log.d("TBR:", "onActivityResult: delete markerIndex: "+markerIndex);

                // Prepare for undoing later by storing both marker and position in track
                mUndoDeleteIndex = markerIndex;
                mUndoDeleteMarker = new MarkerObject();
                mUndoDeleteMarker = markerList.get(markerIndex);

                // Delete physical marker and then the MarkerObject
                markerList.get(markerIndex).getMarker().remove();
                markerList.remove(markerIndex);
                updatePolyline();

                // Make a snackbar message offering undo
                Snackbar.make(findViewById(R.id.map), "Marker deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int previousIndex = mUndoDeleteIndex-1;
                                if (previousIndex>=0) {
                                    addMarker(mUndoDeleteMarker.getMarker().getPosition(), previousIndex);
                                } else {
                                    addMarker(mUndoDeleteMarker.getMarker().getPosition());
                                }
                                // reset undo variables
                                mUndoDeleteMarker=null;
                                mUndoDeleteIndex=-1;
                            }
                        })
                        .setActionTextColor(Color.RED)
                        .show();
            }
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
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


    private void gotoLocation(LatLng loc, float zoom) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(loc, zoom);
        mMap.moveCamera(update);
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /*
     * Search button is pressed
     *
     * Look up a place name and find the coordinates, then pass the coordinates to
     * the normal appending addMarker
     */
    public void geoLocate(View v) throws IOException {

        hideSoftKeyboard(v);

        TextView tv = (TextView) findViewById(R.id.editText1);
        String searchString = tv.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list.size() > 0) {
            Address adr = list.get(0);
            String locality = adr.getLocality();


            double lat = adr.getLatitude();
            double lng = adr.getLongitude();
            LatLng location = new LatLng(lat, lng);

            addMarker(location);
        }
    }


    /*
     * This one is used when adding a marker from text geocoding and
     * from long pressing the map
     *
     * Creates the physical marker on the map and appends a MarkerObject on the markerList
     */
    private void addMarker(LatLng loc) {

        MarkerOptions options = createMarkerOptions(loc);
        Marker marker = mMap.addMarker(options);
        gotoLocation(loc, ZOOM_NORMAL);


        ArrayList<Pejling> pejlinger = new ArrayList<>();
        pejlinger = nearestVORs(marker);

        MarkerObject mo = new MarkerObject(marker, marker.getTitle(), marker.getSnippet(), pejlinger);
        markerList.add(mo);

        updatePolyline();
        updateNavinfo();

    }


    /*
     * This one is used when adding a marker from midpoint dragging where we need to
     * insert a point in between other points instead of just appending
     *
     */
    private void addMarker(LatLng loc, int afterThis) {

        MarkerOptions options = createMarkerOptions(loc);
        Marker marker = mMap.addMarker(options);

        ArrayList<Pejling> pejlinger = new ArrayList<>();
        pejlinger=nearestVORs(marker);

        MarkerObject mo = new MarkerObject(marker, marker.getTitle(), marker.getSnippet(), pejlinger);

        markerList.add(afterThis+1, mo);
        updatePolyline();
        updateNavinfo();
    }


    /*
    * Used for all add marker functions
    * Create the options needed for a new marker
    */
    private MarkerOptions createMarkerOptions(LatLng loc) {
        Address adr = findAddress(loc);

        String text = adr.getLocality();
        MarkerOptions options = new MarkerOptions()
                .title(text)
                .draggable(true)
                .position(loc);

        String country = adr.getCountryName();
        if (country.length() > 0) {
            options.snippet(country);
        }

        return options;

    }


    /*
    * When the info window is clicked, we will launch the NavPagerActivity
    * allowing us to edit texts or delete a marker. To achieve that we pass
    * along the currentMarkerIndex (index of the last clicked marker)
    */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = NavPagerActivity.newIntent(this, currentMarkerIndex);
        startActivityForResult(intent, REQUEST_M_ID);
        marker.hideInfoWindow();
    }


}
