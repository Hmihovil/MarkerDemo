package com.example.tbrams.markerdemo;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tbrams.markerdemo.components.Util;
import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Obstacle;
import com.example.tbrams.markerdemo.data.ReportingPoint;
import com.example.tbrams.markerdemo.db.DbAdmin;
import com.example.tbrams.markerdemo.dbModel.AreaItem;
import com.example.tbrams.markerdemo.dbModel.CoordItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class GoogleSheetActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final String TAG = "TBR:";

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};

    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);


        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText("");

        //mOutputText.setText(
        //        "Click the \'" + BUTTON_TEXT + "\' button to test the API.");
        activityLayout.addView(mOutputText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");

        setContentView(activityLayout);


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


        getResultsFromApi();
    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    public boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                GoogleSheetActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getAllData();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }


        private List<String> getAllData() throws IOException {
            List<String> result = new ArrayList<>();


            result.add(getDataFromApi());
            result.add(getPublicAerodromes());
            result.add(getPrivateAerodromes());
            result.add(getRecreationalAerodromes());
            result.add(getReportingPoints());
            result.add(getObstacles());
            result.add(getAreas());
            return result;
        }

        /**
         * Fetch a list of Navaid names and locations from a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private String getDataFromApi() throws IOException {
            String spreadsheetId = "1G3rMDgZqItvOUVfxZOaIpTTg1YnR4UfxKvX9wEeZUkc";
            String range = "NavAids!A2:H";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
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
                        Log.d(TAG, "getDataFromApi: Unrecognized NavAid Type: " + row.get(2));
                    }

                    Log.d(TAG, "getDataFromApi: nType: " + nType);


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
                    Log.d(TAG, "getDataFromApi: New Navaid name: " + na.getName() + " @" + na.getPosition());
                    navAidsList.add(na);

                }

                // update database
                // This is a clean up operation, meaning the Nav Aids table will be wiped before
                // inserting these data again.
                DbAdmin dbAdmin = new DbAdmin(GoogleSheetActivity.this);
                dbAdmin.updateNavAidsFromMaster(navAidsList, true);

            }
            return "NavAids updated";
        }


        /**
         * Fetch a list of Area definitions from a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return Status message
         * @throws IOException
         */
        private String getAreas() throws IOException {
            String spreadsheetId = "1G3rMDgZqItvOUVfxZOaIpTTg1YnR4UfxKvX9wEeZUkc";
            String range = "TMA!A2:H";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();


            List<List<Object>> values = response.getValues();
            // Now we have an array packed with Strings from the spreadsheet
            // column 0: AREA NAME  [STRING]
            // column 1: AREA TYPE [STRING] (CTR or TMA for now)
            // Column 2: IDENT [STRING] [OPT] for example RK TMA "E"
            // column 3: LOCATION [STRING], format "55 13 44N 009 12 50E" or "55:13:44 N 009:12:50 E"]
            // Column 4: SEQUENCE [INT] Sequence# in the polygon for sorting
            // Column 5: FROM [INT] [OPT] ("GND" or xx or "FLxx", where xx is a number)
            // Column 6: TO [INT]   (xx or FLXX, where xx is a number)
            // Column 7: CLASS      ("C", "D", "E", "G", "G/E")


            List<AreaItem> areaList = new ArrayList<>();

            int nType = 0;

            if (values != null) {

                String name = "";
                String nextName = "";
                String nextIdent = "";
                String nextCategory = "";
                String nextClass = "";
                String nextFrom = "";
                String nextTo = "";

                int category = 0;
                int from = 0;
                int to = 0;
                AreaItem areaItem = null;

                boolean create_in_progress = false;

                List<LatLng> nextCoordList = new ArrayList<>();

                for (int i = 0; i < values.size(); i++) {
                    List row = values.get(i);
                    String tempName = row.get(0).toString();

                    if (!tempName.equals("")) {

                        if (create_in_progress) {
                            // create object

                            areaItem = constructAreaItem(nextName, nextCategory, nextIdent, nextClass, nextFrom, nextTo, nextCoordList);
                            areaList.add(areaItem);

                            Log.d(TAG, "Created new area: " + areaItem.getAreaName() + " " + areaItem.getAreaIdent());

                            create_in_progress = false;
                        }

                        // Prepare new object
                        create_in_progress = true;

                        nextName = tempName;
                        nextIdent = row.get(2).toString();
                        nextCategory = row.get(1).toString().toUpperCase();
                        nextClass = row.get(7).toString();
                        nextFrom = row.get(5).toString();
                        nextTo = row.get(6).toString();

                        nextCoordList = new ArrayList<>();

                    }


                    // New coordinate - we do not have a name in column 0
                    // need to create a new coordinate and add it to the vList
                    LatLng pos = Util.convertVFG(row.get(3).toString());
                    nextCoordList.add(pos);

                }

                // This is for the last AreaItem, still need to be constructed and added to the list
                areaItem = constructAreaItem(nextName, nextCategory, nextIdent, nextClass, nextFrom, nextTo, nextCoordList);
                areaList.add(areaItem);

                Log.d(TAG, "Created last new area: " + areaItem.getAreaName() + " " + areaItem.getAreaIdent());


                // update database
                // This is a clean up operation, meaning the Nav Aids table will be wiped before
                // inserting these data again.
                DbAdmin dbAdmin = new DbAdmin(GoogleSheetActivity.this);
                dbAdmin.updateAreasFromMaster(areaList, true);

            }

            return "Areas parsed";
        }


        /**
         * Fetch a list of PrivateAerodromes names and locations from a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private String getPrivateAerodromes() throws IOException {
            String spreadsheetId = "1G3rMDgZqItvOUVfxZOaIpTTg1YnR4UfxKvX9wEeZUkc";
            String range = "PrivateAerodromes!A2:I";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
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
                DbAdmin dbAdmin = new DbAdmin(GoogleSheetActivity.this);
                dbAdmin.updateAerodromesFromMaster(adList, false);

            }
            return "Private Aerodromes updated";
        }


        /**
         * Fetch a list of Recreational Aerodromes names and locations from a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private String getRecreationalAerodromes() throws IOException {
            String spreadsheetId = "1G3rMDgZqItvOUVfxZOaIpTTg1YnR4UfxKvX9wEeZUkc";
            String range = "Recreational!A2:E";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
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
                DbAdmin dbAdmin = new DbAdmin(GoogleSheetActivity.this);
                dbAdmin.updateAerodromesFromMaster(adList, false);

            }
            return "Recreational Aerodromes updated";
        }


        /**
         * Fetch a list of Public Aerodromes names and locations from a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private String getPublicAerodromes() throws IOException {
            String spreadsheetId = "1G3rMDgZqItvOUVfxZOaIpTTg1YnR4UfxKvX9wEeZUkc";
            String range = "PublicAerodromes!A2:J";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
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
                DbAdmin dbAdmin = new DbAdmin(GoogleSheetActivity.this);
                dbAdmin.updateAerodromesFromMaster(adList, true);

            }
            return "Public Aerodromes updated";
        }


        /**
         * Fetch a list of Aerodrome Reporting Points from a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private String getReportingPoints() throws IOException {
            String spreadsheetId = "1G3rMDgZqItvOUVfxZOaIpTTg1YnR4UfxKvX9wEeZUkc";
            String range = "ReportingPoints!A2:C";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
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
                DbAdmin dbAdmin = new DbAdmin(GoogleSheetActivity.this);
                dbAdmin.updateReportingPointsFromMaster(rpList, true);

            }
            return "Reporting Points updated";
        }


        /**
         * Fetch a list of Obstacles from a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private String getObstacles() throws IOException {
            String spreadsheetId = "1G3rMDgZqItvOUVfxZOaIpTTg1YnR4UfxKvX9wEeZUkc";
            String range = "Obstacles!A2:E";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
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
                DbAdmin dbAdmin = new DbAdmin(GoogleSheetActivity.this);
                dbAdmin.updateObstaclesFromMaster(obstacleList, true);

            }
            return "Obstacles updated";
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }


        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }


        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GoogleSheetActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

    private AreaItem constructAreaItem(String nextName, String nextCategory, String nextIdent, String nextClass, String nextFrom, String nextTo, List<LatLng> nextCoordList) {

        int category = getAirspaceCategory(nextCategory);
        int from = Util.parseAltitude(nextFrom);
        int to = Util.parseAltitude(nextTo);

        AreaItem areaItem = new AreaItem(null, nextName, category, nextIdent, nextClass, from, to);

        // Build a list of Coordinate Item Object from the LatLng list we already have
        List<CoordItem> coordinateList = new ArrayList<>();
        for (int j = 0; j < nextCoordList.size(); j++) {
            coordinateList.add(new CoordItem(null, areaItem.getAreaId(), nextCoordList.get(j), j));
        }
        // Add them to the AreaItem object and then append the new object to the list
        areaItem.setCoordItemList(coordinateList);

        return areaItem;
    }



    private int getAirspaceCategory(String nextCategory) {
        int category = 0;
        if (nextCategory.equals("CTR")) {
            category = AreaItem.CTR;
        } else if (nextCategory.equals("TMA")) {
            category = AreaItem.TMA;
        } else if (nextCategory.equals("TIA")) {
            category = AreaItem.TIA;
        } else if (nextCategory.equals("TIZ")) {
            category = AreaItem.TIZ;
        } else if (nextCategory.equals("LTA")) {
            category = AreaItem.LTA;
        }
        return category;
    }
}

