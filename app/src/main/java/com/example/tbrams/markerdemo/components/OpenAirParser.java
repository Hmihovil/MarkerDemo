package com.example.tbrams.markerdemo.components;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.example.tbrams.markerdemo.dbModel.AreaItem;
import com.example.tbrams.markerdemo.dbModel.CoordItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.tbrams.markerdemo.components.Util.parseAltitude;

public class OpenAirParser {
    private static final String TAG = "TBR:OpenAirParser";
    private static final int STEP_SIZE = 1;
    private static final float DEFAULT_LINE_WIDTH = 10f;
    private static final float SPECIAL_LINE_WIDTH = 25f;

    private static final String COLOR_UNDEFINED = "#FFFF00";
    private static final String COLOR_G = "#B0C4DE";   // LightSteelBlue
    private static final String COLOR_E = "#BA55D3";   // MediumOrchid
    private static final String COLOR_D = "#7B68EE";   // MediumSlateBlue
    private static final String COLOR_C = "#C71585";   // MediumVioletRed
    private static final String COLOR_B = "#1E90FF";   // DodgerBlue
    private static final String COLOR_R = "#CD853F";   // Peru
    private static final String COLOR_W = "#CD5C5C";   // IndianRed
    private static final String COLOR_P = "#B22222";   // Firebrick

    private LatLng mCenter = null;
    private int mStep_direction = 1;
    private ArrayList<LatLng> mCoordList = new ArrayList<>();
    private String mOutlineColor;
    private float mOutlineWidth;
    private Context mContext;
    private String mAirspaceClass = "";
    private String mAirspaceName = "";
    private int mAirspaceFrom = 0;
    private int mAirspaceTo = 0;
    private int mAirSpaceType = 0;


    public OpenAirParser(Context context) {
        mContext = context;
    }


    /**
     * Go through the lines of OpenAir commands and process accordingly.
     * <p>
     * Will create a list of AreaItems, one for each OpenAir polygon specification. All specifications
     * are specated by empty cells, or cells with an Asterix in first position as per convention
     *
     * @param openAirCommands list of openAirCommand Strings
     * @return List of AreaItem objects
     */
    public List<AreaItem> parseCommands(List<String> openAirCommands) {

        List<AreaItem> areaItemList = new ArrayList<>();
        for (String cmd : openAirCommands) {
            cmd = cmd.trim();
            if (cmd.equals("DONE") || cmd.charAt(0) == '*') {
                // If we have accumulted some coordinates so far, create an area - otherwise skip the comment
                if (mCoordList.size() > 0) {
                    areaItemList.add(createAreaAndReset());
                }
            } else {
                parseCommand(cmd);
            }
        }
        areaItemList.add(createAreaAndReset());

        return areaItemList;
    }

    /**
     * Utility function making it easy to dump data and reset internal storage.

     private void plotAndReset() {

     // Create a Google Maps Polygon and populate it with coordinates interpreted form the OpenAir spec.
     PolygonOptions polyOptions = new PolygonOptions();
     for (LatLng pos : mCoordList) {
     polyOptions.add(pos);
     }

     // Use outline only to keep cluttering at a minimum, color of outline is defined by Airspace
     polyOptions.strokeColor(Color.parseColor(mOutlineColor));
     polyOptions.strokeWidth(mOutlineWidth);

     // Display polygon
     Polygon polygon = mMap.addPolygon(polyOptions);

     // Reset internal storage
     mCoordList = new ArrayList<>();
     mOutlineColor = COLOR_UNDEFINED;
     mOutlineWidth=DEFAULT_LINE_WIDTH;
     }
     */


    /**
     * Utility function making it easy to create AreaItems and reset internal storage.
     *
     * @return AreaItem
     */
    private AreaItem createAreaAndReset() {

        // Create an Area Item and populate it with coordinates interpreted form the OpenAir spec.
        AreaItem areaItem = new AreaItem(null, mAirspaceName, mAirSpaceType, null, mAirspaceClass, mAirspaceFrom, mAirspaceTo);

        List<CoordItem> coordItemList = new ArrayList<>();
        for (int i = 0; i < mCoordList.size(); i++) {
            coordItemList.add(new CoordItem(null, areaItem.getAreaId(), mCoordList.get(i), i));
        }
        areaItem.setCoordItemList(coordItemList);

        // Reset internal storage
        mCoordList = new ArrayList<>();
        mAirspaceClass = "";
        mAirspaceName = "";
        mAirspaceFrom = -8888;
        mAirspaceTo = -8888;

        return areaItem;
    }

