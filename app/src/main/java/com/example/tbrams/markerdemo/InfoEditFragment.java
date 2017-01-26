package com.example.tbrams.markerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tbrams.markerdemo.data.ExtraMarkers;
import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Pejling;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.example.tbrams.markerdemo.NavPagerActivity.setSomethingUpdated;

public class InfoEditFragment extends Fragment implements View.OnClickListener {

    public static final String EXTRA_MARKER_ID = "com.example.tbrams.markerdemo.marker_id";
    public static final int    ACTION_DELETE = 2;
    public static final int    ACTION_CANCEL = 3;
    private static final String TAG = "TBR:";

    private int      markerIndex = -1;
    private EditText mEditName ;
    private EditText mEditAltitude;
    private EditText mEditNote;

    private Button   btnUpdate, btnDelete, btnCancel;
    private Vibrator mVib;


    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();

    ExtraMarkers sExtraMarkers = ExtraMarkers.get(getActivity());
    List<NavAid> mNavAidList= sExtraMarkers.getNavAidList();


    public static InfoEditFragment newInstance(int markerIndex) {

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_MARKER_ID, markerIndex);

        InfoEditFragment fragment = new InfoEditFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Now inflate the detailed layout for each WP - at this stage, it is a ScrollView with a number of specially formatted
        // CardViews - one updating/deleting, one for Next WP, one for VOR
        final View v = inflater.inflate(R.layout.info_window_edit, container, false);

        // Get marker id from argument bundle
        markerIndex = (int) getArguments().getSerializable(EXTRA_MARKER_ID);

        Log.d(TAG, "InfoEditFragment, received markerIndex from fragment arguments: "+markerIndex);

        final MarkerObject mo =markerList.get(markerIndex);


        // Get a reference to the individual cards in the layout, so we can find the individual
        // widgets with ease (otherwise naming might clash because there are so many)
        CardView cardInput = (CardView) v.findViewById(R.id.card_name_input);
        CardView cardLocation = (CardView) v.findViewById(R.id.card_location);
        CardView cardNextLocation = (CardView) v.findViewById(R.id.card_next_location);
        CardView cardVOR = (CardView) v.findViewById(R.id.card_vor);


        // Setup cardInput - these are the fields we are looking to handle
        mEditName = (EditText) cardInput.findViewById(R.id.wpname_txt);
        mEditAltitude = (EditText) cardInput.findViewById(R.id.wpalt_txt);
        mEditNote = (EditText) cardInput.findViewById(R.id.wpnote_txt);

        // Update Edit Text fields with marker provided info
        mEditName.setText(mo.getText());
        mEditNote.setText(mo.getNote());
        mEditAltitude.setText(Double.toString(mo.getALT()));

