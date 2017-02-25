package com.example.tbrams.markerdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.ExtraMarkers;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Obstacle;
import com.example.tbrams.markerdemo.data.ReportingPoint;
import com.example.tbrams.markerdemo.db.DataSourceArea;
import com.example.tbrams.markerdemo.db.DbAdmin;
import com.example.tbrams.markerdemo.dbModel.AreaItem;

import java.util.ArrayList;
import java.util.List;


public class FrontActivity extends AppCompatActivity {

    public static final String TAG="TBR:FA";
    private static final int SETTINGS_RESULT = 3;


    // urls to load navigation header background image
    // and profile image
    private static final String urlNavHeaderBg = "http://api.androidhive.info/images/nav-menu-header-bg.jpg";
    private static final String urlProfileImg = "https://lh3.googleusercontent.com/OourXDyOSAS9q65-InyB3qrAVt1EIO8uA80ta6jBB3IgK8MfdqgkAAA3AvpegO_1-pftvls6s0j4zTjyvSECRVr9PnESJT_ni3QkIxsIr7bG0-IfLmrUblfD0YHp0TFb-4zK_w=w1331-h999-no";

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName;
    private Toolbar toolbar;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_SETTINGS = "settings";
    public static String CURRENT_TAG = TAG_HOME;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler myHandler;

    // DB Related
    DbAdmin mDbAdmin;
    DataSourceArea mDataSourceArea;

    private ExtraMarkers sExtraMarkers = ExtraMarkers.get(this);
    private List<NavAid> mNavAidList = sExtraMarkers.getNavAidList();
    private List<Aerodrome> mAerodromeList = sExtraMarkers.getAerodromeList();
    private List<ReportingPoint> mReportingPointList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d(TAG, "onCreate: Before prepare DB content, mNavAidList.size(): "+ mNavAidList.size());
        Log.d(TAG, "onCreate: Before prepare DB content, AdList.size(): "+ mAerodromeList.size());

        // Get all the details in sync with the DB
        prepareDbContent();

        mNavAidList = sExtraMarkers.getNavAidList();
        mAerodromeList = sExtraMarkers.getAerodromeList();
        mReportingPointList = sExtraMarkers.getReportingPointList();

        Log.d(TAG, "onCreate: After prepare DB content, mNavAidList.size(): "+ mNavAidList.size());
        Log.d(TAG, "onCreate: After prepare DB content, AdList.size(): "+ mAerodromeList.size());


        myHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }
    }





    private void prepareDbContent() {
        // Get a handle to the Database Admin helper
        mDbAdmin = new DbAdmin(this);
        mDataSourceArea = new DataSourceArea(this);

        // Just in case it is a new installation, make sure we have the empty tables at least
        mDbAdmin.makeSureWeHaveTables();

        Log.d(TAG, "prepareDbContent: after mDbAdmin make sure....");
        // check if we can load Nav Aids list from database or we need to load from samples
        List<NavAid> navList = mDbAdmin.getAllNavAids(null);
        Log.d(TAG, "onCreate: from DB - navList.size(): "+navList.size());
        if (navList==null||navList.size()==0) {
            // use the build in samples as backup
            sExtraMarkers.setNavAidList(sExtraMarkers.getSampleNavAidList());
            Log.d(TAG, "onCreate: Could not get navaid data from db, using samples");
        } else {
            // load this list from the database
            sExtraMarkers.setNavAidList(navList);
        }


        // Check if we can load Aerodromes from the database or if we need to load from samples
        List<Aerodrome> adList = mDbAdmin.getAllAerodromes(null);
        Log.d(TAG, "onCreate: from DB - adList.size(): "+adList.size());
        if (adList==null||adList.size()==0) {
            // use the build in samples as backup
            sExtraMarkers.setAerodromeList(sExtraMarkers.getSampleAerodromeList());
            Log.d(TAG, "onCreate: Could not get AD data from db, using samples");
        } else {
            // load this list from the database
            sExtraMarkers.setAerodromeList(adList);
        }


        boolean userNotified=false;
        // Check if we can load Reporting Points from the database. We do not have samples for this
        List<ReportingPoint> rpList = mDbAdmin.getAllReportingPoints(null);
        Log.d(TAG, "onCreate: from DB - rpList.size(): "+rpList.size());
        if (rpList==null||rpList.size()==0) {
            Toast.makeText(this, "No reporting points - Please update from server", Toast.LENGTH_SHORT).show();
            userNotified=true;
        } else {
            // load this list from the database
            sExtraMarkers.setReportingPointList(rpList);
        }


        // Check if we can load Obstacles from the database. We do not have samples for this
        List<Obstacle> oList = mDbAdmin.getAllObstacles(null);
        Log.d(TAG, "onCreate: from DB - oList.size(): "+oList.size());
        if (oList==null||oList.size()==0) {
            if (!userNotified) {
                Toast.makeText(this, "No Obstacles - Please update from server", Toast.LENGTH_SHORT).show();
                userNotified=true;
            }
        } else {
            // load this list from the database
            sExtraMarkers.setObstaclesList(oList);
        }


        // Check if we can load Areas and Coordinates from the database.
        List<AreaItem> areaList = mDataSourceArea.getAllAreas(null);
        Log.d(TAG, "onCreate: Areas from DB - areaList.size(): "+areaList.size());
        if (areaList==null||areaList.size()==0) {
            Log.d(TAG, "No area data in database");
        } else {
            // use the area list from the database
            sExtraMarkers.setAreaItemList(areaList);
        }
    }





    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            myHandler.post(mPendingRunnable);
        }


        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        HomeFragment homeFragment;
        switch (navItemIndex) {
            case 0:
                // home
                homeFragment = new HomeFragment();
                return homeFragment;

//            case 4:
//                // Launch the PreferenceActivity
//                Intent preferenceIntent = new Intent(this, MarkerPreferenceActivity.class);
//                startActivityForResult(preferenceIntent, SETTINGS_RESULT);
//
//                homeFragment = new HomeFragment();
//                return homeFragment;

            default:
                return new HomeFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }



    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }



    /***
     * Load navigation menu header information
     * like background image, profile image and name
     */
    private void loadNavHeader() {
        // name, website
        txtName.setText("Torben Brams");

        // loading header background image
        Glide.with(this).load(urlNavHeaderBg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);

        // Loading profile image
        Glide.with(this).load(urlProfileImg)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);

    }


    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;

                    case R.id.nav_settings:
                        // launch new preference intent instead of loading fragment
                        Intent preferenceIntent = new Intent(FrontActivity.this, MarkerPreferenceActivity.class);
                        startActivityForResult(preferenceIntent, SETTINGS_RESULT);
                        return true;

                    case R.id.nav_about:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(FrontActivity.this, AboutActivity.class));
                        drawer.closeDrawers();
                        return true;

                    case R.id.db_export:
                        Log.d(TAG, "onNavigationItemSelected: Database Export");

                        mDbAdmin.exportJSON();

                        break;


                    case R.id.db_import:
                        Log.d(TAG, "onNavigationItemSelected: Database import");

                        // Flush the tables and import JSON files
                        mDbAdmin.importJSON();

                        break;

                    case R.id.db_reset:
                        Log.d(TAG, "onNavigationItemSelected: Database Reset...");

                        // Clean DB and load test data
                        mDbAdmin.populateDatabase();
                        Toast.makeText(FrontActivity.this, "Database reset and populated with sample data", Toast.LENGTH_SHORT).show();

                        break;

                    case R.id.db_navaids:
                        Log.d(TAG, "onNavigationItemSelected: Update from server");

                        // Launch GoogleSheetsActivity
                        Intent sheetsIntent = new Intent(FrontActivity.this, GoogleSheetActivity.class);
                        startActivity(sheetsIntent);

                        break;


                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes. We don't want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open. We don't want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }



    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*

        // show menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        // when fragment is settings, load the menu created for notifications
        if (navItemIndex == 4) {
            getMenuInflater().inflate(R.menu.db_menu, menu);
        }
        */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.db_export) {
            Toast.makeText(getApplicationContext(), "Export action!", Toast.LENGTH_LONG).show();
            return true;
        }

        if (id == R.id.db_import) {
            Toast.makeText(getApplicationContext(), "Action Import!", Toast.LENGTH_LONG).show();
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();


        // Make sure we have a network connection
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setTitle("No network")
                    .setMessage("This application needs a network connection")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // Terminate the activity
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This is where we will receive a call, when the for example the preferences has been done

        if (requestCode == SETTINGS_RESULT) {
            // A preference has been changed

            Toast.makeText(this, "Preferences done...", Toast.LENGTH_SHORT).show();
        }
    }


}
