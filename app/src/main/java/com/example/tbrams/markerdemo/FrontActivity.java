package com.example.tbrams.markerdemo;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.db.DataSource;
import com.example.tbrams.markerdemo.dbModel.TripItem;

import static com.example.tbrams.markerdemo.TripAdapter.TRIP_KEY;

public class FrontActivity extends AppCompatActivity {

    public static final String TAG="TBR:FA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.front_page);

        final EditText editName = (EditText) findViewById(R.id.frontTripName);
        final TextView txtInstr = (TextView) findViewById(R.id.frontTextInstruction);
        final Button createBtn = (Button) findViewById(R.id.frontCreateBtn);
        final Button browseBtn = (Button) findViewById(R.id.frontBrowseBtn);


        String name = editName.getText().toString();

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Create");

                String name =String.valueOf(editName.getText());
                if (!name.matches("^[0-9A-Za-z\\s]{1,}[\\.\\,\\-\\_]{0,1}[A-Za-z\\s\\?\\-\\_0-9]{0,}$")) {
                    editName.setError("You need to provide a proper description");
                    return;
                }

                // Name is OK, use it for constructor and keep it handing in markerlab
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

        browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Browse existing trips...");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }

}