        // set a lost focus event handler to automatically update the position name
        mEditName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mEditName.getText()!=null) {
                        Log.d(TAG, "InfoEditFrament, focus lost in text field");
                        if (!mo.getText().equals(String.valueOf(mEditName.getText()))) {
                            Log.d(TAG, "Something updated in text");
                            setSomethingUpdated(true);
                            mo.setText(String.valueOf(mEditName.getText()));
                        }
                    }
                }
            }
        });


        // Next Location Card
        TextView WPtextLabel = (TextView) cardNextLocation.findViewById(R.id.heading_label);
        TextView WPtextDist = (TextView) cardNextLocation.findViewById(R.id.dist_txt);
        TextView WPtextHeading = (TextView) cardNextLocation.findViewById(R.id.hdg_txt);

        if ((markerIndex+1)<markerList.size()) {
            WPtextLabel.setText(String.format(Locale.ENGLISH, "Next WP: %s", markerList.get(markerIndex + 1).getText()));
            double dist =markerList.get(markerIndex+1).getDist();
            double heading = markerList.get(markerIndex+1).getTT();
            WPtextDist.setText(String.format(Locale.ENGLISH, "%.1f nm", dist));
            WPtextHeading.setText(String.format(Locale.ENGLISH, "%.0f ˚", heading));
        } else {
            WPtextLabel.setText("Final destination") ;
            WPtextDist.setText("NA");
            WPtextHeading.setText("NA");
        }

        // VOR Card
        ArrayList<Pejling> pejlinger = markerList.get(markerIndex).getPejlinger();

        TextView VORtext1 = (TextView) cardVOR.findViewById(R.id.vor1_txt);
        TextView VORrad1 = (TextView) cardVOR.findViewById(R.id.vor1rad_txt);
        TextView VORdist1 = (TextView) cardVOR.findViewById(R.id.vor1dist_txt);

        TextView VORtext2 = (TextView) cardVOR.findViewById(R.id.vor2_txt);
        TextView VORrad2 = (TextView) cardVOR.findViewById(R.id.vor2rad_txt);
        TextView VORdist2 = (TextView) cardVOR.findViewById(R.id.vor2dist_txt);

        TextView VORtext3 = (TextView) cardVOR.findViewById(R.id.vor3_txt);
        TextView VORrad3 = (TextView) cardVOR.findViewById(R.id.vor3rad_txt);
        TextView VORdist3 = (TextView) cardVOR.findViewById(R.id.vor3dist_txt);

        VORtext1.setText(mNavAidList.get((pejlinger.get(0).getMarkerIndex())).getName());
        VORtext2.setText(mNavAidList.get((pejlinger.get(1).getMarkerIndex())).getName());
        VORtext3.setText(mNavAidList.get((pejlinger.get(2).getMarkerIndex())).getName());

        VORrad1.setText(String.format("%03d \u00B0", (int)(pejlinger.get(0).getHeading() + 360) % 360));
        VORrad2.setText(String.format("%03d \u00B0", (int)(pejlinger.get(1).getHeading() + 360) % 360));
        VORrad3.setText(String.format("%03d \u00B0", (int)(pejlinger.get(2).getHeading() + 360) % 360));

        VORdist1.setText(String.format("%.2f nm", pejlinger.get(0).getDistance() / 1852.));
        VORdist2.setText(String.format("%.2f nm", pejlinger.get(1).getDistance() / 1852.));
        VORdist3.setText(String.format("%.2f nm", pejlinger.get(2).getDistance() / 1852.));



        // Location Card
        TextView tvLat = (TextView) cardLocation.findViewById(R.id.lat_txt);
        TextView tvLon = (TextView) cardLocation.findViewById(R.id.lon_txt);
        TextView tvVar = (TextView) cardLocation.findViewById(R.id.mag_txt);

        tvLat.setText(String.format("%.2f  °", mo.getMarker().getPosition().latitude));
        tvLon.setText(String.format("%.2f  °", mo.getMarker().getPosition().longitude));
        tvVar.setText(String.format("%.1f  °", mo.getVAR()));

        btnUpdate = (Button) cardInput.findViewById(R.id.btn_update);
        btnDelete = (Button) cardInput.findViewById(R.id.btn_delete);
        btnCancel = (Button) cardInput.findViewById(R.id.btn_cancel);
        btnUpdate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        mVib = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);

        return v;
    }



    /*
     * Helper function used to send results back to the main activity where it can
     * be processed in the onActivityResult method
     */
    private void sendResult(int resultCode, int markerIndex) {

        Intent intent = new Intent();
        intent.putExtra(EXTRA_MARKER_ID, markerIndex);
        getActivity().setResult(resultCode, intent);
        getActivity().onBackPressed();

        mVib.vibrate(30);
    }


    /*
     * Click Handler for this screen
     * Will take care of update, delete and cancel. Both delete and cancel will
     * end the activity, but update will merely update the data values with the
     * text provided by the user.
     */
    @Override
    public void onClick(View view) {
        if (view == btnUpdate) {
            String titleString = String.valueOf(mEditName.getText());
            String snippetString = String.valueOf(mEditNote.getText());
            Double alt=0.0;
            if (!mEditAltitude.toString().equals("")) {
                String altString = mEditAltitude.getText().toString();
                alt = Double.parseDouble(altString);
            }

            // update marker data
            markerList.get(markerIndex).setText(titleString);
            markerList.get(markerIndex).setNote(snippetString);
            markerList.get(markerIndex).setALT(alt);

            // update physical markers
            markerList.get(markerIndex).getMarker().setTitle(titleString);
            markerList.get(markerIndex).getMarker().setSnippet(snippetString);

            setSomethingUpdated(true);

        } else if (view == btnDelete) {
            sendResult(ACTION_DELETE, markerIndex);


        } else if (view == btnCancel) {
            setSomethingUpdated(false);
            sendResult(ACTION_CANCEL, markerIndex);


        }

    }
}
