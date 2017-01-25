package com.example.tbrams.markerdemo.components;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;

import com.example.tbrams.markerdemo.R;
import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Pejling;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

public class MarkerDemoUtils extends AppCompatActivity {

    private static final String TAG = "TBR:MarkerDemoUtils";
    public static final int AERODROME_MAX_ZOOM = 14;
    public static final int AERODROME_MIN_ZOOM = 7;
    public static final int NAVAID_MAX_ZOOM = 16;
    public static final int NAVAID_MIN_ZOOM = 7;
    public static final int ZOOM_CHANGE_MAP_TYPE = 16;

    private final static List<Marker> midpointList = new ArrayList<>();
    private static Polyline polyline;
    private float mZoomLevel;
    private String mSearchedFor;
    private boolean mHideNavAidIcons = false;
    private boolean mHideADicons = false;
    private boolean mMapTypeChangedByZoom=false;






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
            plist.add(new Pejling(i, dist, heading));
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

    /*
     * This function is looking up a location and doing the geocoding.
     * Returning an address
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
        if (list!=null) {
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
        mZoomLevel = zoomLevel;
    }

    public boolean hideNavAidIcons() { return mHideNavAidIcons; }
    public void setHideNavAidIcons(boolean hideNavAidIcons) {
        mHideNavAidIcons = hideNavAidIcons;
    }

    public boolean hideADicons() { return mHideADicons; }
    public void setHideADicons(boolean hideADicons) {
        mHideADicons = hideADicons;
    }

    public boolean isMapTypeChangedByZoom() { return mMapTypeChangedByZoom; }
    public void setMapTypeChangedByZoom(boolean mapTypeChangedByZoom) {
        mMapTypeChangedByZoom = mapTypeChangedByZoom;
    }


    // NavAids Related

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
            // Create all NavAids markers and keep record in an ArrayList
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
                    case NavAid.LOCALIZER:
                        iconInt=R.drawable.ic_device_gps_grey;
                        snippetText = "LOCALIZER";
                        break;
                }



                Marker m = gMap.addMarker(new MarkerOptions()
                        .title(navAidList.get(i).getName())
                        .snippet(snippetText+" "+navAidList.get(i).getFreq())
                        .position(navAidList.get(i).getPosition()).icon(BitmapDescriptorFactory.fromResource(iconInt)));

                m.setAnchor(.5f, .5f);
                navAidMarkers.add(m);
            }
        }

        if ( hideNavAidIcons() || getZoomLevel() > NAVAID_MAX_ZOOM || getZoomLevel()< NAVAID_MIN_ZOOM) {
            // Preference off for AD markers - hide them
            for (Marker m : navAidMarkers) {
                m.setVisible(false);
            }
        } else {
            // Update size of each NavAidMarker relative to zoom level
            for (Marker m : navAidMarkers) {
                m.setVisible(true);
                //  m.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName)));
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

        // Create the markers if not alrady there
        String iconName = "ic_device_airplanemode_on";
        int iconInt = R.drawable.ic_device_airplanemode_on;
        if (adMarkers.size() == 0) {
            // Create all AD markers and keep record in an ArrayList
            for (int i = 0; i < adList.size(); i++) {
                Marker m = gMap.addMarker(new MarkerOptions()
                        .title(adList.get(i).getIcaoName())
                        .snippet(adList.get(i).getName())
                        .position(adList.get(i).getPosition()).icon(BitmapDescriptorFactory.fromResource(iconInt)));

                m.setAnchor(0.5f, .5f);
                adMarkers.add(m);
            }
        }



        if (hideADicons() || getZoomLevel() > AERODROME_MAX_ZOOM || getZoomLevel()< AERODROME_MIN_ZOOM) {
            for (Marker m : adMarkers) {
                m.setVisible(false);
            }
        } else {

            // Update size of each ADMarker relative to zoom level
            for (Marker m : adMarkers) {
                m.setVisible(true);
                //       m.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName)));
            }
        }
    }


}
