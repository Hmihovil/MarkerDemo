package com.example.tbrams.markerdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.Aerodromes;
import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.NavAids;
import com.example.tbrams.markerdemo.data.Pejling;
import com.example.tbrams.markerdemo.db.DataSource;
import com.example.tbrams.markerdemo.dbModel.TripItem;
import com.example.tbrams.markerdemo.dbModel.WpItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import java.util.Locale;

import static com.example.tbrams.markerdemo.TripAdapter.TRIP_KEY;
import static com.example.tbrams.markerdemo.TripAdapter.WP_KEY;
import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;
import static com.google.maps.android.SphericalUtil.interpolate;

public class MarkerDemoActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {

    private static final int REQUEST_M_ID=1;
    private static final int REQUEST_GLOBAL_VARS=2;
    private static final int SETTINGS_RESULT = 3;

    private static final float ZOOM_OVERVIEW = 10.0f;

    private static final String DEGREES="\u00B0";

    private  MarkerObject mUndoDeleteMarker = null;
    private  int mUndoDeleteIndex= -1;

    private static GoogleMap mMap;
    private SharedPreferences mSharedPrefs;
    private float mZoomLevel;
    private boolean mMapTypeChangedByZoom=false;


    private final MarkerLab markerLab = MarkerLab.getMarkerLab(this);
    private final List<MarkerObject> markerList = markerLab.getMarkers();

    private final NavAids navaids = NavAids.get(this);
    private final List<NavAid> vorList = navaids.getList();
    private final List<Marker> mNavAidMarkers = new ArrayList<>();
    private boolean mHideADicons = false;
    private boolean mHideNavAidIcons = false;


    private final Aerodromes aerodromes = Aerodromes.get(this);
    private final List<Aerodrome> ADList = aerodromes.getList();
    private final List<Marker> mADMarkers = new ArrayList<>();


    private final static List<Marker> midpointList = new ArrayList<>();
    private static Polyline polyline;
    private static int currentMarkerIndex=-1;

