package com.example.tbrams.markerdemo.components;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OpenAirInterpreter {
    public static final String TAG = "OpenAirInterpreter";
    private static final int STEP_SIZE = 1;

    private ArrayList<LatLng> mCoordList;
    private LatLng mCenter;
    private int mStep_direction;


    public OpenAirInterpreter() {
        mStep_direction = 1;
        mCenter = null;
        mCoordList = new ArrayList<>();
    }

    /**
     * Interpret the OpenAir commands and return a list of LatLng coordinates.
     *
     * @param cmd String with "\n" separated list of OpenAir Commands
     * @return List of LatLng coordinates
     */
    public ArrayList<LatLng> generatePolygon(String cmd) {
        String[] cmds = cmd.trim().split("\n");   // trim() will remove a leading "\n" if any

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

}
