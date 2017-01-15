package com.example.tbrams.markerdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.db.DataSource;
import com.example.tbrams.markerdemo.dbModel.TripItem;

import static com.example.tbrams.markerdemo.TripAdapter.TRIP_KEY;

public class FrontActivity extends AppCompatActivity {

    public static final String TAG="TBR:FA";
    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context=(Context) this;


        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.front_page);

        final EditText editName = (EditText) findViewById(R.id.frontTripName);
        final TextView txtInstr = (TextView) findViewById(R.id.frontTextInstruction);
        final Button createBtn = (Button) findViewById(R.id.frontCreateBtn);
        final Button browseBtn = (Button) findViewById(R.id.frontBrowseBtn);

        final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.mycontent);


        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Create");


//                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
//                builder.setTitle("AppCompatDialog");
//                builder.setMessage("Lorem ipsum dolor...");
//                builder.setPositiveButton("OK", null);
//                builder.setNegativeButton("Cancel", null);
//                builder.show();
//

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                builder.setTitle("Title");

                View viewInflated = LayoutInflater.from(context).inflate(R.layout.enter_name, viewGroup, false);

                // Set up the input
                final AutoCompleteTextView input = (AutoCompleteTextView) viewInflated.findViewById(R.id.input_trip_name);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();

                        // Any name is OK, use it for constructor and keep it handing in markerlab
                        TripItem trip = new TripItem(null,name,null, null);
                        DataSource datasource = new DataSource(getApplicationContext());
                        datasource.open();
                        trip = datasource.createTrip(trip);
                        datasource.close();

                        Log.d(TAG,"Trip ID after insert: "+ trip.getTripId());

                        // Save tripname in markerLab singleton storage
                        MarkerLab markerLab = MarkerLab.getMarkerLab(getApplicationContext());
                        markerLab.setTripName(trip.getTripName());

                        // Start MarkerDemoActivity with tripID extra argument
                        Intent intent = new Intent(getApplicationContext(), MarkerDemoActivity.class);
                        intent.putExtra(TRIP_KEY, trip.getTripId());
                        Log.d(TAG,"FrontActivity -> MarkerDemoActivity with TripId: "+trip.getTripId());
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                builder.show();

        }
});








//        createBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "Create");
//
//                String name = editName.getText().toString();
//
//                // Any name is OK, use it for constructor and keep it handing in markerlab
//                TripItem trip = new TripItem(null,name,null, null);
//                DataSource datasource = new DataSource(getApplicationContext());
//                datasource.open();
//                trip = datasource.createTrip(trip);
//                datasource.close();
//
//                Log.d(TAG,"Trip ID after insert: "+ trip.getTripId());
//
//                // Save tripname in markerLab singleton storage
//                MarkerLab markerLab = MarkerLab.getMarkerLab(getApplicationContext());
//                markerLab.setTripName(trip.getTripName());
//
//                // Start MarkerDemoActivity with tripID extra argument
//                Intent intent = new Intent(getApplicationContext(), MarkerDemoActivity.class);
//                intent.putExtra(TRIP_KEY, trip.getTripId());
//                Log.d(TAG,"FrontActivity -> MarkerDemoActivity with TripId: "+trip.getTripId());
//                startActivity(intent);
//
//            }
//        });

        browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Browse existing trips...");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            new AlertDialog.Builder(this)
                    .setTitle("No network")
                    .setMessage("This application needs a network connection")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with finish()
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

    }
}