    /**
     * Parse coordinates in the String 'openAir format.
     * <p>
     * Uses a Regular Expression to parse the components of a coordinate string, convert into double
     * and create a LatLng object that can be used in Google Maps.
     *
     * @param coordString for example "39:29.9 N 119:46.1W" or "39 : 29:9 N 119:46 :1W" for KRNO airport
     * @return LatLng object
     */
    public LatLng parseCoordinateString(String coordString) {
        String pattern = "([\\d]+) *: *([\\d]+) *[:.] *([\\d])+ *([NS]) *([\\d]+) *: *([\\d]+) *[:.] *([\\d]+) *([EW])";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(coordString);
        if (m.find()) {

            // Given a string like "39:29.9 N 119:46.1W" we will get 8 matches:
            // "39", "29", "9", "N" and "119", "46", "1", "W" starting at index 1

            Double lat, lon;
            lat = Double.parseDouble(m.group(1)) + Double.parseDouble(m.group(2)) / 60 + Double.parseDouble(m.group(3)) / 3600;
            lon = Double.parseDouble(m.group(5)) + Double.parseDouble(m.group(6)) / 60 + Double.parseDouble(m.group(7)) / 3600;

            if (m.group(4).toUpperCase().equals("S")) lat = lat * -1;
            if (m.group(8).toUpperCase().equals("W")) lon = lon * -1;

            return new LatLng(lat, lon);

        } else {
            Log.e(TAG, "parseCoordinateString: Cannot parse coordinate String: " + coordString);
            return null;
        }
    }


