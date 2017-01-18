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
import java.util.Locale;


public class TimeFragment extends Fragment implements View.OnClickListener {

    private static final int C_TIME=0;
    private static final int C_TURN=1;
    private static final int C_TRACK=2;
    private static final int C_TALK=3;
    private static final int C_CHECK=4;
    private static final int C_ARRIVED=5;
    private static final int C_TAKEOFF=6;
    private static final int C_AFIS=7;
    private static final int C_LAND=8;
    private static final String TAG = "TBR:TimeFragment ";

    private int segmentIndex = 0;
    private View v;
    private TextView tvNextWP;
    private TextView tvWPnumber;
    private TextView tvWPtotal;
    private TextView tvHeading;
    private TextView tvDistance;
    private TextView tvRETO;
    private TextView tvDiff;
    private TextView tvRetoLbl;
    private TextView tvCommand;
    private Button checkBtn;
    private Time mTime;
    private Boolean noIncrement=true;

    private Button timeBtn, talkBtn, nextBtn;

    private MarkerObject fromWP;
    private MarkerObject toWP;
    private static int mCommand;
    private List<String> mCommandList = new ArrayList();


    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().setTitle(markerLab.getTripName());

        mTime=new Time();

        v = inflater.inflate(R.layout.time_fragment_layout, container, false);
        tvNextWP = (TextView) v.findViewById(R.id.textViewNextWP);
        tvWPnumber = (TextView) v.findViewById(R.id.textViewWPnumber);
        tvWPtotal = (TextView) v.findViewById(R.id.textViewWPtotal);
        tvHeading = (TextView) v.findViewById(R.id.textViewMH);
        tvDistance = (TextView) v.findViewById(R.id.textViewDist);
        tvRETO = (TextView) v.findViewById(R.id.textViewRETO);
        tvDiff = (TextView) v.findViewById(R.id.textViewDiff);
        tvCommand = (TextView) v.findViewById(R.id.textViewHints);
        tvRetoLbl = (TextView) v.findViewById(R.id.textViewRETOLabel);
        checkBtn = (Button) v.findViewById(R.id.buttonCheck);
        checkBtn.setOnClickListener(this);

        mCommandList.add("TIME");
        mCommandList.add("TURN");
        mCommandList.add("TRACK");
        mCommandList.add("TALK");
        mCommandList.add("CHECK");
        mCommandList.add("Arrived at destination");
        mCommandList.add("Ready when you are...");
        mCommandList.add("Listen to AFIS");
        mCommandList.add("Request Landing Clearance");


        updateFields();

        // First state
        newCommand(C_TAKEOFF);