    private Vibrator mVib;
    private Object mSearchedFor;
    private static String mTripId;
    private String mWpId;
    private boolean mPlanUpdated=false;
    DataSource      mDataSource;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_demo);

        // make sure we have default values by running this first time a user launches the app
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Get trip and wp index from extra arguments
        mTripId = getIntent().getStringExtra(TRIP_KEY);
        mWpId   = getIntent().getStringExtra(WP_KEY);
        Log.d("TBR:","Received Trip id: " + mTripId +" and wp id: "+ mWpId);

        this.setTitle(markerLab.getTripName());

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updatePreferenceFlags();


        mVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                                                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);

        final EditText searchText = (EditText) findViewById(R.id.editText1);
        searchText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            try {
                                geoLocate(v);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });


        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    geoLocate(view);
                    searchText.requestFocus();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FAB);
        fab.setImageResource(R.drawable.ic_work_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Click here when you are done adding way points", Snackbar.LENGTH_LONG)
                        .setAction("Go!", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(MarkerDemoActivity.this, GlobalNavValActivity.class);
                                startActivityForResult(intent, REQUEST_GLOBAL_VARS);

                                Log.d("TBR:", "GlobalNavValActivity Started...");
                            }
                        }).show();
            }
        });


    }




    /*
     * Called by getMapAsync when ready
     */
    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        // Check map type preference and update the map here
        updateMapType();
        updateZoom();

        LatLng position = null;
        try {
            position = searchLocation(mSharedPrefs.getString("startPlace", "Roskilde airport, Denmark"));
        } catch (IOException e) {
            e.printStackTrace();
        }



        if (mMap!=null) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,mZoomLevel));

            // Disable the navigation toolbar that will otherwise pop up after setting a marker
            mMap.getUiSettings().setMapToolbarEnabled(false);

            // Enable my location button though
            //       mMap.getUiSettings().setMyLocationButtonEnabled(true);

            plotNavAids();
            plotAerodromes();


            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    CameraPosition cameraPosition = mMap.getCameraPosition();
                    mZoomLevel=cameraPosition.zoom;
                    Log.d("TBR:", "Camera Idle Listener, zoomlevel: "+ mZoomLevel);

                    writePreferenceChanges();

                    if(mZoomLevel> 16.0) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        mMapTypeChangedByZoom = true;
                    } else {
                        // Switch back to preferred map type after "forced" change due to zoom
                        if (mMapTypeChangedByZoom) {
                            updateMapType();
                            mMapTypeChangedByZoom = false;
                        }

                        // Update both Aerodromes and Navaid icons was well
                        plotNavAids();
                        plotAerodromes();
                    }
                }
            });


            // This is where the Way Point data is shown and can be edited if clicked
            mMap.setOnInfoWindowClickListener(this);
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    // check if a NavAid has been clicked
                    for (Marker m : mNavAidMarkers) {
                        if (marker.getId().equals(m.getId())) {
                            Log.d("TBR:","NavAid marker clicked");
                            return null;
                        }
                    }

                    // check if an Aerodrome has been clicked
                    for (Marker m : mADMarkers) {
                        if (marker.getId().equals(m.getId())) {
                            Log.d("TBR:","Aerodrome marker clicked");
                            return null;
                        }
                    }


                    View v = getLayoutInflater().inflate(R.layout.info_window, null);

                    TextView tvLocality = (TextView) v.findViewById(R.id.tvLocality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tvLat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tvLng);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.tvSnippet);
                    tvSnippet.setText(marker.getSnippet());

                    LatLng latLng = marker.getPosition();
                    tvLocality.setText(marker.getTitle());

                    tvLat.setText("Lat: " +String.format("%.4f", latLng.latitude));
                    tvLng.setText("Lon: " +String.format("%.4f", latLng.longitude));

                    int markerIndex=getIndexById(marker.getId());

                    // Default values negative - we will substitute this with NA in the text later
                    double dist    = -999;
                    double heading = -999;
                    if ((markerIndex+1)<markerList.size()) {
                        // Get heading and distance from next WP, unless this was the last in the list
                        MarkerObject mo = markerList.get(markerIndex+1);
                        dist = mo.getDist();
                        heading = mo.getTT();
                    }

                    TextView tvDist = (TextView) v.findViewById(R.id.textDist);
                    TextView tvHead = (TextView) v.findViewById(R.id.textHeading);

                    if (dist<0) {
                        tvDist.setText("NA");
                    } else {
                        tvDist.setText(String.format("%.1f nm", dist));
                    }

                    if (heading<0) {
                        tvHead.setText("Heading NA");
                    } else {
                        tvHead.setText(String.format("Heading %.0f ", heading)+DEGREES);
                    }

                    return v;

                }
            });
        }


        // Long clicking on the map adds a way point
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
            @Override
            public void onMapLongClick(LatLng latLng) {
                mVib.vibrate(50);
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
                mVib.vibrate(25);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                updatePolyline();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                
                mPlanUpdated = true;

                mVib.vibrate(50);
                updateMarkerInfo(marker);
                updatePolyline();
                updateNavinfo();
            }
        });



        // We will always have a trip ID because at this point a trip will exist in the database
        // double check to be sure though.

        if (mTripId != null) {
            loadTripFromDb(mTripId);
        }
    }



    /*
     * Plot Aerodrome icons on the map with customized markers
     * All markers are initially created and filed away in the mADMarkers list. Everything is
     * Offset 50% in both directions, so they will center on the position of the AD.
     *
     * When the markers are already done, all we need to do is resize the icon depending on
     * the Zoom level.
     *
     * For zoom levels above 14 it seems there is n need for this functionality as the native
     * airport markers start to appear in Google Maps.
     *
     * Zooming out over level 7 makes the maps ugly because of the extra large icons, so they
     * are hidden at these levels as well
     *
     */
    private void plotAerodromes() {

        // Create the markers if not alrady there
        String iconName = "ic_device_airplanemode_on";
        int iconInt = R.drawable.ic_device_airplanemode_on;
        if (mADMarkers.size() == 0) {
            // Create all AD markers and keep record in an ArrayList
            for (int i = 0; i < ADList.size(); i++) {
                Marker m = mMap.addMarker(new MarkerOptions()
                        .title(ADList.get(i).getIcaoName())
                        .snippet(ADList.get(i).getName())
                        .position(ADList.get(i).getPosition()).icon(BitmapDescriptorFactory.fromResource(iconInt)));

                m.setAnchor(0.5f, .5f);
                mADMarkers.add(m);
            }
        }



        if (mHideADicons || mZoomLevel > 14 || mZoomLevel<7) {
            for (Marker m : mADMarkers) {
                m.setVisible(false);
            }
        } else {

            // Update size of each ADMarker relative to zoom level
            for (Marker m : mADMarkers) {
                m.setVisible(true);
         //       m.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName)));
            }
        }
    }


 /*
 * Plot NavAid icons on the map with customized markers
 * All markers are initially created and filed away in the m list. Everything is
 * Offset 50% in both directions, so they will center on the position of the AD.
 *
 * When the markers are already done, all we need to do is resize the icon depending on
 * the Zoom level.
 *
 * For zoom levels above 16 the map type will be changed to Hybrid and there is no longer need
 * for this marker.
 *
 */
    private void plotNavAids() {

        String snippetText="invalid";
        int iconInt=R.drawable.ic_device_gps_blue;
        if (mNavAidMarkers.size() == 0) {
            // Create all NavAids markers and keep record in an ArrayList
            for (int i = 0; i < vorList.size(); i++) {
                 switch (vorList.get(i).getType()){
                     case NavAid.VOR:
                         iconInt=R.drawable.ic_device_gps_blue;
                         snippetText = "VOR";
                         break;
                     case NavAid.VORDME:
                         iconInt=R.drawable.ic_device_gps_green;
                         snippetText = "VOR/DME";
                         break;
                     case NavAid.DME:
                         iconInt=R.drawable.ic_device_gps_purple;
                         snippetText = "DME";
                         break;
                     case NavAid.NDB:
                         iconInt=R.drawable.ic_device_gps_black;
                         snippetText = "NDB";
                         break;
                     case NavAid.TACAN:
                         iconInt=R.drawable.ic_device_gps_red;
                         snippetText = "TACAN";
                         break;
                     case NavAid.VORTAC:
                         iconInt=R.drawable.ic_device_gps_orange;
                         snippetText = "VORTAC";
                         break;
                     case NavAid.LOCALIZER:
                         iconInt=R.drawable.ic_device_gps_grey;
                         snippetText = "LOCALIZER";
                         break;
                 }



                Marker m = mMap.addMarker(new MarkerOptions()
                        .title(vorList.get(i).getName())
                        .snippet(snippetText+" "+vorList.get(i).getFreq())
                        .position(vorList.get(i).getPosition()).icon(BitmapDescriptorFactory.fromResource(iconInt)));

                m.setAnchor(.5f, .5f);
                mNavAidMarkers.add(m);
            }
        }


        if (mHideNavAidIcons || mZoomLevel >16 || mZoomLevel<7) {
            // Preference off for AD markers - hide them
            for (Marker m : mNavAidMarkers) {
                m.setVisible(false);
            }
       } else {
            // Update size of each NavAidMarker relative to zoom level
            for (Marker m : mNavAidMarkers) {
                m.setVisible(true);
              //  m.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName)));
            }
        }
    }



    private Dimension getDimensions(String iconName) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Dimension d = new Dimension();
        d.w=imageBitmap.getWidth();
        d.h=imageBitmap.getHeight();

        return d;
    }


    private static class Dimension {
        int w,h;
    }

    public Bitmap resizeMapIcons(String iconName){

        // Try to get the sizing right
        // At ZoomLevel 12 60px

        // At Zoomlevel  12 100px ... not more than that
        // 13.9  233  seems OK

        double pixelSizeAtZoom14 = 500; //the size of the icon at zoom level 0
        int maxPixelSize = 150;       //restricts the maximum size of the icon, otherwise the browser will choke at higher zoom levels trying to scale an image to millions of pixels
        int relativePixelSize = (int) Math.round(pixelSizeAtZoom14*Math.pow(2,(mZoomLevel-14))); // use 2 to the power of current zoom to calculate relative pixel size.  Base of exponent is 2 because relative size should double every time you zoom in

        if(relativePixelSize > maxPixelSize) //restrict the maximum size of the icon
            relativePixelSize = maxPixelSize;

        Log.d("TBR:", "Rel pixel size: "+relativePixelSize);

        // Create bitmap from drawable and size it.. if it makes sense
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        if (relativePixelSize>0) {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, relativePixelSize, relativePixelSize, false);
            return resizedBitmap;
        } else {
            return imageBitmap;
        }
    }

    private void updateMapType() {
        switch (mSharedPrefs.getString("mapType","1")) {
            case "1":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "2":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "3":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "4":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }



    private void updateZoom() {
        mZoomLevel = (float) Double.parseDouble(mSharedPrefs.getString("zoomLevel","10."));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(mZoomLevel), 1000, null);
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
    private int getIndexById(String mid) {
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

    private void updatePolyline() {
        if (polyline==null) {
            Log.d("TBR:", "UpdatePolyLine: ==null");
            PolylineOptions lineOptions = new PolylineOptions().geodesic(true);
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
     * Update distance and heading info for all markers except the last one
     * After this is done all markers will have the distance to next marker and
     * the initial heading needed to reach next marker
     */
    private void updateNavinfo(){
        if (markerList.size()<2) {
            return;
        }

        for (int i=0;i<markerList.size()-1;i++){
            MarkerObject mFrom =markerList.get(i);
            MarkerObject mTo = markerList.get(i+1);

            double dist=computeDistanceBetween(mFrom.getMarker().getPosition(), mTo.getMarker().getPosition());
            double heading = computeHeading(mFrom.getMarker().getPosition(), mTo.getMarker().getPosition());

            // Set these values on the to point, that way we will have all we need in the WP
            // dist from prev, required heading from prev, IAS, TAS etc later...

            mTo.setDist(dist);
            mTo.setTT(heading);
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
     * This is where we will get to when another activity is terminated. We should be able
     * to process a markerIndex along with a code indicating how we are supposed to act on it
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_M_ID) {

            // This is for handling results after showing the Waypoint Info Screen
            
            if (data!=null) {
                int markerIndex = (int) data.getSerializableExtra(InfoEditFragment.EXTRA_MARKER_ID);
                Log.d("TBR:", "onActivityResult: Received markerIndex:"+markerIndex);
                
                Marker m=null;
                switch (resultCode) {
                    case InfoEditFragment.ACTION_CANCEL:
                        Log.d("TBR:","OnActivityResult, resultCode: ACTION_CANCEL");
                        m=markerList.get(markerIndex).getMarker();
                        gotoLocation(m.getPosition(),ZOOM_OVERVIEW);
                        break;

                    case NavPagerActivity.ACTION_UPDATE:
                        Log.d("TBR:","OnActivityResult, resultCode: ACTION_UPDATE");
                        m=markerList.get(markerIndex).getMarker();
                        gotoLocation(m.getPosition(),ZOOM_OVERVIEW);

                        // We need to request save on exit unless we want to lose changes
                        mPlanUpdated=true;
                        break;
                    
                    
                    case InfoEditFragment.ACTION_DELETE:
                        Log.d("TBR:","OnActivityResult, resultCode: ACTION_DELETE");

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

                        // Set camera on previous WP if there is one
                        if ((markerIndex-1)>=0) {
                            gotoLocation(markerList.get(markerIndex-1).getMarker().getPosition(), ZOOM_OVERVIEW);
                        } else {
                            // Otherwise use the preferred start location
                            gotoPreferredStartLocation();
                        }


                        // We need to request save on exit unless we want to lose changes
                        mPlanUpdated=true;
                        break;
                }
            }
        } else if (requestCode == REQUEST_GLOBAL_VARS) {

            // This is for handling results after getting global navigation variables
            
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d("TBR:","Result OK received from GlobalNavValFragment");

                    Intent intent = DetailPagerActivity.newIntent(this, currentMarkerIndex);
                    startActivity(intent);
                    break;
                
                case Activity.RESULT_CANCELED:
                    Log.d("TBR:", "Result Cancelled from GlobalNavValFragment");
                    break;
            }


        } else if (requestCode == SETTINGS_RESULT) {
            // A preference has been changed - update the member variables and refresh the display

            updatePreferenceFlags();

            updateMapType();
            updateZoom();
        }

    }

    private void gotoPreferredStartLocation() {
        LatLng position = null;
        try {
            position = searchLocation(mSharedPrefs.getString("startPlace", "Roskilde airport, Denmark"));
            gotoLocation(position, mZoomLevel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePreferenceFlags() {
        mHideADicons = ! mSharedPrefs.getBoolean("ADs", true);
        mHideNavAidIcons = ! mSharedPrefs.getBoolean("navAids", true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
            case R.id.mapPreferences:
                // Launch the PreferenceActivity
                Intent preferenceIntent = new Intent(this, MarkerPreferenceActivity.class);
                startActivityForResult(preferenceIntent, SETTINGS_RESULT);
                break;

            case R.id.importNavAids:
                // Launch GoogleSheetsActivity
                Intent sheetsIntent = new Intent(this, GoogleSheetActivity.class);
                startActivity(sheetsIntent);
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
    private void geoLocate(View v) throws IOException {

        hideSoftKeyboard(v);

        TextView tv = (TextView) findViewById(R.id.editText1);
        // check for help request
        if (String.valueOf(tv.getText()).matches(":help")) {
            tv.setError("Special commands:\n:navaids - list navaids\n" +
                    ":ad - list installed aerodromes");
            tv.setText("");
            return;
        }

        // check if special command :navaids
        if (String.valueOf(tv.getText()).matches(":navaids")) {
            String miniHelp="Valid names:\n";
            for (NavAid n : vorList) {
                miniHelp += n.getName() + "\n";
            }
            tv.setError(miniHelp);
            tv.setText("");
            return;
        }

        // check if special command aerodromes
        if (String.valueOf(tv.getText()).matches(":ad")) {
            String miniHelp="Installed Aerodromes:\n";
            for (Aerodrome n : ADList) {
                miniHelp += n.getIcaoName() + "\n";
            }
            tv.setError(miniHelp);
            tv.setText("");
            return;
        }


        mSearchedFor = tv.getText().toString();
        tv.setText("");

        // Check existing NavAid names - they are not in Google Places
        for (NavAid n : vorList) {
            if (n.getName().equals(mSearchedFor)) {
                placeAndZoomOnMarker(n.getPosition(), mZoomLevel);
                return;
            }
        }

        // Check existing AD names - they are (probably) not in Google Places
        for (Aerodrome n : ADList) {
            if (n.getIcaoName().equals(mSearchedFor)) {
                placeAndZoomOnMarker(n.getPosition(), mZoomLevel);
                return;
            }
        }


        // Look up location name
        LatLng position = searchLocation((String) mSearchedFor);
        placeAndZoomOnMarker(position, mZoomLevel);

    }


    private LatLng searchLocation(String searchedFor) throws IOException {
        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(String.valueOf(searchedFor), 1);

        LatLng position = null;

        if (list.size() > 0) {
            Address adr = list.get(0);

            double lat = adr.getLatitude();
            double lng = adr.getLongitude();
            position = new LatLng(lat, lng);
        }

        return position;
    }



    private void placeAndZoomOnMarker(LatLng position, float zoom) {
        gotoLocation(position, zoom);
        addMarker(position);
    }


    /*
     * This one is used when adding a marker from text geocoding and
     * from long pressing the map
     *
     * Creates the physical marker on the map and appends a MarkerObject on the markerList
     */
    private void addMarker(LatLng loc) {

        Marker marker =createMapMarker(loc);

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

        Marker marker =createMapMarker(loc);

        ArrayList<Pejling> pejlinger = new ArrayList<>();
        pejlinger=nearestVORs(marker);

        MarkerObject mo = new MarkerObject(marker, marker.getTitle(), marker.getSnippet(), pejlinger);

        markerList.add(afterThis+1, mo);
        updatePolyline();
        updateNavinfo();
    }


    /*
     * createMapMarker
     * Establish MarkerOptions and create Google Maps Marker and make surethe title is safe.
     * Before returning set the mPlanUpdated flag as this is a change we need to save upon
     * editing
     *
     * Used by both addMarker functions internally
     *
     * Params  Latlng location of the marker
     * Returns Marker Freshly baked marker
     */
    private Marker createMapMarker(LatLng loc) {
        MarkerOptions options = createMarkerOptions(loc);
        Marker marker = mMap.addMarker(options);

        if (marker.getTitle()==null) {
            // This happens sometimes when a location name cannot be found
            marker.setTitle("Unknown location");
        }

        // Flag that we need to save upon exiting the program
        mPlanUpdated = true;

        return marker;
    }


    /*
    * Used for all add marker functions
    * Create the options needed for a new marker
    */
    private MarkerOptions createMarkerOptions(LatLng loc) {
        Address adr = findAddress(loc);

        String text = adr.getLocality();
        MarkerOptions options = new MarkerOptions()
                .draggable(true)
                .position(loc);

        if (mSearchedFor=="") {
            options.title(text);
        } else {
            // Use the name from the searchfield this time instead of the location name/blank
            options.title((String) mSearchedFor);
            mSearchedFor="";
        }

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



    private void writePreferenceChanges() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("zoomLevel", String.format(Locale.US, "%.1f",mZoomLevel));
        editor.apply();

    }


    /*
     *  Open the database and get all way points into our local markerList by mapping each
     *  of the database WpItems into the local MarkerObjects and then adding them to the
     *  ArrayList.
     *
     */
    private void loadTripFromDb(String tripId) {
        // Get a handle to the database helper and prepare the database
        DataSource dataSource = new DataSource(getApplicationContext());
        dataSource.open();

        // copy trip details from database into marker array etc ...
        List<WpItem> ListFromDB = dataSource.getAllWps(tripId);
        dataSource.close();

        // In this version map between WpItem og MarkerObject ... we probably need to store the
        // MarkerObject in the DB when we get furhter down the road

        for (WpItem wp : ListFromDB) {

            String name = wp.getWpName();
            LatLng location = new LatLng(wp.getWpLat(), wp.getWpLon());

            Log.d("TBR:", "getWpName: "+name);
            Log.d("TBR:", "getWpId: "+wp.getWpId());
            Log.d("TBR:", "getWpAltitude: "+wp.getWpAltitude());
            Log.d("TBR:", "getWpDistance: "+wp.getWpDistance());
            Log.d("TBR:", "getWpLat: "+wp.getWpLat());
            Log.d("TBR:", "getWpLon: "+wp.getWpLon());

            MarkerOptions options = new MarkerOptions()
                    .draggable(true)
                    .position(location);

            Marker m = mMap.addMarker(options);
            m.setTitle(name);

            ArrayList<Pejling> pejlinger = new ArrayList<>();
            pejlinger = nearestVORs(m);

            MarkerObject mo = new MarkerObject(m, name, null, pejlinger);

            mo.setMyId(wp.getWpId());      // WP id
            mo.setALT(wp.getWpAltitude()); // WP Alt
            mo.setDist(wp.getWpDistance()); // Dist

            // If a specfic way point was selected - keep a reference when found
            if (!mWpId.equals("")) {
                if (mWpId.equals(wp.getWpId())) {
                    gotoLocation(location, ZOOM_OVERVIEW);
                }
            }
            markerList.add(mo);
        }

        // Now if we did not get a starting point, we will just use the first one ... or the
        // preferred starting point if there are no points
        if (mWpId.equals("")) {
            if (markerList.size()>0){
                gotoLocation(markerList.get(0).getMarker().getPosition(), ZOOM_OVERVIEW);
            } else {
                // Otherwise use the preferred start location
                gotoPreferredStartLocation();
            }
        }
        updatePolyline();
        updateNavinfo();

    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();

        List<WpItem> listForDB;


        Log.d("TBR", "MarkerDemoActivity, onBackPressed called");

        // if trip is new or has been updated we should offer to save it here

        // FOR NOW, JUST CONSIDER UPDATES UNTIL WE GOT IT RIGHT

        if (mPlanUpdated) {
            Toast.makeText(this, "Saving to DB", Toast.LENGTH_SHORT).show();

            // Get a handle to the database helper and prepare the database
            mDataSource = new DataSource(this);
            mDataSource.open();


            // get the trip name somehow, then remove existing trip

            String tripName = mDataSource.getTripName(mTripId);
            Log.d("TBR:", "MarkerDemoActivity, onBackPressed> TripName from DB: "+tripName);

            // mDataSource.DeleteTrip(id) and all the previous waypoints
            mDataSource.deleteTrip(mTripId);
            Log.d("TBR:", "Trip deleted along with Waypoints");


            // create a list with populated WpItems ... mapped from the MarkerItem list

            listForDB = new ArrayList<>();
            Log.d("TBR:", "Creating WpItems from markerList");
            for (int i = 0; i < markerList.size(); i++) {
                MarkerObject mo=markerList.get(i);
                WpItem wp = new WpItem(
                        mo.getMyId(),
                        mo.getText(),
                        mo.getMarker().getPosition().latitude,
                        mo.getMarker().getPosition().longitude,
                        mo.getDist(),
                        (int) mo.getALT(),
                        null,                       // Trip ID ... will be set when adding trip?
                        i);


                Log.d("TBR:", "#"+i+" wp.getWpName: "+wp.getWpName());
                listForDB.add(wp);
            }


            // then createTrip like this
            TripItem ti =  mDataSource.addFullTrip(tripName, listForDB);
            mDataSource.close();
            Log.d("TBR", "Data updated in DB");

            // update the current trip id, so we can show related wp's
            mTripId = ti.getTripId();


        }

        // Clean up ...
        mMap.clear();
        markerList.clear();
        midpointList.clear();
        polyline.remove();
        polyline=null;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static String getCurrentTripId(){
        return mTripId;
    }

}
