package com.example.tbrams.markerdemo;

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


public class TalkFragment extends Fragment {
    public static final String EXTRA_SEGMENT_ID = "com.example.tbrams.markerdemo.segment_id";

    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();
    NavAids navaids = NavAids.get(getActivity());
    List<NavAid> vorList = navaids.getList();

    private int segmentIndex = -1;



    public static TalkFragment newInstance(int segmentIndex){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_SEGMENT_ID, segmentIndex);

        TalkFragment fragment = new TalkFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Get marker id from argument bundle
        segmentIndex = (int) getArguments().getSerializable(EXTRA_SEGMENT_ID);
        Log.d("TBR:","TalkFragment/onCreateView - intent/segmentIndex: "+segmentIndex);

        final View v = inflater.inflate(R.layout.talk_fragment_layout, container, false);
        final TextView tvPositionName = (TextView) v.findViewById(R.id.textViewPositionName);
        final TextView tvPositionTime = (TextView) v.findViewById(R.id.textViewPositionTime);
        final TextView tvPositionAlt = (TextView) v.findViewById(R.id.textViewPositionAlt);
        final TextView tvPositionNextName = (TextView) v.findViewById(R.id.textViewPositionNextName);
        final TextView tvPositionNextTime = (TextView) v.findViewById(R.id.textViewPositionNextTime);
        final TextView tvPositionNextTimeLabel = (TextView) v.findViewById(R.id.textViewPositionNextTimeLabel);
        final TextView tvPositionNextAlt = (TextView) v.findViewById(R.id.textViewPositionNextAlt);
        final TextView tvPositionNextAltLabel = (TextView) v.findViewById(R.id.textViewPositionNextAltLabel);
        final TextView tvPositionThenName = (TextView) v.findViewById(R.id.textViewPositionThenName);
        final TextView tvPositionThenNameLabel = (TextView) v.findViewById(R.id.textViewPositionThenLabel);
        final TextView tvPositionNextLabel   = (TextView) v.findViewById(R.id.textViewExpLabel);

        MarkerObject thisWP = markerList.get(segmentIndex+1);
        tvPositionName.setText(thisWP.getText());
        tvPositionTime.setText(String.format("%.0f",thisWP.getTIME()));
        tvPositionAlt.setText(String.format("%.0f", thisWP.getALT()));


        if ((markerList.size()-segmentIndex)>3) {
            // At least two segments to work on, start with the Expect fields
            MarkerObject nextWP = markerList.get(segmentIndex+2);
            tvPositionNextName.setText(nextWP.getText());
            tvPositionNextTime.setText(String.format("%.0f",nextWP.getRETO()));
            tvPositionNextAlt.setText(String.format("%.0f",nextWP.getALT()));

            // Then info
            MarkerObject thenWP = markerList.get(segmentIndex+3);
            tvPositionThenName.setText(thenWP.getText());

        } else {
            // Just a report point and then final dest in the expect position of the layout

            // Hide the next time and alt fields and labels
            tvPositionNextAlt.setVisibility(View.INVISIBLE);
            tvPositionNextAltLabel.setVisibility(View.INVISIBLE);
            tvPositionNextTime.setVisibility(View.INVISIBLE);
            tvPositionNextTimeLabel.setVisibility(View.INVISIBLE);

            // change next label to be final destination "Then"
            tvPositionNextLabel.setText("Then");
 //           tvPositionNextName.setText(markerList.get(segmentIndex+1).getText());
            tvPositionNextName.setText("TEMP");

            // hide label and textView for original Then field
            tvPositionThenName.setVisibility(View.INVISIBLE);
            tvPositionThenNameLabel.setVisibility(View.INVISIBLE);

        }


        Button btnOK = (Button) v.findViewById(R.id.btnPositionOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss this screen
                getActivity().onBackPressed();
            }
        });

        return v;
    }



}
