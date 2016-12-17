package com.example.tbrams.markerdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.NavAids;

import java.util.List;


public class TalkFragment extends Fragment {
    public static final String EXTRA_MARKER_ID = "com.example.tbrams.markerdemo.marker_id";

    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();
    NavAids navaids = NavAids.get(getActivity());
    List<NavAid> vorList = navaids.getList();

    private int markerIndex = -1;



    public static TalkFragment newInstance(int markerIndex){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_MARKER_ID, markerIndex);

        TalkFragment fragment = new TalkFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Get marker id from argument bundle
        markerIndex = (int) getArguments().getSerializable(EXTRA_MARKER_ID);
        Log.d("TBR:","TalkFragment/onCreateView - intent/markerIndex: "+markerIndex);

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

        MarkerObject thisWP = markerList.get(markerIndex);

        if (markerIndex+1<markerList.size()) {
            MarkerObject nextWP = markerList.get(markerIndex+1);
            tvPositionNextName.setText(nextWP.getText());
            tvPositionNextTimeLabel.setVisibility(View.VISIBLE);
            tvPositionNextTime.setText(String.format("%0f",nextWP.getRETO()));
            tvPositionNextAlt.setVisibility(View.VISIBLE);
            tvPositionNextTime.setText(String.format("%0f",nextWP.getALT()));
        } else {
            // hide labels
            tvPositionNextAlt.setVisibility(View.INVISIBLE);
            tvPositionNextAltLabel.setVisibility(View.INVISIBLE);
            tvPositionNextTime.setVisibility(View.INVISIBLE);
            tvPositionNextTimeLabel.setVisibility(View.INVISIBLE);
        }

        if (markerIndex+2<markerList.size()) {
            MarkerObject thenWP = markerList.get(markerIndex+2);
            tvPositionThenName.setVisibility(View.INVISIBLE);
            tvPositionThenName.setText(thenWP.getText());
            tvPositionThenNameLabel.setVisibility(View.INVISIBLE);
        } else {
            // hide labels
            tvPositionThenName.setVisibility(View.INVISIBLE);
            tvPositionThenNameLabel.setVisibility(View.INVISIBLE);
        }


        return v;
    }


}
