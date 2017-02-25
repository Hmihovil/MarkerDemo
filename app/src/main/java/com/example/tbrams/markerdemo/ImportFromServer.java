package com.example.tbrams.markerdemo;


import android.app.Activity;
import android.util.Log;

import com.example.tbrams.markerdemo.components.OpenAirParser;
import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Obstacle;
import com.example.tbrams.markerdemo.data.ReportingPoint;
import com.example.tbrams.markerdemo.db.DbAdmin;
import com.example.tbrams.markerdemo.dbModel.AreaItem;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImportFromServer {

    public static final String TAG = "TBR:ImportFromServer";
    private static final String SPREADSHEET_ID = "1G3rMDgZqItvOUVfxZOaIpTTg1YnR4UfxKvX9wEeZUkc";
    private Sheets mService = null;
    private Activity mActivity=null;

    public ImportFromServer(Sheets service, Activity activity) {
        this.mService=service;
        this.mActivity=activity;

    }

    public String getItAll() throws IOException {
        String result="";

        result+= getNavAids("NavAids!A2:H");
        result+=getPublicAerodromes("PublicAerodromes!A2:J");
        result+=getRecreationalAerodromes("Recreational!A2:E");
        result+=getPrivateAerodromes("PrivateAerodromes!A2:I");
        result+=getReportingPoints("ReportingPoints!A2:C");
        result+=getObstacles("Obstacles!A2:E");
        result+=getOpenAirAreas("TMA2!A3:B");

        return result;
    }


    /**
     * Fetch a list of Navaid names and locations from a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return List of names and majors
     * @throws IOException
     */
    private String getNavAids(String range) throws IOException {
        List<String> results = new ArrayList<String>();
        ValueRange response = this.mService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();


        List<NavAid> navAidsList = new ArrayList<>();
        List<List<Object>> values = response.getValues();

        // Now we have an array packed with Strings from the spreadsheet
        // column 0: Station [STRING]
        // column 1: Identifier [STRING]
        // Column 2: TYPE  [String, VOR, DME, VOR/DME, NDB, L, TACAN, VORTAC]
        // column 3: Location [String, format "55 13 44.18N 009 12 50.61E"]
        // Column 4: FREQ [OPT String]
        // Column 5: USAGE [OPT String]
        // Column 6: ELEV [OPT Double]
        // Column 7: MIL FREQ [OPT String]

        int nType = 0;

        if (values != null) {

            for (List row : values) {
                String sType = row.get(2).toString();
                if (sType.equals("VOR")) {
                    nType = NavAid.VOR;
                } else if (sType.equals("DME")) {
                    nType = NavAid.DME;
                } else if (sType.equals("VOR/DME")) {
                    nType = NavAid.VORDME;
                } else if (sType.equals("NDB")) {
                    nType = NavAid.NDB;
                } else if (sType.equals("L")) {
                    nType = NavAid.LOCATOR;
                } else if (sType.equals("TACAN")) {
                    nType = NavAid.TACAN;
                } else if (sType.equals("VORTAC")) {
                    nType = NavAid.VORTAC;
                } else {
                    Log.d(TAG, "getNavAids: Unrecognized NavAid Type: " + row.get(2));
                }

                Log.d(TAG, "getNavAids: nType: " + nType);


                String station = row.get(0).toString();
                String ident = row.get(1).toString();
                String location = row.get(3).toString();

                String freq = null;
                if (!row.get(4).toString().equals("")) {
                    freq = row.get(4).toString();
                }

                String usage = null;
                if (!row.get(5).toString().equals("")) {
                    usage = row.get(5).toString();
                }

                double elev = 0;
                if (row.size() > 6) {
                    if (!row.get(6).toString().equals("")) {
                        elev = Double.parseDouble(row.get(6).toString());
                    }
                }

                String milfreq = null;
                if (row.size() > 7) {
                    if (!row.get(7).toString().equals("")) {
                        milfreq = row.get(7).toString();
                    }
                }


//                    // TODO: Need a constructor taking MilFreq as well
                NavAid na = new NavAid(station, ident, nType, location, freq, usage, elev);
                Log.d(TAG, "getNavAids: New Navaid name: " + na.getName() + " @" + na.getPosition());
                navAidsList.add(na);

            }

            // update database
            // This is a clean up operation, meaning the Nav Aids table will be wiped before
            // inserting these data again.
            DbAdmin dbAdmin = new DbAdmin(mActivity);
            dbAdmin.updateNavAidsFromMaster(navAidsList, true);

        }
        return "NavAids updated\n";
    }



    /**
     * Fetch a list of Public Aerodromes names and locations from a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return List of names and majors
     * @throws IOException
     */
    private String getPublicAerodromes(String range) throws IOException {
        List<String> results = new ArrayList<String>();
        ValueRange response = this.mService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();


        List<Aerodrome> adList = new ArrayList<>();
        List<List<Object>> values = response.getValues();

        // Now we have an array packed with Strings from the spreadsheet
        // column 0: NAME [STRING]
        // column 1: ICAO [STRING]
        // Column 2: Location [String, format "55 13 44.18N 009 12 50.61E"]
        // column 3: RADIO [OPT String]
        // Column 4: FREQ [OPT String]
        // Column 5: PHONE [OPT String]
        // Column 6: WEB [OPT Double]
        // Column 7: PPR [OPT String, "Yes"]
        // Column 8: REMARKS [OPT String]
        // Column 9: LINK (OPT String)

        boolean PPR = false;
        String link = "";

        if (values != null) {
            for (List row : values) {
                if (row.size() > 6) {
                    String sPPR = row.get(7).toString().toUpperCase();
                    if (sPPR.equals("YES")) {
                        PPR = true;
                    } else {
                        PPR = false;
                    }
                }

                String name = row.get(0).toString();
                String icao = row.get(1).toString();
                String location = row.get(2).toString();

                String radio = null;
                if (!row.get(3).toString().equals("")) {
                    radio = row.get(4).toString();
                }

                String freq = null;
                if (!row.get(4).toString().equals("")) {
                    freq = row.get(4).toString();
                }

                String phone = null;
                if (!row.get(5).toString().equals("")) {
                    phone = row.get(5).toString();
                }

                String web = null;
                if (!row.get(5).toString().equals("")) {
                    web = row.get(5).toString();
                }

                if (row.size() > 8) {
                    link = row.get(8).toString().toUpperCase();
                }

                // TODO: Need a constructor taking Remarks as well
                Aerodrome ad = new Aerodrome(icao, name, location, Aerodrome.PUBLIC, radio, freq, PPR, link);
                Log.d(TAG, "getData: New Public Aerodrome: " + ad.getName() + " @" + ad.getPosition());
                adList.add(ad);

            }

            // update database - with publich being the first, we will clear out the database as well
            // And start on a clean sheet.
            DbAdmin dbAdmin = new DbAdmin(mActivity);
            dbAdmin.updateAerodromesFromMaster(adList, true);

        }
        return "Public Aerodromes updated\n";
    }



    /**
     * Fetch a list of PrivateAerodromes names and locations from a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return List of names and majors
     * @throws IOException
     */
    private String getPrivateAerodromes(String range) throws IOException {
        List<String> results = new ArrayList<String>();
        ValueRange response = this.mService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();


        List<Aerodrome> adList = new ArrayList<>();
        List<List<Object>> values = response.getValues();

        // Now we have an array packed with Strings from the spreadsheet
        // column 0: NAME [STRING]
        // column 1: ICAO [STRING]
        // Column 2: Location [String, format "55 13 44.18N 009 12 50.61E"]
        // column 3: RADIO [OPT String]
        // Column 4: FREQ [OPT String]
        // Column 5: PHONE [OPT String]
        // Column 6: WEB [OPT Double]
        // Column 7: PPR [OPT String, "Yes"]
        // Column 8: REMARKS [OPT String]

        boolean PPR = false;

        if (values != null) {
            for (List row : values) {
                if (row.size() > 6) {
                    String sPPR = row.get(7).toString().toUpperCase();
                    if (sPPR.equals("YES")) {
                        PPR = true;
                    } else {
                        PPR = false;
                    }
                }

                String name = row.get(0).toString();
                String icao = row.get(1).toString();
                String location = row.get(2).toString();

                String radio = null;
                if (!row.get(3).toString().equals("")) {
                    radio = row.get(4).toString();
                }

                String freq = null;
                if (!row.get(4).toString().equals("")) {
                    freq = row.get(4).toString();
                }

                String phone = null;
                if (!row.get(5).toString().equals("")) {
                    phone = row.get(5).toString();
                }

                String web = null;
                if (!row.get(5).toString().equals("")) {
                    web = row.get(5).toString();
                }


                // TODO: Need a constructor taking Remarks as well
                Aerodrome ad = new Aerodrome(icao, name, location, Aerodrome.PRIVATE, radio, freq, PPR, null);
                Log.d(TAG, "getData: New Private Aerodrome: " + ad.getName() + " @" + ad.getPosition());
                adList.add(ad);

            }

            // update database
            // This is an append operation, keep everything in the database with the risk of getting
            // duplicates now.

            // TODO: Remove existing ads of this type first
            DbAdmin dbAdmin = new DbAdmin(mActivity);
            dbAdmin.updateAerodromesFromMaster(adList, false);

        }
        return "Private Aerodromes updated\n";
    }


    /**
     * Fetch a list of Recreational Aerodromes names and locations from a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return List of names and majors
     * @throws IOException
     */
    private String getRecreationalAerodromes(String range) throws IOException {
        List<String> results = new ArrayList<String>();
        ValueRange response = this.mService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();


        List<Aerodrome> adList = new ArrayList<>();
        List<List<Object>> values = response.getValues();

        // Now we have an array packed with Strings from the spreadsheet
        // column 0: NAME [STRING]
        // column 1: ICAO [OPT STRING]
        // Column 2: Location [String, format "56 01 08N 008 40 55E" or "56 00 10.2N 009 05 34.8E"]
        // column 3: ACTIVITIES [OPT String], "P: Parachuting", "HG: Hang Gliders", "G: Gliders", "CL: Cable Launch".
        //                                    sometimes comma separated
        // Column 4: REMARKS [OPT String]

        if (values != null) {
            for (List row : values) {

                String name = row.get(0).toString();
                String icao = row.get(1).toString();
                String location = row.get(2).toString();
                String activities = row.get(3).toString();

                String remarks = "";
                if (row.size() > 4) {
                    remarks = row.get(4).toString();
                }

                // TODO: Need a constructor taking Remarks as well
                Aerodrome ad = new Aerodrome(name, icao, location, Aerodrome.RECREATIONAL, activities, remarks);
                Log.d(TAG, "getData: New Recreational Aerodrome: " + ad.getName() + " @" + ad.getPosition());
                adList.add(ad);

            }

            // update database
            // This is an append operation, keep everything in the database with the risk of getting
            // duplicates now.

            // TODO: Remove existing ads of this type first
            DbAdmin dbAdmin = new DbAdmin(mActivity);
            dbAdmin.updateAerodromesFromMaster(adList, false);

        }

        return "Recreational Aerodromes updated\n";
    }



    /**
     * Fetch a list of Aerodrome Reporting Points from a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return List of names and majors
     * @throws IOException
     */
    public String getReportingPoints(String range) throws IOException {

        List<String> results = new ArrayList<String>();
        ValueRange response = this.mService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();


        List<ReportingPoint> rpList = new ArrayList<>();
        List<List<Object>> values = response.getValues();

        // Now we have an array packed with Strings from the spreadsheet
        // column 0: ICAO AD Name[STRING]
        // column 1: NAME Reporting Point[STRING]
        // Column 2: Location [String, format "55 13 44.18N 009 12 50.61E"]

        String link = "";

        if (values != null) {
            for (List row : values) {

                String icao = row.get(0).toString();
                String name = row.get(1).toString();
                String location = row.get(2).toString();


                ReportingPoint rp = new ReportingPoint(icao, name, location);
                Log.d(TAG, "getData: Reporting Point: " + rp.getName() + " @" + rp.getAerodrome() + " " + rp.getPosition());
                rpList.add(rp);

            }

            // update database
            DbAdmin dbAdmin = new DbAdmin(mActivity);
            dbAdmin.updateReportingPointsFromMaster(rpList, true);

        }
        return "Reporting Points updated\n";
    }


    /**
     * Fetch a list of Obstacles from a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return List of names and majors
     * @throws IOException
     */
    private String getObstacles(String range) throws IOException {
        List<String> results = new ArrayList<String>();
        ValueRange response = this.mService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();


        List<Obstacle> obstacleList = new ArrayList<>();
        List<List<Object>> values = response.getValues();

        // Now we have an array packed with Strings from the spreadsheet
        // column 0: NAME [STRING]
        // column 1: WHAT [STRING]
        // Column 2: Location [String, format "55 13 44.18N 009 12 50.61E"]
        // Column 3: ELEVATION INT
        // Column 4: HEIGHT    INT

        String link = "";

        if (values != null) {
            for (List row : values) {

                String name = row.get(0).toString();
                String what = row.get(1).toString();
                String location = row.get(2).toString();
                int elevation = Integer.parseInt(row.get(3).toString());
                int height = Integer.parseInt(row.get(4).toString());

                Obstacle obstacle = new Obstacle(name, what, location, elevation, height);

                Log.d(TAG, "getData: Obstacle: " + obstacle.getName() + " height: " + obstacle.getElevation() + " @" + obstacle.getPosition());
                obstacleList.add(obstacle);

            }

            // update database
            DbAdmin dbAdmin = new DbAdmin(mActivity);
            dbAdmin.updateObstaclesFromMaster(obstacleList, true);

        }
        return "Obstacles updated\n";
    }


    /**
     * Fetch a list of OpenAir Area definitions from a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return Status message
     * @throws IOException
     */
    private String getOpenAirAreas(String range) throws IOException {
        List<String> results = new ArrayList<String>();
        ValueRange response = this.mService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();


        OpenAirParser  openAirParser = new OpenAirParser();

        List<List<Object>> values = response.getValues();
        // Now we have an array packed with Strings from the spreadsheet
        // column 0: OpenAir Command [STRING]
        // column 1: Additional Comments [STRING] (optional)


        // Convert input objects to a list of OpenAir String commands
        List<String> openAircommands=new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            List row = values.get(i);
            String tempCommand;
            if (row.size()==0 || row.get(0).toString().charAt(0)=='*') {
                tempCommand="DONE";
            } else {
                tempCommand = row.get(0).toString();
            }
            openAircommands.add(tempCommand);
        }

        List<AreaItem> areaList = openAirParser.parseInitialCommands(openAircommands);

        // inserting these into the database with a cleanup operation
        DbAdmin dbAdmin = new DbAdmin(mActivity);
        dbAdmin.updateAreasFromMaster(areaList, true);

        return "OpenAir Areas parsed\n";
    }

}
