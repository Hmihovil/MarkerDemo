package com.example.tbrams.markerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tbrams.markerdemo.data.ExtraMarkers;
import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Pejling;

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
    private TextView timeRetoLbl;
    private TextView timeRetoTxt;
    private TextView timeDiffTxt;
    private TextView commandTxt;
    private Button commandBtn;
    private TextView VORtext1, VORrad1, VORdist1;
    private TextView VORtext2, VORrad2, VORdist2;
    private TextView VORtext3, VORrad3, VORdist3;
    private Time mTime;
    private Boolean noIncrement=true;

    private TextView nextLabel, nextDist, nextHdg;

    private Button timeBtn, talkBtn, nextBtn;

    private MarkerObject fromWP;
    private MarkerObject toWP;
    private static int mCommand;
    private List<String> mCommandList = new ArrayList();

    ExtraMarkers sExtraMarkers = ExtraMarkers.get(getActivity());
    List<NavAid> mNavAidList= sExtraMarkers.getNavAidList();

    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().setTitle(markerLab.getTripName());

        mTime=new Time();

        v = inflater.inflate(R.layout.time_layout_main, container, false);

        CardView nextLocationCard = (CardView) v.findViewById(R.id.card_next_location);
        nextLabel = (TextView) nextLocationCard.findViewById(R.id.heading_label);
        nextDist = (TextView) nextLocationCard.findViewById(R.id.dist_txt);
        nextHdg = (TextView) nextLocationCard.findViewById(R.id.hdg_txt);

        CardView timeCard = (CardView) v.findViewById(R.id.card_time);
        timeRetoLbl = (TextView) timeCard.findViewById(R.id.reto_lbl);
        timeRetoTxt = (TextView) timeCard.findViewById(R.id.reto_txt);
        timeDiffTxt = (TextView) timeCard.findViewById(R.id.diff_txt);

        CardView cmdCard = (CardView) v.findViewById(R.id.card_time_command);
        commandTxt = (TextView) cmdCard.findViewById(R.id.command_txt);
        commandBtn = (Button) cmdCard.findViewById(R.id.command_btn);
        commandBtn.setOnClickListener(this);


        CardView vorCard = (CardView) v.findViewById(R.id.card_vor);
        VORtext1 = (TextView) vorCard.findViewById(R.id.vor1_txt);
        VORrad1 = (TextView) vorCard.findViewById(R.id.vor1rad_txt);
        VORdist1 = (TextView) vorCard.findViewById(R.id.vor1dist_txt);

        VORtext2 = (TextView) vorCard.findViewById(R.id.vor2_txt);
        VORrad2 = (TextView) vorCard.findViewById(R.id.vor2rad_txt);
        VORdist2 = (TextView) vorCard.findViewById(R.id.vor2dist_txt);

        VORtext3 = (TextView) vorCard.findViewById(R.id.vor3_txt);
        VORrad3 = (TextView) vorCard.findViewById(R.id.vor3rad_txt);
        VORdist3 = (TextView) vorCard.findViewById(R.id.vor3dist_txt);





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

        ArrayList<Pejling> pejlinger = toWP.getPejlinger();

        // Next Location Card
        nextLabel.setText(String.format(Locale.ENGLISH, "Next WP: %s", toWP.getText()));
        nextHdg.setText(String.format(Locale.ENGLISH, "%03d ˚", (int) toWP.getMH()));
        nextDist.setText(String.format(Locale.ENGLISH, "%.1f nm", toWP.getDist()));

