package com.example.tbrams.markerdemo.components;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;

import com.example.tbrams.markerdemo.R;
import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Obstacle;
import com.example.tbrams.markerdemo.data.Pejling;
import com.example.tbrams.markerdemo.data.ReportingPoint;
import com.example.tbrams.markerdemo.dbModel.AreaItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;
import static com.google.maps.android.SphericalUtil.interpolate;

public class MarkerDemoUtils extends AppCompatActivity {

    private static final String TAG = "TBR:MarkerDemoUtils";
    public static final int AERODROME_MAX_ZOOM = 15;  // At this point normal map icons show up
    public static final int AERODROME_MIN_ZOOM = 7;   // At this point the cluttering is too high
    public static final int OBSTACLE_MAX_ZOOM = 17;  // At this point normal map icons show up
    public static final int OBSTACLE_MIN_ZOOM = 7;   // At this point the cluttering is too high
    public static final int NAVAID_MAX_ZOOM = 17;     // At this point we have switched to sat mode
    public static final int NAVAID_MIN_ZOOM = 7;      // At this point the cluttering is too high
    public static final int ZOOM_CHANGE_MAP_TYPE = 16;

    private static final String CONTROL_AREA_COLOR = "#10aa0000";
    private static final float CONTROL_AREA_BORDER = 1f;
    private static final String TERMINAL_AREA_COLOR = "#100000aa";
    private static final float TERMINAL_AREA_BORDER = 1f;

    private final static List<Marker> midpointList = new ArrayList<>();
    private static Polyline polyline;
    private float mZoomLevel;
    private String mSearchedFor;
    private boolean mMapTypeChangedByZoom=false;

    private boolean mHide_public_ad=false;
    private boolean mHide_private_ad=false;
    private boolean mHide_recreational_ad=false;

    private boolean mHide_reporting_points=false;
    private boolean mHide_obstacles=false;
    private boolean mHide_TMA=false;
    private boolean mHide_CTR=false;

    private boolean mHide_VOR=false;
    private boolean mHide_VORDME=false;
    private boolean mHide_NDB=false;
    private boolean mHide_TACAN=false;
    private boolean mHide_VORTAC=false;
    private boolean mHide_DME=false;
    private boolean mHide_Locator=false;


    // MidPoint related


