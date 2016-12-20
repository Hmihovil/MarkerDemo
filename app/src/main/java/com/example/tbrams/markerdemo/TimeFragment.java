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

import java.util.ArrayList;
import java.util.List;


public class TimeFragment extends Fragment implements View.OnClickListener {

    public static final String EXTRA_SEGMENT_ID = "com.example.tbrams.markerdemo.segment_id";

    private static final int C_TIME=0;
    private static final int C_TURN=1;
    private static final int C_TRACK=2;
    private static final int C_TALK=3;
    private static final int C_CHECK=4;
    private static final int C_ARRIVED=5;

    private int segmentIndex = 0;
    private View v;
    private TextView tvNextWP;
    private TextView tvWPnumber;
    private TextView tvWPtotal;
    private TextView tvHeading;
    private TextView tvDistance;
    private TextView tvRETO;
    private TextView tvDiff;
    private TextView tvCommand;
    private Time mTime;

    private Button timeBtn, talkBtn, nextBtn;

    private MarkerObject fromWP;
    private MarkerObject toWP;
    private static int mCommand;
    private List<String> mHints = new ArrayList();


    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mTime=new Time();
        mCommand = C_TIME;

        v = inflater.inflate(R.layout.time_fragment_layout, container, false);
        tvNextWP = (TextView) v.findViewById(R.id.textViewNextWP);
        tvWPnumber = (TextView) v.findViewById(R.id.textViewWPnumber);
        tvWPtotal = (TextView) v.findViewById(R.id.textViewWPtotal);
        tvHeading = (TextView) v.findViewById(R.id.textViewMH);
        tvDistance = (TextView) v.findViewById(R.id.textViewDist);
        tvRETO = (TextView) v.findViewById(R.id.textViewRETO);
        tvDiff = (TextView) v.findViewById(R.id.textViewDiff);
        tvCommand = (TextView) v.findViewById(R.id.textViewHints);

        Button checkBtn = (Button) v.findViewById(R.id.buttonCheck);
        checkBtn.setOnClickListener(this);

        mHints.add("TIME");
        mHints.add("TURN");
        mHints.add("TRACK");
        mHints.add("TALK");
        mHints.add("CHECK");
        mHints.add("Arrived at destination");

        updateFields();


        return v;

    }

    private void updateAdvice() {
        mCommand =(mCommand +1)%mHints.size();
        tvCommand.setText(mHints.get(mCommand));
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
        tvHeading.setText(String.format("%.0f ˚", toWP.getTT()));
        tvDistance.setText(String.format("%.1f nm", toWP.getDist()));

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

        // Command
        tvCommand.setText(mHints.get(mCommand));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TBR:","onResume...");

    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.buttonCheck) {

            // Process Click action depending on what state we are in
            switch (mCommand) {
                case C_TIME:
                    Log.d("TBR:", "C_TIME command");

                    // Get zulu time in minutes
                    String minutes = mTime.getZuluTime().split(":")[1];
                    Log.d("TBR:", "Zulu time in minutes is: "+minutes);

                    double time = Double.parseDouble(minutes);
                    toWP.setATO(time);    // set ATO to the "actual" time
                    Log.d("TBR:", "ATO set to "+minutes+" for "+toWP.getText());

                    // Calculate and time difference
                    double timeDifference = toWP.getETO()-toWP.getATO();
                    Log.d("TBR:", "Diff from estimate is: "+timeDifference+" for "+toWP.getText());


                    if (segmentIndex<markerList.size()-2) {
                        // If we have a WP following toWP, then we need to update
                        MarkerObject thenWP = markerList.get(segmentIndex+2);
                        double reto = thenWP.getETO() - timeDifference;
                        thenWP.setRETO(reto);
                        Log.d("TBR:", "RETO set to: " + reto + " for "+thenWP.getText());

                        // Debug only
                        Log.d("TBR:", "Dumping ETO/RETO for all markers here");
                        for (int i=1;i<markerList.size();i++){
                            Log.d("TBR:", markerList.get(i).getText() + " ETO: "+markerList.get(i).getETO()+" RETO: "+markerList.get(i).getRETO());
                        }

                        // We have arrived at toWP, next action is to change heading to the following point
                        tvCommand.setText("TURN: Heading "+thenWP.getMH()+"˚");
                        mCommand =C_TURN;

                    } else {
                        mCommand =C_ARRIVED;
                    }
                    break;

                case C_TURN:
                    Log.d("TBR:", "C_TURN command");

                    if (segmentIndex<markerList.size()-2) {
                        segmentIndex++;
                        updateFields();
                    }

                    tvCommand.setText("TRACK");
                    mCommand =C_TRACK;

                    break;

                case C_TALK:
                    Log.d("TBR:", "C_TALK command");

                    Intent intent = TalkActivity.newIntent(getActivity(), segmentIndex);
                    startActivity(intent);

                    tvCommand.setText("CHECK");
                    mCommand =C_CHECK;

                    break;


                case C_TRACK:
                    Log.d("TBR:", "C_TRACK command");

                    tvCommand.setText("TALK");
                    mCommand =C_TALK;

                    break;

                case C_CHECK:
                    Log.d("TBR:", "C_CHECK command");

                    tvCommand.setText("TIME");
                    mCommand =C_TIME;

                    break;

                case C_ARRIVED:
                    Log.d("TBR:", "C_ARRIVED command");
                    break;

                default:
                    Log.d("TBR:", "Something went wrong in switch statement");
                    mCommand =C_ARRIVED;
                    break;
            }

        }


        /* OLD MODEL HEREAFTER ...
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

            if (segmentIndex<markerList.size()-1) {
                nextBtn.setEnabled(true);
            }

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
        */
    }
}