//        tvWPnumber.setText( String.format(Locale.ENGLISH, "%d", segmentIndex+1));
//        tvWPtotal.setText( String.format(Locale.ENGLISH, "%d", markerList.size()-1));


        // Time Card
        if (segmentIndex==0) {
            // Special for first Segment. We only have ETO for first destination (No ATO, hence no RETO)
            // TODO: Need some time formatting here later

            timeRetoLbl.setText(R.string.ETO);
            timeRetoTxt.setText(String.format(Locale.ENGLISH, "%02d", (int) toWP.getETO()));
        } else {

            timeRetoLbl.setText(R.string.RETO);
            timeRetoTxt.setText(String.format(Locale.ENGLISH, "%02d", (int)toWP.getRETO()));
        }

        // Time difference
        timeDiffTxt.setText(String.format(Locale.ENGLISH, "%02d", (int)fromWP.getDiff()));


        // Command card
        commandTxt.setText(mCommandList.get(C_TAKEOFF));

        // VOR Card
        VORtext1.setText(mNavAidList.get((pejlinger.get(0).getMarkerIndex())).getName());
        VORtext2.setText(mNavAidList.get((pejlinger.get(1).getMarkerIndex())).getName());
        VORtext3.setText(mNavAidList.get((pejlinger.get(2).getMarkerIndex())).getName());

        VORrad1.setText(String.format("%03d \u00B0", (int)(pejlinger.get(0).getHeading() + 360) % 360));
        VORrad2.setText(String.format("%03d \u00B0", (int)(pejlinger.get(1).getHeading() + 360) % 360));
        VORrad3.setText(String.format("%03d \u00B0", (int)(pejlinger.get(2).getHeading() + 360) % 360));

        VORdist1.setText(String.format("%.2f nm", pejlinger.get(0).getDistance() / 1852.));
        VORdist2.setText(String.format("%.2f nm", pejlinger.get(1).getDistance() / 1852.));
        VORdist3.setText(String.format("%.2f nm", pejlinger.get(2).getDistance() / 1852.));

    }



    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume...");

    }



    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.command_btn) {

            String zuluMinutes;
            String zuluTime;

            // Process Click action depending on what state we are in
            switch (mCommand) {
                case C_TIME:
                    // Get zulu time in minutes and set ATO.
                    // Unlike the Pinto board situation, difference will be auto calculated here ^_^
                    zuluTime    = mTime.getZuluTime();
                    String[] zStrings=zuluTime.split(":");
                    Double zMins = Double.parseDouble(zStrings[0])*60+Integer.parseInt(zStrings[1]);
                    zuluMinutes = zStrings[1];
                    Toast.makeText(getActivity(), "Time logged: "+zuluTime, Toast.LENGTH_SHORT).show();

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
                        toWP.setATO(zMins);
                        newCommand(C_ARRIVED);
                    }
                    break;

                case C_TURN:

                    newCommand(C_TRACK);
                    break;


                case C_TRACK:

                    newCommand(C_TALK);
                    break;


                case C_TALK:

                    // Launch talk activity with info on this segment
                    Intent intent = TalkActivity.newIntent(getActivity(), segmentIndex);
                    startActivity(intent);

                    newCommand(C_CHECK);
                    break;


                case C_CHECK:
                    newCommand(C_TIME);
                    break;


                case C_ARRIVED:
                    break;

                case C_TAKEOFF:
                    // Got the cleared for Take Off, record the time and update all ETO times based
                    // on current time plus previous ETO estimate.


                    zuluTime = mTime.getZuluTime();
                    zuluMinutes = zuluTime.split(":")[1];

                    Log.d(TAG, "Takeoff Zulu Time is: "+zuluTime);
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

                commandTxt.setText(String.format(Locale.ENGLISH, "TURN: Heading %03d˚",(int)toWP.getMH()));
                commandBtn.setText("ON COURSE");
                break;

            case C_ARRIVED:
                int hrs = (int)(toWP.getATO()/60);
                int min = (int)(toWP.getATO()%60);
                commandTxt.setText(String.format(Locale.ENGLISH, "Arrived at destination\nTime was: %02d:%02d",hrs, min));
                commandBtn.setEnabled(false);
                commandBtn.setText("GOOD JOB");
                break;

            case C_TIME:
                // Update instruction text and make the button log time
                commandTxt.setText(mCommandList.get(cmd));
                commandBtn.setText("LOG");
                break;

            case C_TALK:
                commandTxt.setText(mCommandList.get(cmd));
                commandBtn.setText("TALK");
                break;

            case C_TRACK:
                // Update instruction text and make the button confirm action
                commandTxt.setText(mCommandList.get(cmd));
                commandBtn.setText("TRACK OK");
                break;

            case C_CHECK:
                // Update check instruction text and make the button confirm action
                commandTxt.setText(mCommandList.get(cmd));
                commandBtn.setText("DONE");
                break;

            case C_TAKEOFF:
                // do nothing, but wait for the pilot to click the button
                break;

            default:
                //
                commandTxt.setText(mCommandList.get(cmd));
                commandBtn.setText("DONE");
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
