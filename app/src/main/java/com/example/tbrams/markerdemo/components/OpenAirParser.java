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
    private String mDefList;
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

}