    /**
     * Parse OpenAir Commands.
     * Currently only supports a subset of the many available commands - feel free to extend at your own discretion.
     *
     * @param cmd A line from an OpenAir formatted file, for example "DP 39:29.9 N 119:46.1W"
     */
    public void parseCommand(String cmd) {

        // First pattern matches two groups - the main command and the rest of the line
        String pattern = "(AN|AC|AL|AH|DC|DA|DP|DB|V|\\*) ([\\w\\d\\s\\:\\.\\=\\+\\-\\,]*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(cmd);

        if (m.find()) {
            String command = m.group(1).toUpperCase();
            String rest = m.group(2).trim().toUpperCase();

            LatLng pos = null;
            int radius;
            int fromDeg;
            int toDeg;

            switch (command) {
                case "*":
                    // Comment - do nothing
                    Log.d(TAG, "parseCommand, comment : " + rest);
                    break;

                case "AT":
                    // OpenAir Language extension: Airspace Type
                    switch (rest) {
                        case "CTR":
                            mAirSpaceType = AreaItem.CTR;
                            break;
                        case "TMA":
                            mAirSpaceType = AreaItem.TMA;
                            break;
                        case "LTA":
                            mAirSpaceType = AreaItem.LTA;
                            break;
                        case "TIZ":
                            mAirSpaceType = AreaItem.TIZ;
                            break;
                        case "TIA":
                            mAirSpaceType = AreaItem.TIA;
                            break;
                        case "D":
                            mAirSpaceType=AreaItem.DANGER;
                            break;
                        case "R":
                            mAirSpaceType=AreaItem.RESTRICTED;
                            break;
                        case "P":
                            mAirSpaceType=AreaItem.PROHIBITED;
                            break;
                        case "GLIDER":
                            mAirSpaceType=AreaItem.GLIDER;
                            break;
                        case "PARACHUTE":
                            mAirSpaceType=AreaItem.PARACHUTE;
                            break;
                    }
                    break;

                case "AC":
                    mAirspaceClass = rest;
                    break;


                case "AN":
                    mAirspaceName = rest;
                    break;

                case "AL":
                    mAirspaceFrom = parseAltitude(rest);
                    break;

                case "AH":
                    mAirspaceTo = parseAltitude(rest);
                    break;

                case "DC":
                    Log.d(TAG, "parseCommand: Draw Circle");

                    // Draw Circle command - expect an decimal argument
                    radius = (int) (Double.parseDouble(rest) * 1852);
                    pos = null;
                    if (mCenter != null) {
                        for (int deg = 0; deg < 360; deg++) {
                            pos = SphericalUtil.computeOffset(mCenter, radius, deg);
                            addPosToCoordList(pos);
                        }
                    }
                    break;

                case "V":
                    // Variable Assignment Command
                    // The pattern matches a variable name and the value argument from the rest of the line above

                    Log.d(TAG, "parseCommand: Variable assignment");

                    String assignPattern = "([\\w]+)\\s*=([\\s\\w\\d\\:\\.\\+\\-]*)";
                    r = Pattern.compile(assignPattern);
                    m = r.matcher(rest);

                    if (m.find()) {
                        if (m.group(1).equals("D")) {
                            // Variable name D means this is a Direction assignment
                            Log.d(TAG, String.format("Direction command, sign: %s", m.group(2)));
                            if (m.group(2).equals("+")) {
                                mStep_direction = 1;
                            } else {
                                mStep_direction = -1;
                            }

                        } else {
                            // A position variable assignment, any variable name us supported although I have only seen X used
                            Log.d(TAG, String.format("Variable assignment: %s identified, remaining arguments: %s", m.group(1), m.group(2)));

                            pos = parseCoordinateString(rest);
                            if (pos != null) {
                                Log.d(TAG, "Setting mCenter to: " + pos);
                                mCenter = pos;

                            } else {
                                // If we cannot parse this as a position, we need to look into this later
                                Log.e(TAG, "parseCommand: Unsupported assignment...");
                            }
                        }

                    } else {
                        // We did not find anything useful in the argument string after the name

                        Log.d(TAG, "parseCommand: Variable argument parsing error");
                    }

                    break;


                case "DA":
                    // Draw Arc Command
                    // Pattern matches three comma separated integer aruments

                    Log.d(TAG, "parseCommand: Draw Arc command");

                    String threeArgsPattern = "([\\d]+)\\s*\\,\\s*([\\d]+)\\s*\\,\\s*([\\d]+)";
                    r = Pattern.compile(threeArgsPattern);
                    m = r.matcher(rest);

                    if (m.find()) {
                        radius = Integer.parseInt(m.group(1)) * 1852;
                        fromDeg = Integer.parseInt(m.group(2));
                        toDeg = Integer.parseInt(m.group(3));
                        drawArcFromTo(radius, fromDeg, toDeg);
                    } else {
                        // We did not find the expected three integers in the argument string
                        Log.e(TAG, "parseCommand: Draw arc parameters not recognized");
                    }
                    break;

                case "DP":
                    // Define Point Command
                    // Pattern matches a potential coordinate string

                    Log.d(TAG, "parseCommand: Draw Point Commannd");

                    String coordPattern = "([\\d\\:\\. \\w]+)";
                    r = Pattern.compile(coordPattern);
                    m = r.matcher(rest);
                    if (m.find()) {
                        pos = parseCoordinateString(m.group(1));
                        addPosToCoordList(pos);

                        Log.d(TAG, "Got a coordinate : " + pos);

                    } else {
                        Log.e(TAG, "parseCommand: Problem parsing DP argument");
                    }
                    break;


                case "DB":
                    // Draw Between Command
                    Log.d(TAG, "parseCommand: Draw between command");

                    // Pattern matches two possible coordinates separated by a comma
                    String betweenPattern = "([\\d\\:\\. \\w]+) *, *([\\d\\:\\. \\w]+)";
                    r = Pattern.compile(betweenPattern);
                    m = r.matcher(rest);

                    if (m.find()) {
                        LatLng pos1, pos2;
                        pos1 = parseCoordinateString(m.group(1));
                        pos2 = parseCoordinateString(m.group(2));
                        Log.d(TAG, "parseCommand: Got two coordinates : " + pos1 + " and " + pos2);

                        if (pos1 != null && pos2 != null) {
                            fromDeg = ((int) SphericalUtil.computeHeading(mCenter, pos1) + 360) % 360;
                            toDeg = ((int) SphericalUtil.computeHeading(mCenter, pos2) + 360) % 360;
                            radius = (int) SphericalUtil.computeDistanceBetween(mCenter, pos1);
                            drawArcFromTo(radius, fromDeg, toDeg);
                        }
                    } else {
                        Log.e(TAG, "parseCommand: Problem parsing draw between arguments");
                    }
                    break;

                default:
                    Log.d(TAG, "parseCommand: not recognized");
                    break;
            }


        } else {
            Log.e(TAG, "parseCommand: Cannot parse command: " + cmd);
        }
    }

    /**
     * Utility function producing Arc coodinates with a given radius between to headings.
     * <p>
     * Requires a center point to be in place - will ignore command if not defined.
     *
     * @param radius
     * @param fromDeg
     * @param toDeg
     */
    private void drawArcFromTo(int radius, int fromDeg, int toDeg) {
        if (mCenter != null) {
            double x, y;
            LatLng newPos;
            int degrees = fromDeg;
            int step = mStep_direction * STEP_SIZE;
            do {
                newPos = SphericalUtil.computeOffset(mCenter, radius, degrees);
                addPosToCoordList(newPos);
                degrees += step;
                if (Math.abs(((degrees + 360) % 360) - toDeg) < STEP_SIZE)
                    break;
            } while (true);

        }
    }

    /**
     * Utility function converting navigation headings to normal math angle notation.
     * <p>
     * For example in Navigation 270 degrees is West, but in a coordinate system this is more like south.
     * Though i would need this, but will just leave it here anyway...
     *
     * @param compass navigational degrees
     * @return corodinate system degrees
     */
    public double compasToMathDegrees(double compass) {
        return (double) (((90 - compass) + 360) % 360);
    }


    private void addPosToCoordList(LatLng pos) {
        mCoordList.add(pos);
    }

}
