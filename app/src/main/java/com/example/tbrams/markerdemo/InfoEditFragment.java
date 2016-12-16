package com.example.tbrams.markerdemo;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.NavAids;
import com.example.tbrams.markerdemo.data.Pejling;

import java.util.ArrayList;
import java.util.List;

public class InfoEditFragment extends Fragment implements View.OnClickListener {

    public static final String EXTRA_MARKER_ID = "com.example.tbrams.markerdemo.marker_id";
    public static final int ACTION_UPDATE = 1;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_CANCEL = 3;

    private int markerIndex = -1;
    private Button btnUpdate, btnDelete, btnCancel;


    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();
    NavAids navaids = NavAids.get(getActivity());
    List<NavAid> vorList = navaids.getList();


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
        final View v = inflater.inflate(R.layout.info_window_edit, container, false);

        // Get marker id from argument bundle
        markerIndex = (int) getArguments().getSerializable(EXTRA_MARKER_ID);

        // Update Edit Text fields with marker provided info
        EditText eTit = (EditText) v.findViewById(R.id.placeText);
        eTit.setText(markerList.get(markerIndex).getText());
        EditText eSnp = (EditText) v.findViewById(R.id.snippetText);
        eSnp.setText(markerList.get(markerIndex).getSnippet());

        // Update Next WP fields
        TextView WPtextLabel = (TextView) v.findViewById(R.id.textViewNextWP);
        if ((markerIndex+1)<markerList.size()) {
            TextView WPtextDist = (TextView) v.findViewById(R.id.textViewDistance);
            TextView WPtextHeading = (TextView) v.findViewById(R.id.textViewHeading);
            WPtextLabel.setText("Next WP: "+markerList.get(markerIndex+1).getText());
            double dist =markerList.get(markerIndex).getDist();
            double heading = markerList.get(markerIndex).getTT();
            WPtextDist.setText(String.format("%.1f nm", dist));
            WPtextHeading.setText(String.format("%.0f ˚", heading));
        } else {
            WPtextLabel.setText("Final destination") ;
        }

        // update VOR fields for marker
        ArrayList<Pejling> pejlinger = markerList.get(markerIndex).getPejlinger();
        TextView VORtext1 = (TextView) v.findViewById(R.id.VORname1);
        TextView VORrad1 = (TextView) v.findViewById(R.id.VORrad1);
        TextView VORdist1 = (TextView) v.findViewById(R.id.VORdist1);

        TextView VORtext2 = (TextView) v.findViewById(R.id.VORname2);
        TextView VORrad2 = (TextView) v.findViewById(R.id.VORrad2);
        TextView VORdist2 = (TextView) v.findViewById(R.id.VORdist2);

        TextView VORtext3 = (TextView) v.findViewById(R.id.VORname3);
        TextView VORrad3 = (TextView) v.findViewById(R.id.VORrad3);
        TextView VORdist3 = (TextView) v.findViewById(R.id.VORdist3);

        VORtext1.setText(vorList.get((pejlinger.get(0).getMarkerIndex())).getName());
        VORtext2.setText(vorList.get((pejlinger.get(1).getMarkerIndex())).getName());
        VORtext3.setText(vorList.get((pejlinger.get(2).getMarkerIndex())).getName());

        VORrad1.setText(String.format("%.1f \u00B0", (pejlinger.get(0).getHeading() + 360) % 360));
        VORrad2.setText(String.format("%.1f \u00B0", (pejlinger.get(1).getHeading() + 360) % 360));
        VORrad3.setText(String.format("%.1f \u00B0", (pejlinger.get(2).getHeading() + 360) % 360));

        VORdist1.setText(String.format("%.2f nm", pejlinger.get(0).getDistance() / 1852.));
        VORdist2.setText(String.format("%.2f nm", pejlinger.get(1).getDistance() / 1852.));
        VORdist3.setText(String.format("%.2f nm", pejlinger.get(2).getDistance() / 1852.));


        btnUpdate = (Button) v.findViewById(R.id.buttonUpdate);
        btnDelete = (Button) v.findViewById(R.id.buttonDelete);
        btnCancel = (Button) v.findViewById(R.id.buttonCancel);
        btnUpdate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

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
            EditText eTitle = (EditText)  getView().findViewById(R.id.placeText);
            EditText eSnippet = (EditText) getView().findViewById(R.id.snippetText);
            String titleString = String.valueOf(eTitle.getText());
            String snippetString = String.valueOf(eSnippet.getText());

            // update marker data
            markerList.get(markerIndex).setText(titleString);
            markerList.get(markerIndex).setSnippet(snippetString);

            // update physical markers
            markerList.get(markerIndex).getMarker().setTitle(titleString);
            markerList.get(markerIndex).getMarker().setSnippet(snippetString);

        } else if (view == btnDelete) {
            sendResult(ACTION_DELETE, markerIndex);


        } else if (view == btnCancel) {
            sendResult(ACTION_CANCEL, markerIndex);


        }

    }
}