    /*
     *  Clear all physical traces of midpoints from the map, then go through all established
     *  markers again and calculate new locations for midpoints. Then add them to the map and
     *  to our midPointList
     */
    public void updateMidpoints(List<MarkerObject> markerList, GoogleMap gMap) {
        if (markerList.size()<2) return;

        // remove old markers from map and clear the storage
        for (int i=0;i<midpointList.size();i++){
            midpointList.get(i).remove();
        }
        midpointList.clear();

        // Go through all markers and add new non draggable midpoint markers
        for (int i=0; i<markerList.size()-1;i++) {
            LatLng midPt = interpolate(markerList.get(i).getMarker().getPosition(), markerList.get(i+1).getMarker().getPosition(), 0.5);
            Marker marker = gMap.addMarker(new MarkerOptions()
                    .position(midPt)
                    .anchor((float)0.5, (float)0.5)
                    .alpha(.6f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_circle)));

            midpointList.add(marker);
        }
    }

    /*
     * Determine if the marker argument is a midpoint marker
     */
    public boolean isMidPoint(Marker marker) {
        return (getMidpointIndex(marker)>=0);
    }


    /*
     * Look up the marker in the midpointList and return the index. If not found return
     * minus one.
     */
    public static int getMidpointIndex(Marker marker) {
        for (int i=0;i<midpointList.size();i++){
            if (midpointList.get(i).getPosition().equals(marker.getPosition()))
                return i;
        }
        return -1;
    }



    public static void clearMidpoints() {
        midpointList.clear();
    }




    // Navigation related

    /*
     * Update distance and heading info for all markers except the last one
     * After this is done all markers will have the distance to next marker and
     * the initial heading needed to reach next marker
     */
    public void updateNavinfo(List<MarkerObject> markerList){
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
     * Find the three nearest VORs for the marker in question and return a list
     * with the sorted results
     */
    public ArrayList<Pejling> nearestVORs(Marker m, List<NavAid> vorList) {

        ArrayList<Pejling> plist= new ArrayList<>();

        for (int i=0;i<vorList.size();i++) {
            LatLng position = vorList.get(i).getPosition();
            double dist=computeDistanceBetween(position, m.getPosition());
            double heading = computeHeading(position, m.getPosition());
            int original_id=vorList.get(i).getSeq_id();
            plist.add(new Pejling(original_id, dist, heading));
        }


        Collections.sort(plist);

        ArrayList<Pejling> result = new ArrayList<>();
        for (int i=0; i<3;i++) {
            result.add(plist.get(i));
        }

        return result;
    }



    // Bitmap related

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

    public Bitmap resizeMapIcons(String iconName, double zoomLevel){

        // Try to get the sizing right
        // At ZoomLevel 12 60px

        // At Zoomlevel  12 100px ... not more than that
        // 13.9  233  seems OK

        double pixelSizeAtZoom14 = 500; //the size of the icon at zoom level 0
        int maxPixelSize = 150;       //restricts the maximum size of the icon, otherwise the browser will choke at higher zoom levels trying to scale an image to millions of pixels
        int relativePixelSize = (int) Math.round(pixelSizeAtZoom14*Math.pow(2,(zoomLevel-14))); // use 2 to the power of current zoom to calculate relative pixel size.  Base of exponent is 2 because relative size should double every time you zoom in

        if(relativePixelSize > maxPixelSize) //restrict the maximum size of the icon
            relativePixelSize = maxPixelSize;

        Log.d(TAG, "Rel pixel size: "+relativePixelSize);

        // Create bitmap from drawable and size it.. if it makes sense
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        if (relativePixelSize>0) {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, relativePixelSize, relativePixelSize, false);
            return resizedBitmap;
        } else {
            return imageBitmap;
        }
    }


    /*
     * Convert dp pixes to normal pixels
     *
     */
    float dp2px(float pix) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pix,
                getResources().getDisplayMetrics());
    }



    // Map and Zoom related

    public void updateZoom(SharedPreferences sharedPrefs, GoogleMap gMap) {
        float zoomLevel = (float) Double.parseDouble(sharedPrefs.getString("zoomLevel","10."));
        setZoomLevel(zoomLevel);
        gMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 1000, null);
    }



    public void updateMapType(SharedPreferences sharedPrefs, GoogleMap gMap) {
        switch (sharedPrefs.getString("mapType","1")) {
            case "1":
                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "2":
                gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "3":
                gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "4":
                gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }



    // Marker Related

    /*
     * Find and return the marker index with the marker_id as specified
     * otherwise return minus one
     */
    public int getIndexById(String mid, List<MarkerObject> markerList) {
        for (int i=0; i<markerList.size();i++) {
            if (markerList.get(i).getMarker().getId().equals(mid)) {
                return i;
            }
        }
        return -1;
    }





    /*
     * find new location text for existing marker - used when dragged
     */
    public void updateMarkerInfo(Marker marker) {

        Address adr = findAddress(marker.getPosition());
        marker.setTitle(adr.getLocality());
        marker.setSnippet(adr.getCountryName());
    }



    /*
     * Used for all add marker functions
     * Create the options needed for a new marker
     */
    public MarkerOptions createMarkerOptions(LatLng loc) {
        Address adr = findAddress(loc);

        String text="";
        String country = "";
        if (adr!=null) {
            text = adr.getLocality();
            adr.getCountryName();

        } else {
            text = "GeoLookup Failed";
        }

        MarkerOptions options = new MarkerOptions()
                .draggable(true)
                .position(loc);

        if (mSearchedFor == "") {
            options.title(text);
        } else {
            // Use the name from the searchfield this time instead of the location name/blank
            options.title((String) mSearchedFor);
            mSearchedFor = "";
        }

        if (country.length() > 0) {
            options.snippet(country);
        }


        return options;
    }



    // Polyline related

    /*
     * Refresh polyline from marker coordinates
     *
     * This one is called after a new marker has been added, when a marker is dragged and
     * when the activity resumes
     */

    public void updatePolyline(List<MarkerObject> markerList, GoogleMap gMap) {
        if (polyline==null) {
            Log.d(TAG, "UpdatePolyLine: ==null");
            PolylineOptions lineOptions = new PolylineOptions().geodesic(true);
            polyline = gMap.addPolyline(lineOptions);
        }

        List<LatLng> points = new ArrayList<>();
        for (int i=0;i<markerList.size();i++){
            points.add(markerList.get(i).getMarker().getPosition());
        }
        polyline.setPoints(points);

        updateMidpoints(markerList, gMap);

    }


    public void clearPolyline() {
        polyline.remove();
        polyline=null;
    }



    // Address related

    /**
     * This function is looking up a location and doing the reverse geo coding.
     *
     * @param location LatLng Location object
     * @Return Address An address object or null if nothing is found
     */
    public Address findAddress(LatLng location) {
        Geocoder gc = new Geocoder(getApplicationContext());
        List<Address> list = null;

        try {
            list = gc.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address adr=null;
        if (list!=null && list.size()!=0) {
            adr = list.get(0);
            Log.d(TAG, "findAddress: Got adr from lookup: "+adr);
        } else {
            Log.d(TAG, "findAddress: Got a null from address lookup");
        }
        return adr;
    }



    // Member Variables


    public String getSearchedFor() {
        return mSearchedFor;
    }
    public void setSearchedFor(String searchedFor) {
        mSearchedFor = searchedFor;
    }

    public float getZoomLevel() { return mZoomLevel; }
    public void setZoomLevel(float zoomLevel) {
        if (getZoomLevel()!=zoomLevel) {
            Log.d(TAG, "setZoomLevel: " + zoomLevel);

            String msg = String.format(Locale.ENGLISH,"Zoom level: %.1f", zoomLevel);
            SpannableStringBuilder ssb = new SpannableStringBuilder().append(msg);
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#ff333333")), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            Snackbar snackbar=Snackbar.make(getCurrentFocus(), ssb, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(0xdddddddd);
            snackbar.show();

            mZoomLevel = zoomLevel;
        }
    }



    // These are for showing/hiding specific components. Used after restoring preferences
    public boolean isHide_public_ad() {
        return mHide_public_ad;
    }

    public void setHide_public_ad(boolean hide_public_ad) {
        mHide_public_ad = hide_public_ad;
    }

    public boolean isHide_private_ad() {
        return mHide_private_ad;
    }

    public void setHide_private_ad(boolean hide_private_ad) {
        mHide_private_ad = hide_private_ad;
    }

    public boolean isHide_obstacles() {
        return mHide_obstacles;
    }

    public void setHide_obstacles(boolean hide_obstacles) {
        mHide_obstacles = hide_obstacles;
    }

    public boolean isHide_recreational_ad() {
        return mHide_recreational_ad;
    }

    public void setHide_recreational_ad(boolean hide_recreational_ad) {
        mHide_recreational_ad = hide_recreational_ad;
    }

    public boolean isHide_TMA() {
        return mHide_TMA;
    }

    public void setHide_TMA(boolean hide_TMA) {
        mHide_TMA = hide_TMA;
    }

    public boolean isHide_CTR() {
        return mHide_CTR;
    }

    public void setHide_CTR(boolean hide_CTR) {
        mHide_CTR = hide_CTR;
    }

    public boolean isHide_VOR() {
        return mHide_VOR;
    }

    public void setHide_VOR(boolean hide_VOR) {
        mHide_VOR = hide_VOR;
    }

    public boolean isHide_VORDME() {
        return mHide_VORDME;
    }

    public void setHide_VORDME(boolean hide_VORDME) {
        mHide_VORDME = hide_VORDME;
    }

    public boolean isHide_NDB() {
        return mHide_NDB;
    }

    public void setHide_NDB(boolean hide_NDB) {
        mHide_NDB = hide_NDB;
    }

    public boolean isHide_TACAN() {
        return mHide_TACAN;
    }

    public void setHide_TACAN(boolean hide_TACAN) {
        mHide_TACAN = hide_TACAN;
    }

    public boolean isHide_VORTAC() {
        return mHide_VORTAC;
    }

    public void setHide_VORTAC(boolean hide_VORTAC) {
        mHide_VORTAC = hide_VORTAC;
    }

    public boolean isHide_Locator() {
        return mHide_Locator;
    }

    public void setHide_Locator(boolean hide_Locator) {
        mHide_Locator = hide_Locator;
    }


    public boolean isHide_reporting_points() {
        return mHide_reporting_points;
    }

    public boolean isHide_DME() {
        return mHide_DME;
    }

    public void setHide_DME(boolean hide_DME) {
        mHide_DME = hide_DME;
    }

    public void setHide_reporting_points(boolean hide_reporting_points) {
        mHide_reporting_points = hide_reporting_points;
    }

    public boolean isMapTypeChangedByZoom() { return mMapTypeChangedByZoom; }
    public void setMapTypeChangedByZoom(boolean mapTypeChangedByZoom) {
        mMapTypeChangedByZoom = mapTypeChangedByZoom;
    }


    // Nav Aids Related

    /*
     * Plot NavAid icons on the map with customized markers
     * All markers are initially created and filed away in the m list. Everything is
     * Offset 50% in both directions, so they will center on the position of the AD.
     *
     *
     * Should the markers are already be created, all we need to do is resize the icon depending on
     * the Zoom level.
     *
     * For zoom levels above 16 the map type will be changed to Hybrid and there is no longer need
     * for this marker.
     *
     */
    public void plotNavAids(List<Marker> navAidMarkers, List<NavAid> navAidList, GoogleMap gMap) {

        Log.d(TAG, "plotNavAids: navAidList.size(): "+navAidList.size());
        Log.d(TAG, "plotNavAids: navAidMarkers.size(): "+navAidMarkers.size());

        String snippetText="invalid";
        int iconInt=R.drawable.ic_device_gps_blue;
        if (navAidMarkers.size() == 0) {
            // Create all Nav Aids markers and keep record in an ArrayList
            for (int i = 0; i < navAidList.size(); i++) {
                switch (navAidList.get(i).getType()){
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
                    case NavAid.LOCATOR:
                        iconInt=R.drawable.ic_device_gps_grey;
                        snippetText = "Locator";
                        break;
                }



                Marker m = gMap.addMarker(new MarkerOptions()
                        .title(navAidList.get(i).getIdent())
                        .snippet(snippetText+" "+navAidList.get(i).getFreq())
                        .position(navAidList.get(i).getPosition()).icon(BitmapDescriptorFactory.fromResource(iconInt)));

                m.setAnchor(.5f, .5f);
                navAidMarkers.add(m);
            }
        }


        // Hide/show selected markers
        for (int i = 0; i < navAidList.size(); i++) {
            NavAid na = navAidList.get(i);
            Marker m = navAidMarkers.get(i);

            // Double check the zoom level
            if (getZoomLevel() > NAVAID_MAX_ZOOM || getZoomLevel() < NAVAID_MIN_ZOOM) {
                // If exceeded then hide all markers in this category
                m.setVisible(false);

            } else {

                // Set visibility according to preferences
                switch (na.getType()) {
                    case NavAid.LOCATOR:
                        if (mHide_Locator) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                    case NavAid.VOR:
                        if (mHide_VOR) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                    case NavAid.DME:
                        if (mHide_DME) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                    case NavAid.VORDME:
                        if (mHide_VORDME) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                    case NavAid.NDB:
                        if (mHide_NDB) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                    case NavAid.TACAN:
                        if (mHide_TACAN) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                    case NavAid.VORTAC:
                        if (mHide_VORTAC) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                }
            }
        }

    }


    // AD Related

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
    public void plotAerodromes(List<Marker> adMarkers, List<Aerodrome> adList, GoogleMap gMap ) {

        Log.d(TAG, "plotAerodromes: Entering, adList.size(): "+adList.size());

        // Create the markers if not already there
        int iconPublicAirfield = R.drawable.ic_public_airfield;
        int iconPrivateAirfield = R.drawable.ic_private_airfield;
        int iconRecreationalAirfield = R.drawable.ic_recreational_airfield;
        if (adMarkers.size() == 0) {
            // Create all AD markers and keep record in an ArrayList
            for (int i = 0; i < adList.size(); i++) {
                int iconAD = 0;
                String title="";
                String note="";
                switch (adList.get(i).getType()) {
                    case Aerodrome.PUBLIC:
                        iconAD=iconPublicAirfield;
                        title=adList.get(i).getIcaoName();
                        note=adList.get(i).getName();
                        break;

                    case Aerodrome.PRIVATE:
                        iconAD=iconPrivateAirfield;
                        title=adList.get(i).getName();
                        if (!adList.get(i).getIcaoName().equals("")) title+=" ("+adList.get(i).getIcaoName()+")";
                        note="Private airfield ";
                        if (adList.get(i).getRemarks()!=null) note+=adList.get(i).getRemarks();
                        break;

                    case Aerodrome.RECREATIONAL:
                        iconAD=iconRecreationalAirfield;
                        title=adList.get(i).getName();
                        if (!adList.get(i).getIcaoName().equals("")) title+=" ("+adList.get(i).getIcaoName()+")";
                        note=adList.get(i).getRemarks();
                        String activity=adList.get(i).getActivity().toUpperCase();
                        String extraNote="";
                        if (activity.contains("HG")) {
                            extraNote+="Hang Gliders";
                            activity.replace("HG","");
                        }
                        if(activity.contains("G")) {
                            extraNote+=" Gliders";
                            activity.replace("G","");
                        }
                        if(activity.contains("CL")) {
                            extraNote+=" Cable Launch";
                            activity.replace("CL","");
                        }
                        if(activity.contains("P")) {
                            extraNote+=" Parachutes";
                        }
                        if (note.equals("")) {
                            note=extraNote;
                        }
                        break;
                }
                Marker m = gMap.addMarker(new MarkerOptions()
                        .title(title)
                        .snippet(note)
                        .position(adList.get(i).getPosition())
                        .icon(BitmapDescriptorFactory.fromResource(iconAD)));

                m.setAnchor(0.5f, .5f);
                adMarkers.add(m);
            }
        }

        // Hide/show selected markers
        for (int i = 0; i < adList.size(); i++) {
            Aerodrome ad = adList.get(i);
            Marker m = adMarkers.get(i);

            // Double check the zoom level
            if (getZoomLevel() > AERODROME_MAX_ZOOM || getZoomLevel() < AERODROME_MIN_ZOOM) {

                // If outside limits, hide all markers of this category
                m.setVisible(false);

            } else {

                // Set visibility according to preferences
                switch (ad.getType()) {
                    case Aerodrome.PUBLIC:
                        if (mHide_public_ad) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                    case Aerodrome.PRIVATE:
                        if (mHide_private_ad) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;

                    case Aerodrome.RECREATIONAL:
                        if (mHide_recreational_ad) {
                            m.setVisible(false);
                        } else {
                            m.setVisible(true);
                        }
                        break;
                }
            }
        }

    }


    // Reporting Points Related

    /**
     * Plot Reporting Point icons on the map with customized markers.
     *
     * All markers are initially created and filed away in the mRPMarkers list. Everything is
     * Offset in both directions, so they will center on the position of the RP.
     *
     * param rpMarkers  A list of Reporting Point Markers
     * param rpList     A list of Reporting Point Objects
     * param gMap       A handle to the map object
     *
     */
    public void plotReportingPoints(List<Marker> rpMarkers, List<ReportingPoint> rpList, GoogleMap gMap ) {

        Log.d(TAG, "plotReportingPoints: Entering, rpList.size(): "+rpList.size());

        // Create the markers if not alrady there
        int iconReportingPoint = R.drawable.ic_reporting_point;
        if (rpMarkers.size() == 0) {
            // Create all RP markers and keep record in an ArrayList
            for (int i = 0; i < rpList.size(); i++) {
                Marker m = gMap.addMarker(new MarkerOptions()
                        .title(rpList.get(i).getName())
                        .snippet(rpList.get(i).getAerodrome()+" Reporting Point")
                        .position(rpList.get(i).getPosition()).icon(BitmapDescriptorFactory.fromResource(iconReportingPoint)));

                m.setAnchor(0.5f, .6f);
                rpMarkers.add(m);
            }
        }

        if (isHide_reporting_points() || getZoomLevel() > AERODROME_MAX_ZOOM || getZoomLevel()< AERODROME_MIN_ZOOM) {
            for (Marker m : rpMarkers) {
                m.setVisible(false);
            }
        } else {

            // Update size of each ADMarker relative to zoom level
            for (Marker m : rpMarkers) {
                m.setVisible(true);
            }
        }
    }


    /**
     * Plot Reporting Point icons on the map with customized markers.
     *
     * All markers are initially created and filed away in the mRPMarkers list. Everything is
     * Offset in both directions, so they will center on the position of the RP.
     *
     * param rpMarkers  A list of Reporting Point Markers
     * param rpList     A list of Reporting Point Objects
     * param gMap       A handle to the map object
     *
     */
    public void plotObstacles(List<Marker> oMarkers, List<Obstacle> oList, GoogleMap gMap ) {

        Log.d(TAG, "plotObstacles: Entering, oList.size(): "+oList.size());

        // Create the markers if not already there
        int iconObstacle = R.drawable.ic_obstacle;
        if (oMarkers.size() == 0) {
            // Create all Obstacle markers and keep record in an ArrayList
            for (int i = 0; i < oList.size(); i++) {
                Marker m = gMap.addMarker(new MarkerOptions()
                        .title(oList.get(i).getName())
                        .snippet(String.format(Locale.ENGLISH, "%d ft/ %d ft", oList.get(i).getElevation(), oList.get(i).getHeight()))
                        .position(oList.get(i).getPosition())
                        .icon(BitmapDescriptorFactory.fromResource(iconObstacle)));

                m.setAnchor(0.5f, .5f);
                oMarkers.add(m);
            }
        }

        if (isHide_obstacles() || getZoomLevel() > OBSTACLE_MAX_ZOOM || getZoomLevel()< OBSTACLE_MIN_ZOOM) {
            for (Marker m : oMarkers) {
                m.setVisible(false);
            }
        } else {
            for (Marker m : oMarkers) {
                m.setVisible(true);
            }
        }
    }


    public void plotAreas(List<Polygon> polygonList, List<AreaItem> areaItemList, GoogleMap gMap) {

        Log.d(TAG, "plotAreas: ...");
        String color;

        if (polygonList.size() == 0) {
            // Create all area Polygons and keep record in an ArrayList

             for (AreaItem areaItem : areaItemList) {
                List<LatLng> coords = areaItem.getCoordinates();
                String name = areaItem.getAreaName();

                if (areaItem.getAreaType()==Area.CTR) {
                    color = CONTROL_AREA_COLOR;
                } else {
                    color = TERMINAL_AREA_COLOR;
                }

                Polygon polygon = gMap.addPolygon(new PolygonOptions()
                        .addAll(coords)
                      //  .strokeColor(Color.LTGRAY)
                        .strokeColor(Color.BLACK)
                     //   .strokeWidth(.5f)
                        .fillColor(Color.parseColor(color)));

                polygon.setClickable(true);
                polygonList.add(polygon);
            }
        }


        // show/hide each area depending on type and settings
        for (int i = 0; i < areaItemList.size(); i++) {
            switch (areaItemList.get(i).getAreaType()) {
                case AreaItem.CTR:
                    if (isHide_CTR()) {
                        polygonList.get(i).setVisible(false);
                    } else {
                        polygonList.get(i).setVisible(true);
                    }
                    break;

                case AreaItem.TMA:
                    if (isHide_TMA()) {
                        polygonList.get(i).setVisible(false);
                    } else {
                        polygonList.get(i).setVisible(true);
                    }
                    break;

                default:
                    polygonList.get(i).setVisible(true);
            }
        }

    }

}
