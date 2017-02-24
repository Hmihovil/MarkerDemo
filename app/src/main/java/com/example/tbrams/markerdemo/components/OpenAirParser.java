package com.example.tbrams.markerdemo.components;


import android.content.Context;
import android.util.Log;

import com.example.tbrams.markerdemo.dbModel.AreaItem;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.tbrams.markerdemo.components.Util.parseAltitude;

public class OpenAirParser {
    private static final String TAG = "TBR:OpenAirParser";
    private static final int STEP_SIZE = 1;
    private ArrayList<LatLng> mCoordList;
    private String mDefList;
    private LatLng mCenter;
    private int mStep_direction;
    private String mAirspaceClass;
    private String mAirspaceName;
    private int mAirspaceFrom;
    private int mAirspaceTo;
    private int mAirSpaceType;


    public OpenAirParser() {

        mDefList ="";
        mAirspaceClass = "";
        mAirspaceName = "";
        mAirspaceFrom = -8888;
        mAirspaceTo = -8888;
        mAirSpaceType = 0;

        mStep_direction = 1;
        mCenter = null;
        mCoordList = new ArrayList<>();
        mDefList="";

    }

    /**
     * Go through the lines of OpenAir commands and populate a list of AreaItems.
     * <p>
     * Will create a list of AreaItems, one for each OpenAir polygon specification. All specifications
     * are separated by the string "DONE".
     *
     * @param openAirCommands list of openAirCommand Strings
     * @return List of AreaItem objects
     */
    public List<AreaItem> parseInitialCommands(List<String> openAirCommands) {

        List<AreaItem> areaItemList = new ArrayList<>();
        for (String cmd : openAirCommands) {
            cmd = cmd.trim();
            if (cmd.equals("DONE")) {
                // If we have accumulated some definition commands by now, create an areaItem - otherwise just skip the comment
                if (mDefList.length() > 0) {
                    areaItemList.add(createAreaItem());
                }
            } else {
                // We are still not done, keep parsing until we have all the initial info we need for the AreaItem
                parseInitialCommand(cmd);
            }
        }

        // We might not have a final "DONE" statement, so we need this one
        AreaItem lastItem = createAreaItem();
        if (lastItem != null) {
            areaItemList.add(lastItem);
        }

        return areaItemList;
    }




    /**
     * Create AreaItem and reset internal variables used for this.
     *
     * @return AreaItem
     */
    private AreaItem createAreaItem() {

        AreaItem areaItem=null;
        if (mAirspaceName!="" && mAirSpaceType!=0 && mAirspaceFrom!=-8888 && mAirspaceTo!=-8888 && mAirspaceClass!="" && mDefList.length()>0) {

            areaItem = new AreaItem(null, mAirspaceName, mAirSpaceType, null, mAirspaceClass, mAirspaceFrom, mAirspaceTo, mDefList);

            // Reset internal storage
            mDefList = "";
            mAirspaceClass = "";
            mAirspaceName = "";
            mAirspaceFrom = -8888;
            mAirspaceTo = -8888;
            mAirSpaceType = 0;
        }

        return areaItem;
    }





    /**
     * Interpret the OpenAir commands and return a list of LatLng coordinates.
     *
     * @param cmd String with "\n" separated list of OpenAir Commands
     * @return List of LatLng coordinates
     */
    public ArrayList<LatLng> generatePolygon(String cmd) {
        String[] cmds = cmd.split("\n");

        mCoordList = new ArrayList<>();
        mCenter=null;
        mStep_direction=1;

        for (String oac : cmds) {
            InterpretCommand(oac);
        }

        return mCoordList;
    }


    /**
     * Interpret OpenAir Definition Command and generate a list of coordinates in the field
     * variable mCoordList as well as internal storage for center point and direction if needed.
     *
     * @param cmd A line from an OpenAir formatted file, for example "DP 39:29.9 N 119:46.1W"
     */

    public void InterpretCommand(String cmd) {

        // First pattern matches two groups - the main command and the rest of the line
        String pattern = "(AN|AC|AL|AH|AT|DC|DA|DP|DB|V|\\*) ([\\w\\d\\s\\:\\.\\=\\+\\-\\,]*)";
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
                case "V":
                    // Variable Assignment Command
                    String assignPattern = "([\\w]+)\\s*=([\\s\\w\\d\\:\\.\\+\\-]*)";
                    r = Pattern.compile(assignPattern);
                    m = r.matcher(rest);

                    if (m.find()) {
                        if (m.group(1).equals("D")) {
                            // Variable name D means this is a Direction assignment
                            if (m.group(2).equals("+")) {
                                mStep_direction = 1;
                            } else {
                                mStep_direction = -1;
                            }

                        } else {
                            // A position variable assignment, any variable name us supported although I have only seen X used
                            pos = parseCoordinateString(rest);
                            if (pos != null) {
                                mCenter = pos;
                            } else {
                                // If we cannot parse this as a position, we need to look into this later
                                Log.e(TAG, "interpretCommand: Unsupported assignment...");
                            }
                        }

                    } else {
                        // We did not find anything useful in the argument string after the name
                        Log.e(TAG, "interpretCommand: Variable argument parsing error");
                    }

                    break;


                case "DC":
                    // Draw Circle command - expect an decimal argument and ignore unless we have
                    // a center point on file at this point.
                    radius = (int) (Double.parseDouble(rest) * 1852);
                    pos = null;
                    if (mCenter != null) {
                        for (int deg = 0; deg < 360; deg++) {
                            pos = SphericalUtil.computeOffset(mCenter, radius, deg);
                            addPosToCoordList(pos);
                        }
                    }
                    break;


                case "DA":
                    // Draw Arc Command
                    // Pattern matches three comma separated integer aruments
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
                        Log.e(TAG, "interpretCommand: Draw arc parameters not recognized");
                    }
                    break;