        return v;

    }


    private boolean hasNextSegment() {
        return ((segmentIndex + 1) < (markerList.size() - 1));
    }




    private void updateFields() {

        fromWP = markerList.get(segmentIndex);
        toWP   = markerList.get(segmentIndex+1);

        // Segment Number and Total
        tvWPnumber.setText( String.format(Locale.ENGLISH, "%d", segmentIndex+1));
        tvWPtotal.setText( String.format(Locale.ENGLISH, "%d", markerList.size()-1));

        // Next WP Name
        tvNextWP.setText(toWP.getText());

        // Heading and Distance
        tvHeading.setText(String.format(Locale.ENGLISH, "%03d ˚", (int) toWP.getMH()));
        tvDistance.setText(String.format(Locale.ENGLISH, "%.1f nm", toWP.getDist()));

        // RETO
        if (segmentIndex==0) {
            // Special for first Segment
            // We only have ETO for first destination (No ATO, hence no RETO)
            // TODO: Need some time formatting here later

            tvRetoLbl.setText(R.string.ETO);
            tvRETO.setText(String.format(Locale.ENGLISH, "%03d", (int) toWP.getETO()));
        } else {

            tvRetoLbl.setText(R.string.RETO);
            tvRETO.setText(String.format(Locale.ENGLISH, "%03d", (int)toWP.getRETO()));
        }

        // Time difference
        tvDiff.setText(String.format(Locale.ENGLISH, "%02d", (int)fromWP.getDiff()));

    }



    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume...");

    }



    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.buttonCheck) {

            String zuluMinutes;
            String zuluTime;

            // Process Click action depending on what state we are in
            switch (mCommand) {
                case C_TIME:
                    Log.d(TAG, "C_TIME Clicked");

                    // Get zulu time in minutes and set ATO.
                    // Unlike the Pinto board situation, difference will be auto calculated here ^_^
                    zuluTime = mTime.getZuluTime();
                    zuluMinutes = zuluTime.split(":")[1];

                    toWP.setATO(Double.parseDouble(zuluMinutes));

                    Log.d(TAG, "WP "+toWP.getText()+" ATO set to: "+zuluMinutes);
                    Log.d(TAG, "WP "+toWP.getText()+" DIFF found: "+toWP.getDiff());

                    // Check if we have more legs to navigate, otherwise make arrival statement
                    if (segmentIndex<markerList.size()-2) {
                        // This is not the final leg
                        MarkerObject thenWP = markerList.get(segmentIndex+2);
                        double RETO = thenWP.getETO() - toWP.getDiff();
                        thenWP.setRETO(RETO);

                        // Debug
                        Log.d(TAG, thenWP.getText()+" RETO set to: " + RETO);
                        Log.d(TAG, "Dumping ETO/RETO for all markers here");
                        for (int i=1;i<markerList.size();i++){
                            Log.d(TAG, markerList.get(i).getText() + " ETO: "+markerList.get(i).getETO()+" RETO: "+markerList.get(i).getRETO());
                        }

                        // We have arrived at toWP and got the time already
                        // Next action is to change heading towards new point
                        newCommand(C_TURN);

                    } else {

                        newCommand(C_ARRIVED);
                    }
                    break;

                case C_TURN:
                    Log.d(TAG, "C_TURN Clicked");

                    newCommand(C_TRACK);
                    break;


                case C_TRACK:
                    Log.d(TAG, "C_TRACK Clicked");

                    newCommand(C_TALK);
                    break;


                case C_TALK:
                    Log.d(TAG, "C_TALK Clicked");

                    // Launch talk activity with info on this segment
                    Intent intent = TalkActivity.newIntent(getActivity(), segmentIndex);
                    startActivity(intent);

                    newCommand(C_CHECK);
                    break;


                case C_CHECK:
                    Log.d(TAG, "C_CHECK clicked");
                    newCommand(C_TIME);
                    break;


                case C_ARRIVED:
                    Log.d(TAG, "C_ARRIVED clicked");
                    break;

                case C_TAKEOFF:
                    // Got the cleared for Take Off, record the time and update all ETO times based
                    // on current time plus previous ETO estimate.

                    Log.d(TAG, "C_TAKEOFF clicked");

                    zuluTime = mTime.getZuluTime();
                    zuluMinutes = zuluTime.split(":")[1];

                    Log.d(TAG, "Zulu Time is: "+zuluTime);
                    Log.d(TAG, "Minutes is  : "+zuluMinutes);

                    // use it to generate RETOs
                    updateETO(Double.parseDouble(zuluMinutes));

                    newCommand(C_TURN);
                    break;

                default:
                    Log.d(TAG, "Unknown state: " + mCommand);
                    newCommand(C_ARRIVED);
                    break;
            }

        }

    }



    private void newCommand(int cmd) {

        // we need to keep a record of the requested command for the onClick state handler
        mCommand =cmd;
        switch (cmd) {
            case C_TURN:
                // Special treatment of first turn where we keep focus after start
                if (noIncrement) {
                    noIncrement = false;
                } else {
                    segmentIndex++;
                }
                updateFields();

                tvCommand.setText("TURN: Heading "+String.format("%03d ˚",(int)toWP.getMH())+"˚");
                checkBtn.setText("ON COURSE");
                break;

            case C_ARRIVED:
                tvCommand.setText("Arrived at destination "+String.format("%03d",(int)toWP.getATO()));
                checkBtn.setEnabled(false);
                checkBtn.setText("GOOD JOB");
                break;

            case C_TIME:
                // Update instruction text and make the button log time
                tvCommand.setText(mCommandList.get(cmd));
                checkBtn.setText("LOG");
                break;

            case C_TALK:
                tvCommand.setText(mCommandList.get(cmd));
                checkBtn.setText("READ");
                break;

            case C_TRACK:
                // Update instruction text and make the button confirm action
                tvCommand.setText(mCommandList.get(cmd));
                checkBtn.setText("TRACK OK");
                break;

            case C_CHECK:
                // Update check instruction text and make the button confirm action
                tvCommand.setText(mCommandList.get(cmd));
                checkBtn.setText("DONE");
                break;

            case C_TAKEOFF:
                // do nothing, but wait for the pilot to click the button
                break;

            default:
                //
                tvCommand.setText(mCommandList.get(cmd));
                checkBtn.setText("DONE");
                break;
        }

    }


    /*
     *  Update all ETOs to RETOs based on newTime parameter
     */
    private void updateETO(double newTime) {
        Log.d(TAG, "updateETO("+Double.toString(newTime)+")");

        for (int i = 0; i < markerList.size() ; i++) {
            double originalTime=markerList.get(i).getETO();
            markerList.get(i).setETO(originalTime+newTime);
        }
    }
}
