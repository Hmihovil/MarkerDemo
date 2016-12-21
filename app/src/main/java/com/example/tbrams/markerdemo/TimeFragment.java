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
    private static final int C_READY=6;
    private static final int C_AFIS=7;
    private static final int C_LAND=8;

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
        newCommand(C_READY);

        return v;

    }


    private boolean hasNextSegment() {
        return ((segmentIndex + 1) < (markerList.size() - 1));
    }




    private void updateFields() {

        fromWP = markerList.get(segmentIndex);
        toWP   = markerList.get(segmentIndex+1);

        // Segment Number and Total
        tvWPnumber.setText( Integer.toString(segmentIndex+1));
        tvWPtotal.setText( Integer.toString(markerList.size()-1));

        // Next WP Name
        tvNextWP.setText(toWP.getText());

        // Heading and Distance
        tvHeading.setText(String.format("%03d ˚", (int) toWP.getMH()));
        tvDistance.setText(String.format("%.1f nm", toWP.getDist()));

        // RETO
        if (segmentIndex==0) {
            // Special for first Segment
            // We only have ETO for first destination (No ATO, hence no RETO)
            // TODO: Need some time formatting here later

            tvRetoLbl.setText("ETO");
            tvRETO.setText(String.format("%03d", (int) toWP.getETO()));
        } else {

            tvRetoLbl.setText("RETO");
            tvRETO.setText(String.format("%03d", (int)toWP.getRETO()));
        }

        // Time difference
        tvDiff.setText(String.format("%02d", (int)fromWP.getDiff()));

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
                    Log.d("TBR:", "C_TIME State");

                    // Get zulu time in minutes and set ATO. Difference will be auto calculated
                    String minutes = mTime.getZuluTime().split(":")[1];
                    toWP.setATO(Double.parseDouble(minutes));
                    Log.d("TBR:", "WP "+toWP.getText()+" ATO set to: "+minutes);
                    Log.d("TBR:", "WP "+toWP.getText()+" DIFF found: "+toWP.getDiff());

                    if (segmentIndex<markerList.size()-2) {
                        // This is not the final leg
                        MarkerObject thenWP = markerList.get(segmentIndex+2);
                        double RETO = thenWP.getETO() - toWP.getDiff();
                        thenWP.setRETO(RETO);

                        // Debug
                        Log.d("TBR:", thenWP.getText()+" RETO set to: " + RETO);
                        Log.d("TBR:", "Dumping ETO/RETO for all markers here");
                        for (int i=1;i<markerList.size();i++){
                            Log.d("TBR:", markerList.get(i).getText() + " ETO: "+markerList.get(i).getETO()+" RETO: "+markerList.get(i).getRETO());
                        }

                        // We have arrived at toWP and got the time already
                        // Next action is to change heading towards new point
                        newCommand(C_TURN);

                    } else {

                        newCommand(C_ARRIVED);
                    }
                    break;

                case C_TURN:
                    Log.d("TBR:", "C_TURN State");

                    newCommand(C_TRACK);
                    break;


                case C_TRACK:
                    Log.d("TBR:", "C_TRACK State");

                    newCommand(C_TALK);
                    break;


                case C_TALK:
                    Log.d("TBR:", "C_TALK State");

                    Intent intent = TalkActivity.newIntent(getActivity(), segmentIndex);
                    startActivity(intent);

                    newCommand(C_CHECK);
                    break;


                case C_CHECK:
                    Log.d("TBR:", "C_CHECK State");
                    newCommand(C_TIME);
                    break;


                case C_ARRIVED:
                    Log.d("TBR:", "C_ARRIVED State");
                    break;

                case C_READY:
                    Log.d("TBR:", "C_READY State");
                    newCommand(C_TURN);
                    break;

                default:
                    Log.d("TBR:", "Unknown state: " + mCommand);
                    newCommand(C_ARRIVED);
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



    private void newCommand(int cmd) {
        mCommand =cmd;
        switch (cmd) {
            case C_TURN:
                // Special treatment of first turn where we keep focus after start
                if (noIncrement) {
                    noIncrement = false;
                } else {
                    segmentIndex++;
                    updateFields();
                }
                tvCommand.setText("TURN: Heading "+String.format("%03d",(int)toWP.getMH())+"˚");
                checkBtn.setText("DONE");
                break;

            case C_ARRIVED:
                tvCommand.setText("Arrived at destination "+String.format("%03d",(int)toWP.getATO()));
                checkBtn.setEnabled(false);
                checkBtn.setText("DONE");
                break;

            case C_TIME:
                tvCommand.setText(mCommandList.get(cmd));
                checkBtn.setText("LOG");
                break;

            case C_TALK:
                tvCommand.setText(mCommandList.get(cmd));
                checkBtn.setText("READ");
                break;

            default:
                tvCommand.setText(mCommandList.get(cmd));
                checkBtn.setText("DONE");
                break;
        }


    }
}