                case "DP":
                    // Define Point Command
                    // Pattern matches a potential coordinate string

                    String coordPattern = "([\\d\\:\\. \\w]+)";
                    r = Pattern.compile(coordPattern);
                    m = r.matcher(rest);
                    if (m.find()) {
                        pos = parseCoordinateString(m.group(1));
                        addPosToCoordList(pos);
                    } else {
                        Log.e(TAG, "interpretCommand: Problem parsing DP argument");
                    }
                    break;


                case "DB":
                    // Draw Between Command
                    // Pattern matches two possible coordinates separated by a comma
                    String betweenPattern = "([\\d\\:\\. \\w]+) *, *([\\d\\:\\. \\w]+)";
                    r = Pattern.compile(betweenPattern);
                    m = r.matcher(rest);

                    if (m.find()) {
                        LatLng pos1, pos2;
                        pos1 = parseCoordinateString(m.group(1));
                        pos2 = parseCoordinateString(m.group(2));

                        if (pos1 != null && pos2 != null) {
                            fromDeg = ((int) SphericalUtil.computeHeading(mCenter, pos1) + 360) % 360;
                            toDeg = ((int) SphericalUtil.computeHeading(mCenter, pos2) + 360) % 360;
                            radius = (int) SphericalUtil.computeDistanceBetween(mCenter, pos1);
                            drawArcFromTo(radius, fromDeg, toDeg);
                        }
                    } else {
                        Log.e(TAG, "interpretCommand: Problem parsing draw between arguments");
                    }
                    break;

                default:
                    Log.e(TAG, "interpretCommand:  Cannot recognize command: " + cmd);
                    break;
            }


        } else {
            Log.e(TAG, "interpretCommand: Unexpected input: " + cmd);
        }
    }


    /**
     * Append OpenAir Command to string of commands separated by newline.
     *
     * Utility function used in first command parsing phase.
     * Will append to field variable mDefList.
     *
     * @param oac String, Open Air Command
     */

    private void appendDefinition(String oac) {
        mDefList+="\n"+oac;
    }


    /**
     * Parse Initial OpenAir Commands - but just append new area definition commands to the mDefList String.
     *
     * This function is used to avoid having to perform thousands of database inserts when
     * working with complex polygons. We simply postpone the coordinate calculation until
     * import time when the definitions will be interpreted.
     *
     * @param cmd A line from an OpenAir formatted file, for example "DP 39:29.9 N 119:46.1W"
     */
    public void parseInitialCommand(String cmd) {

        // First pattern matches two groups - the main command and the rest of the line
        String pattern = "(AN|AC|AL|AH|AT|DC|DA|DP|DB|V|\\*) ([\\w\\d\\s\\:\\.\\=\\+\\-\\,]*)";
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
                    break;

                case "AT":
                    // OpenAir Language extension: Airspace Type
                    if (rest.equals("CTR")) {
                        mAirSpaceType = AreaItem.CTR;
                    } else if (rest.equals("TMA")) {
                        mAirSpaceType = AreaItem.TMA;
                    } else if (rest.equals("LTA")) {
                        mAirSpaceType = AreaItem.LTA;
                    } else if (rest.equals("TIZ")) {
                        mAirSpaceType = AreaItem.TIZ;
                    } else if (rest.equals("TIA")) {
                        mAirSpaceType = AreaItem.TIA;
                    } else if (rest.equals("D")) {
                        mAirSpaceType=AreaItem.DANGER;
                    } else if (rest.equals("R")) {
                        mAirSpaceType=AreaItem.RESTRICTED;
                    } else if (rest.equals("P")) {
                        mAirSpaceType=AreaItem.PROHIBITED;
                    } else if (rest.equals("GLIDER")) {
                        mAirSpaceType=AreaItem.GLIDER;
                    } else if (rest.equals("PARACHUTE")) {
                        mAirSpaceType=AreaItem.PARACHUTE;
                    } else if (rest.equals("ENV")) {
                        mAirSpaceType=AreaItem.SENSITIVE;
                    } else {
                        Log.e(TAG, "parseInitialCommand: unknown Airspace type - "+ rest);
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

                case "DC":   // These commands are all postponed
                case "V":
                case "DA":
                case "DP":
                case "DB":
                    appendDefinition(cmd);
                    break;

                default:
                    Log.e(TAG, "parseInitialCommand: Cannot parse command: " + cmd);
                    break;
            }


        } else {
            Log.e(TAG, "parseInitialCommand: Unrecognized input: " + cmd);
        }
    }



    /**
     * Utility function producing Arc coordinates with a given radius between to headings.
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



    private void addPosToCoordList(LatLng newPos) {
        mCoordList.add(newPos);
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

}
