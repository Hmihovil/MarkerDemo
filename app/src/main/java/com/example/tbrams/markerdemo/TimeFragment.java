package com.example.tbrams.markerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.NavAids;

import java.util.List;


public class TimeFragment extends Fragment {

    public static final String EXTRA_MARKER_ID = "com.example.tbrams.markerdemo.marker_id";

    private int markerIndex = -1;

    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();
    NavAids navaids = NavAids.get(getActivity());
    List<NavAid> vorList = navaids.getList();

    public static TimeFragment newInstance(int markerIndex) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_MARKER_ID, markerIndex);

        TimeFragment fragment = new TimeFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.time_fragment_layout, container, false);
        final TextView tvNextWP = (TextView) v.findViewById(R.id.textViewNextWP);
        final TextView tvWPnumber = (TextView) v.findViewById(R.id.textViewWPnumber);
        final TextView tvWPtotal = (TextView) v.findViewById(R.id.textViewWPtotal);
        final TextView tvHeading = (TextView) v.findViewById(R.id.textViewMH);
        final TextView tvDistance = (TextView) v.findViewById(R.id.textViewDist);
        final TextView tvRETO = (TextView) v.findViewById(R.id.textViewRETO);
        final TextView tvDiff = (TextView) v.findViewById(R.id.textViewDiff);
        final TextView tvHints = (TextView) v.findViewById(R.id.textViewHints);

        // Get marker id from argument bundle
        markerIndex = (int) getArguments().getSerializable(EXTRA_MARKER_ID);
        Log.d("TBR:","TimeFragment, OnCreatView, markerIndex: "+markerIndex);

        // Next Way Point
        if (markerIndex+1==markerList.size()){
            // end of the segments
            tvNextWP.setText("");
        } else {
            // we have a next WP
            tvNextWP.setText(markerList.get(markerIndex+1).getText());
        }


        // Way Point Number and Total
        tvWPnumber.setText( Integer.toString(markerIndex+1));      // Because most humans start counting at 1
        tvWPtotal.setText( Integer.toString(markerList.size()-1)); // counting legs, not WPs

        final MarkerObject mo=markerList.get(markerIndex);

        // Heading and Distance
        tvHeading.setText(String.format("%.0f", mo.getTT()));
        tvDistance.setText(String.format("%.1f", mo.getDist()));

        // RETO
        // TODO: Need some time formatting here later
        if (markerIndex==0) {
            tvRETO.setText("NA");
        } else {
            Log.d("TBR:", "mo.getRETO(): "+mo.getRETO());
            tvRETO.setText(Double.toString(mo.getRETO()));
        }

        // Time difference
        tvDiff.setText(Double.toString(mo.getDiff()));

        // Hints field
        // TODO: Need to make these hints context relevant and cycle through messages, timer?
        tvHints.setText("Prepare for Take off");

        // TIME Button
        final Button btnTime = (Button) v.findViewById(R.id.buttonTime);
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TBR:", "Time clicked");

                // for testing I will hardcode time stamp to be like:
                int time = 8;
                mo.setATO(time);
                Log.d("TBR:", "ATO set to "+time);


                if (markerIndex+1!=markerList.size()){
                    // we have a next WP, calculate and register the RETO
                    double difference = mo.getETO()-mo.getATO();
                    Log.d("TBR:", "Diff is: "+difference);

                    MarkerObject nextMO = new MarkerObject();
                    nextMO=markerList.get(markerIndex+1);
                    double reto = nextMO.getETO() - difference;
                    nextMO.setRETO(reto);
                    Log.d("TBR:", "RETO for next point is: "+reto);
                }

            }
        });


        // TALK Button
        final Button btnTalk = (Button) v.findViewById(R.id.buttonTalk);
        btnTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TBR:", "Starting TalkActivity");

                Intent intent = TalkActivity.newIntent(getActivity(), markerIndex);
                startActivity(intent);

            }
        });

        return v;

    }


}
