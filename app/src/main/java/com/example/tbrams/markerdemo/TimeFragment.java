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


public class TimeFragment extends Fragment implements View.OnClickListener {

    public static final String EXTRA_SEGMENT_ID = "com.example.tbrams.markerdemo.segment_id";

    private int segmentIndex = 0;
    private View v;
    private TextView tvNextWP;
    private TextView tvWPnumber;
    private TextView tvWPtotal;
    private TextView tvHeading;
    private TextView tvDistance;
    private TextView tvRETO;
    private TextView tvDiff;
    private TextView tvHints;

    private Button timeBtn, talkBtn, nextBtn;

    private MarkerObject fromWP;
    private MarkerObject toWP;


    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.time_fragment_layout, container, false);
        tvNextWP = (TextView) v.findViewById(R.id.textViewNextWP);
        tvWPnumber = (TextView) v.findViewById(R.id.textViewWPnumber);
        tvWPtotal = (TextView) v.findViewById(R.id.textViewWPtotal);
        tvHeading = (TextView) v.findViewById(R.id.textViewMH);
        tvDistance = (TextView) v.findViewById(R.id.textViewDist);
        tvRETO = (TextView) v.findViewById(R.id.textViewRETO);
        tvDiff = (TextView) v.findViewById(R.id.textViewDiff);
        tvHints = (TextView) v.findViewById(R.id.textViewHints);

        timeBtn = (Button) v.findViewById(R.id.buttonTime);
        talkBtn = (Button) v.findViewById(R.id.buttonTalk);
        nextBtn = (Button) v.findViewById(R.id.buttonNext);

        timeBtn.setOnClickListener(this);
        talkBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        updateFields();

        return v;

    }

    private void updateFields() {

        // TODO: Can we assume we have at least Two markers?
        fromWP = markerList.get(segmentIndex);
        toWP   = markerList.get(segmentIndex+1);

        // Next WP Name
        tvNextWP.setText(toWP.getText());

        // Segment Number and Total
        tvWPnumber.setText( Integer.toString(segmentIndex+1));   // The first segment is "1" here
        tvWPtotal.setText( Integer.toString(markerList.size()-1));

        // Heading and Distance
        tvHeading.setText(String.format("%.0f", toWP.getTT()));
        tvDistance.setText(String.format("%.1f", toWP.getDist()));

        // RETO
        // TODO: Need some time formatting here later
        if (segmentIndex==0) {
            // Special for first Segment
            // We only have ETO for first destination (still no ATO and thus no RETO)

            tvRETO.setText(String.format("%.0f", toWP.getETO()));
        } else {

            tvRETO.setText(String.format("%.0f", toWP.getRETO()));
            Log.d("TBR:", "toWP.getRETO(): "+toWP.getRETO());
        }

        // Time difference
        tvDiff.setText(String.format("%.0f", toWP.getDiff()));

        // Hints field
        // TODO: Need to make these hints context relevant and cycle through messages, timer?
        tvHints.setText("Prepare for Take off");
    }



    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.buttonNext) {

            // NEXT Button

            if (segmentIndex<markerList.size()-1) {
                segmentIndex++;
                updateFields();

                talkBtn.setEnabled(false);
                nextBtn.setEnabled(false);

            }

        } else if (view.getId()==R.id.buttonTalk ) {

            // TALK Button

            Intent intent = TalkActivity.newIntent(getActivity(), segmentIndex);
            startActivity(intent);

            nextBtn.setEnabled(true);

        } else if (view.getId()==R.id.buttonTime) {

            // TIME Button

            // for testing I will hardcode time stamp to be like:
            int time = 8;

            toWP.setATO(time);    // set ATO to the "actual" time
            Log.d("TBR:", "ATO set to "+time+" for "+toWP.getText());

            // Calculate and time difference
            double timeDifference = toWP.getETO()-toWP.getATO();
            Log.d("TBR:", "Diff from estimate is: "+timeDifference+" for "+toWP.getText());

            if (segmentIndex<markerList.size()-2) {
                // We have a next segment
                MarkerObject thenWP = markerList.get(segmentIndex+2);
                double reto = thenWP.getETO() - timeDifference;
                thenWP.setRETO(reto);
                Log.d("TBR:", "RETO set to: " + reto + thenWP.getText());
            }
            Log.d("TBR:", "Dumping ETO/RETO for all markers here");
            for (int i=1;i<markerList.size();i++){
                Log.d("TBR:", markerList.get(i).getText() + " ETO: "+markerList.get(i).getETO()+" RETO: "+markerList.get(i).getRETO());
            }

            talkBtn.setEnabled(true);
        }
    }
}
