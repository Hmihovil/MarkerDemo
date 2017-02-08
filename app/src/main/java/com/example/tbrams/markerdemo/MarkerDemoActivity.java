package com.example.tbrams.markerdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
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

import com.example.tbrams.markerdemo.components.MarkerDemoUtils;
import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.ExtraMarkers;
import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Obstacle;
import com.example.tbrams.markerdemo.data.Pejling;
import com.example.tbrams.markerdemo.data.ReportingPoint;
import com.example.tbrams.markerdemo.db.DataSource;
import com.example.tbrams.markerdemo.dbModel.AreaItem;
import com.example.tbrams.markerdemo.dbModel.TripItem;
import com.example.tbrams.markerdemo.dbModel.WpItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.tbrams.markerdemo.TripAdapter.TRIP_KEY;
import static com.example.tbrams.markerdemo.TripAdapter.WP_KEY;
import static com.example.tbrams.markerdemo.components.Util.parseComponent;

public class MarkerDemoActivity extends MarkerDemoUtils implements
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        AlertDialogFragment.SimpleDialogListener {

    private static final int REQUEST_M_ID=1;
    private static final int REQUEST_GLOBAL_VARS=2;
    private static final int SETTINGS_RESULT = 3;

    private static final float ZOOM_OVERVIEW = 10.0f;

    private static final String TAG = "TBR:MDA" ;

    private  MarkerObject mUndoDeleteMarker = null;
    private  int mUndoDeleteIndex= -1;

    private static GoogleMap mMap;
    private SharedPreferences mSharedPrefs;

    private final MarkerLab markerLab = MarkerLab.getMarkerLab(this);
    private final List<MarkerObject> markerList = markerLab.getMarkers();

    // Get markers from singleton storage
    private ExtraMarkers sExtraMarkers = ExtraMarkers.get(this);
    private List<NavAid> mNavAidList = sExtraMarkers.getNavAidList();
    private List<ReportingPoint> mReportingPointList = sExtraMarkers.getReportingPointList();
    private final List<Aerodrome> mAerodromeList = sExtraMarkers.getAerodromeList();
    private final List<Obstacle> mObstacleList = sExtraMarkers.getObstaclesList();
    private final List<AreaItem> mAreaList = sExtraMarkers.getAreaItemList();

    // Special list of VOR only nav. aids
    private final List<NavAid> mVorList = new ArrayList<>();

    // This is used for map marker storage locally
    private final List<Marker> mNavAidMarkers = new ArrayList<>();
    private final List<Marker> mADMarkers = new ArrayList<>();
    private final List<Marker> mRPMarkers = new ArrayList<>();
    private final List<Marker> mObstacleMarkers = new ArrayList<>();
    private final List<Polygon> mPolygons = new ArrayList<>();

    private static int currentMarkerIndex=-1;

    private Vibrator mVib;
    private static String mTripId;
    private String mWpId;
    private boolean mPlanUpdated=false;
    DataSource      mDataSource;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: mNavAidList.size(): "+ mNavAidList.size());
        Log.d(TAG, "onCreate: mAerodromeList.size(): "+ mAerodromeList.size());

        setContentView(R.layout.activity_marker_demo);

        // make sure we have default values by running this first time a user launches the app
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        prepareListOfVORs();


        // Get trip and wp index from extra arguments
        mTripId = getIntent().getStringExtra(TRIP_KEY);
        mWpId   = getIntent().getStringExtra(WP_KEY);
        Log.d(TAG,"Received Trip id: " + mTripId +" and wp id: "+ mWpId);

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
                Snackbar.make(view, "Click here when you have weather and performance details ready", Snackbar.LENGTH_LONG)
                        .setAction("Go!", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(MarkerDemoActivity.this, GlobalNavValActivity.class);
                                startActivityForResult(intent, REQUEST_GLOBAL_VARS);

                                Log.d(TAG, "GlobalNavValActivity Started...");
                            }
                        }).show();
            }
        });


    }

    private void prepareListOfVORs() {
        // Prepare a list with VOR's for navigation101
        for (int i = 0; i < mNavAidList.size(); i++) {
            NavAid na= mNavAidList.get(i);
            if (na.getType()==NavAid.VOR || na.getType()==NavAid.VORDME) {
                // We need an easy way to find this navaid again, keep original index
                na.setSeq_id(i);
                mVorList.add(na);
            }
        }

        Log.d(TAG, "onCreate: mVorList.size(): "+ mVorList.size());
    }





    /*
     * Called by getMapAsync when ready
     */
    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        // Check map type preference and update the map here
        updateMapType(mSharedPrefs, mMap);
        updateZoom(mSharedPrefs, mMap);


        // In rare cases the lookup will time out and we will end up with a null position variable. For that reason,
        // I have set the default backup position to be
        LatLng position = new LatLng(55.74015, 11.96788);  // Skibby!

        try {
            position = searchLocation(mSharedPrefs.getString("startPlace", "Roskilde airport, Denmark"));
        } catch (IOException e) {
            e.printStackTrace();
        }



        if (mMap!=null) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, getZoomLevel()));

            // Disable the navigation toolbar that will otherwise pop up after setting a marker
            mMap.getUiSettings().setMapToolbarEnabled(false);

            // Disable my location button though
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            plotNavAids(mNavAidMarkers, mNavAidList, mMap);
            plotAerodromes(mADMarkers, mAerodromeList, mMap);
            plotReportingPoints(mRPMarkers, mReportingPointList, mMap);
            plotObstacles(mObstacleMarkers, mObstacleList, mMap);
            plotAreas(mPolygons, mAreaList, mMap);

            mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener(){

                @Override
                public void onPolygonClick(Polygon polygon) {
                    Log.d(TAG, "onPolygonClick: polygon clicked, id: "+polygon.getId());
                    for(int i=0;i<mPolygons.size();i++) {
                        Log.d(TAG, "check polygon with id: "+polygon.getId());
                        Log.d(TAG, "mPolygons.get(i).getId(): "+mPolygons.get(i).getId());
                        if (mPolygons.get(i).getId().equals(polygon.getId().toString())) {
                            // Then we know what to show, just now how...
                            Log.d(TAG, "that is "+mAreaList.get(i).getAreaName()+" "+mAreaList.get(i).getAreaIdent());
                            Snackbar.make(getCurrentFocus(), "that is "+mAreaList.get(i).getAreaName()+" "+mAreaList.get(i).getAreaIdent()+
                                    String.format("\nFrom: %d to %d ft", mAreaList.get(i).getAreaFromAlt(),mAreaList.get(i).getAreaToAlt()), Snackbar.LENGTH_LONG).show();

                            break;
                        }
                    }
                }
            });

            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    CameraPosition cameraPosition = mMap.getCameraPosition();
                    setZoomLevel(cameraPosition.zoom);
                    writePreferenceChanges();

                    if(getZoomLevel()> ZOOM_CHANGE_MAP_TYPE) {
                        Log.d(TAG, "onCameraIdle: Changing to hybrid map");
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        setMapTypeChangedByZoom(true);
                    } else {
                        // Switch back to preferred map type after "forced" change due to zoom
                        if (isMapTypeChangedByZoom()) {
                            // Fetch the hide details variables from preferences.
                            updatePreferenceFlags();
                            updateMapType(mSharedPrefs, mMap);
                            setMapTypeChangedByZoom(false);
                        }

                    }
                    // Update both Aerodrome and Navigational aid icons was well
                    plotNavAids(mNavAidMarkers, mNavAidList, mMap);
                    plotAerodromes(mADMarkers, mAerodromeList, mMap);
                    plotReportingPoints(mRPMarkers, mReportingPointList, mMap);
                    plotObstacles(mObstacleMarkers, mObstacleList, mMap);
                    plotAreas(mPolygons, mAreaList, mMap);

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
                            Log.d(TAG,"NavAid marker clicked");
                            return null;
                        }
                    }

                    // check if an Aerodrome has been clicked
                    for (Marker m : mADMarkers) {
                        if (marker.getId().equals(m.getId())) {
                            Log.d(TAG,"Aerodrome marker clicked");
                            return null;
                        }
                    }

                    // check if a Reporting Point has been clicked
                    for (Marker m : mRPMarkers) {
                        if (marker.getId().equals(m.getId())) {
                            Log.d(TAG,"Reporting Point marker clicked");
                            return null;
                        }
                    }

                    // check if an obstacle has been clicked
                    for (Marker m : mObstacleMarkers) {
                        if (marker.getId().equals(m.getId())) {
                            Log.d(TAG,"Obstacle marker clicked");
                            return null;
                        }
                    }


                    View v = getLayoutInflater().inflate(R.layout.info_window, null);

                    TextView tvLocality = (TextView) v.findViewById(R.id.tvLocality);
                    tvLocality.setText(marker.getTitle());

                    int markerIndex=getIndexById(marker.getId(), markerList);

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
                currentMarkerIndex=getIndexById(marker.getId(), markerList);

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
                updatePolyline(markerList, mMap);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                
                mPlanUpdated = true;

                mVib.vibrate(50);
                updateMarkerInfo(marker);
                updatePolyline(markerList, mMap);
                updateNavinfo(markerList);
            }
        });



        // We will always have a trip ID because at this point a trip will exist in the database
        // double check to be sure though.

        if (mTripId != null) {
            loadTripFromDb(mTripId);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
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
                Log.d(TAG, "onActivityResult: Received markerIndex:"+markerIndex);
                
                Marker m=null;
                switch (resultCode) {
                    case InfoEditFragment.ACTION_CANCEL:
                        Log.d(TAG,"OnActivityResult, resultCode: ACTION_CANCEL");
                        m=markerList.get(markerIndex).getMarker();
                        gotoLocation(m.getPosition(),ZOOM_OVERVIEW);
                        break;

                    case NavPagerActivity.ACTION_UPDATE:
                        Log.d(TAG,"OnActivityResult, resultCode: ACTION_UPDATE");
                        m=markerList.get(markerIndex).getMarker();
                        gotoLocation(m.getPosition(),ZOOM_OVERVIEW);

                        // We need to request save on exit unless we want to lose changes
                        mPlanUpdated=true;
                        break;
                    
                    
                    case InfoEditFragment.ACTION_DELETE:
                        Log.d(TAG,"OnActivityResult, resultCode: ACTION_DELETE");

                        // Prepare for undoing later by storing both marker and position in track
                        mUndoDeleteIndex = markerIndex;
                        mUndoDeleteMarker = new MarkerObject();
                        mUndoDeleteMarker = markerList.get(markerIndex);

                        // Delete physical marker and then the MarkerObject
                        markerList.get(markerIndex).getMarker().remove();
                        markerList.remove(markerIndex);
                        updatePolyline(markerList, mMap);

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
                    Log.d(TAG,"Result OK received from GlobalNavValFragment");

                    Intent intent = DetailPagerActivity.newIntent(this, currentMarkerIndex);
                    startActivity(intent);
                    break;
                
                case Activity.RESULT_CANCELED:
                    Log.d(TAG, "Result Cancelled from GlobalNavValFragment");
                    break;
            }


        } else if (requestCode == SETTINGS_RESULT) {
            // A preference has been changed - update the member variables and refresh the display

            updatePreferenceFlags();

            updateMapType(mSharedPrefs, mMap);
            updateZoom(mSharedPrefs, mMap);

        }

    }

    private void gotoPreferredStartLocation() {
        LatLng position = null;
        try {
            position = searchLocation(mSharedPrefs.getString("startPlace", "Roskilde airport, Denmark"));
            gotoLocation(position, getZoomLevel());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePreferenceFlags() {

        setHide_private_ad(! mSharedPrefs.getBoolean("show_private_ad", true));
        setHide_public_ad(! mSharedPrefs.getBoolean("show_public_ad", true));
        setHide_recreational_ad(! mSharedPrefs.getBoolean("show_recreational_ad", true));
        setHide_reporting_points(! mSharedPrefs.getBoolean("show_reporting", true));
        setHide_obstacles(!mSharedPrefs.getBoolean("show_obstacles", true));
        setHide_CTR(!mSharedPrefs.getBoolean("show_CTR", false));
        setHide_TMA(!mSharedPrefs.getBoolean("show_TMA", false));

        setHide_VOR(! mSharedPrefs.getBoolean("show_VOR", true));
        setHide_DME(! mSharedPrefs.getBoolean("show_DME", true));
        setHide_VORDME(! mSharedPrefs.getBoolean("show_VORDME", true));
        setHide_NDB(! mSharedPrefs.getBoolean("show_NDB", true));
        setHide_TACAN(! mSharedPrefs.getBoolean("show_TACAN", true));
        setHide_VORTAC(! mSharedPrefs.getBoolean("show_VORTAC", true));
        setHide_Locator(! mSharedPrefs.getBoolean("show_Locator", true));
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

    /**
     * This function is called when the Search button is pressed.
     *
     * Look up a place name and find the coordinates, then pass the coordinates to
     * the normal appending addMarker.
     *
     * Based on normal Google Search, but includes support for NavAids, Airfields, Reporting points
     * and coordinates, for example:
     *
     * "KORSA", "EKOD", "RP Køge" and "55 12 64N 011 42 96E"
     */
    private void geoLocate(View v) throws IOException {

        hideSoftKeyboard(v);

        TextView tv = (TextView) findViewById(R.id.editText1);
        String searched = tv.getText().toString().toUpperCase();

        // check for help request
        if (searched.matches(":HELP")) {
            tv.setError("Special commands:\n:navaids - list navaids\n" +
                    ":ad - list installed aerodromes");
            tv.setText("");
            return;
        }

        // check if special this is a GPS coordinate
        Pattern pattern = Pattern.compile("(.*?)N (.*)E");
        Matcher matcher = pattern.matcher(searched);
        if (matcher.find()) {
            String lat = matcher.group(1);
            String lon = matcher.group(2);
            LatLng coordinate = new LatLng(parseComponent(lat), parseComponent(lon));
            System.out.println(String.format(Locale.ENGLISH, "Searching for coordinate: (%f, %f)", coordinate.latitude, coordinate.longitude));

            placeAndZoomOnMarker(coordinate, getZoomLevel());
            return;

        }

        // check if special command :navaids
        if (searched.matches(":NAVAIDS")) {
            String miniHelp="Valid names:\n";
            for (NavAid n : mNavAidList) {
                miniHelp += n.getName() + "\n";
            }
            tv.setError(miniHelp);
            tv.setText("");
            return;
        }

        // check if special command aerodromes
        if (searched.matches(":AD")) {
            String miniHelp="Installed Aerodromes:\n";
            for (Aerodrome n : mAerodromeList) {
                miniHelp += n.getIcaoName() + "\n";
            }
            tv.setError(miniHelp);
            tv.setText("");
            return;
        }


        setSearchedFor(searched);
        tv.setText("");

        // Check existing NavAid names - they are not in Google Places
        for (NavAid n : mNavAidList) {
            if (n.getName().equals(getSearchedFor())) {
                placeAndZoomOnMarker(n.getPosition(), getZoomLevel());
                return;
            }
        }

        // Check existing AD names - they are (probably) not in Google Places
        for (Aerodrome n : mAerodromeList) {
            if (n.getIcaoName().equals(getSearchedFor())) {
                placeAndZoomOnMarker(n.getPosition(), getZoomLevel());
                return;
            }
        }

        // Check Reporting Point names - they are (probably) not in Google Places
        // Format for this is "RP name", for example "RP BORUP"
        for (ReportingPoint rp : mReportingPointList) {
            String rpString = String.format("RP %s", rp.getName().toUpperCase());
            if (getSearchedFor().equals(String.format("RP %s",rp.getName().toUpperCase()))) {
                placeAndZoomOnMarker(rp.getPosition(), getZoomLevel());
                return;
            }
        }

        // Look up location name
        LatLng position = searchLocation(getSearchedFor());
        placeAndZoomOnMarker(position, getZoomLevel());

    }


    /**
     * Find the location based on Google Maps Search and return the location.
     *
     * @param searchedFor String, for example "Slaglille"
     * @return            LatLng  object with position
     *
     * @throws IOException
     */
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
        // In rare cases, searching for something will not return a position. Just ignore those
        if (position != null) {
            gotoLocation(position, zoom);
            addMarker(position);
        }
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
        pejlinger = nearestVORs(marker, mVorList);

        MarkerObject mo = new MarkerObject(marker, marker.getTitle(), marker.getSnippet(), pejlinger);
        markerList.add(mo);

        updatePolyline(markerList, mMap);
        updateNavinfo(markerList);

    }


    /*
     * This one is used when adding a marker from midpoint dragging where we need to
     * insert a point in between other points instead of just appending
     *
     */
    private void addMarker(LatLng loc, int afterThis) {

        Marker marker =createMapMarker(loc);

        ArrayList<Pejling> pejlinger = new ArrayList<>();
        pejlinger=nearestVORs(marker, mVorList);

        MarkerObject mo = new MarkerObject(marker, marker.getTitle(), marker.getSnippet(), pejlinger);

        markerList.add(afterThis+1, mo);
        updatePolyline(markerList, mMap);
        updateNavinfo(markerList);
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
        editor.putString("zoomLevel", String.format(Locale.US, "%.1f", getZoomLevel()));
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

        LatLng startPoint = null;
        for (WpItem wp : ListFromDB) {

            String name = wp.getWpName();
            LatLng location = new LatLng(wp.getWpLat(), wp.getWpLon());

            MarkerOptions options = new MarkerOptions()
                    .draggable(true)
                    .position(location);

            Marker m = mMap.addMarker(options);
            m.setTitle(name);

            ArrayList<Pejling> pejlinger = new ArrayList<>();
            pejlinger = nearestVORs(m, mVorList);

            MarkerObject mo = new MarkerObject(m, name, null, pejlinger);

            mo.setMyId(wp.getWpId());      // WP id
            mo.setALT(wp.getWpAltitude()); // WP Alt
            mo.setDist(wp.getWpDistance()); // Dist

            // If a specific way point was selected - keep a reference when found
            if (mWpId!=null) {
                if (mWpId.equals(wp.getWpId())) {
                    startPoint = location;
                }
            }
            markerList.add(mo);
        }

        // Now if we did not get a starting point, we will just use the first one ... or the
        // preferred starting point if there are no points
        if (startPoint==null) {
            if (markerList.size()>0){
                gotoLocation(markerList.get(0).getMarker().getPosition(), ZOOM_OVERVIEW);
            } else {
                // Otherwise use the preferred start location
                gotoPreferredStartLocation();
            }
        } else {
            gotoLocation(startPoint, ZOOM_OVERVIEW);

        }
        updatePolyline(markerList, mMap);
        updateNavinfo(markerList);

    }




    @Override
    public void onBackPressed() {

        Log.d(TAG, "MarkerDemoActivity, onBackPressed called");

        // if trip is new or has been updated we should offer to save it here

        // FOR NOW, JUST CONSIDER UPDATES UNTIL WE GOT IT RIGHT

        if (mPlanUpdated) {
            showSaveDataDialog();
        } else {
            cleanUp();
            super.onBackPressed();
        }

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



    private void showSaveDataDialog() {
        AlertDialogFragment simpleDialog = AlertDialogFragment.newInstance(
                "Plan has changed\nDo you want to save?",
                R.drawable.ic_map_marker,
                "Yes", "No");

        // Use setCancelable() to make the dialog non-cancelable
        simpleDialog.setCancelable(false);
        simpleDialog.show(getSupportFragmentManager(), "TB AlertDialogFragment");
    }


    // These three event listeners are required by the SimpleDialogListener model
    // we will know the response to the dialog this way...

    @Override
    public void onPositiveResult(DialogFragment dlg) {
        Log.i(TAG, "Dialog Positive Result");

        List<WpItem> listForDB;

        Toast.makeText(this, "Saving to DB", Toast.LENGTH_SHORT).show();

        // Get a handle to the database helper and prepare the database
        mDataSource = new DataSource(this);
        mDataSource.open();


        // get the trip name somehow, then remove existing trip

        String tripName = mDataSource.getTripName(mTripId);
        Log.d(TAG, "MarkerDemoActivity, onBackPressed> TripName from DB: "+tripName);

        // mDataSource.DeleteTrip(id) and all the previous waypoints
        mDataSource.deleteTrip(mTripId);
        Log.d(TAG, "Trip deleted along with Waypoints");


        // create a list with populated WpItems ... mapped from the MarkerItem list

        listForDB = new ArrayList<>();
        Log.d(TAG, "Creating WpItems from markerList");
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


            Log.d(TAG, "Building listForDB #"+i+" wp.getWpName: "+wp.getWpName());
            listForDB.add(wp);
        }


        // then createTrip like this
        TripItem ti =  mDataSource.addFullTrip(tripName, listForDB);
        mDataSource.close();
        Log.d(TAG, "Data updated in DB");

        // update the current trip id, so we can show related wp's
        mTripId = ti.getTripId();

        cleanUp();

        super.onBackPressed();

    }

    @Override
    public void onNegativeResult(DialogFragment dlg) {
        Log.i(TAG, "Dialog Negative Result");
        cleanUp();
        super.onBackPressed();

    }

    @Override
    public void onNeutralResult(DialogFragment dlg) {
        Log.i(TAG, "Dialog Neutral Result");
    }

    private void cleanUp() {

        mMap.clear();
        markerList.clear();
        clearMidpoints();
        clearPolyline();
    }


}
