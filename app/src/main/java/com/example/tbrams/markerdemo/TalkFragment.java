package com.example.tbrams.markerdemo;

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

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.NavAids;

import java.util.List;
import java.util.Locale;


public class TalkFragment extends Fragment {
    public static final String EXTRA_SEGMENT_ID = "com.example.tbrams.markerdemo.segment_id";
    public static final String TAG = "TBR:TalkActivity";

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

        getActivity().setTitle(markerLab.getTripName());

        // Get segment id from argument bundle - this is increased from TimeFragment after turning
        // to new heading
        segmentIndex = (int) getArguments().getSerializable(EXTRA_SEGMENT_ID);
        Log.d(TAG, "onCreateView segmentIndex: "+segmentIndex);

        final View v = inflater.inflate(R.layout.talk_layout_main, container, false);

        CardView cardAt = (CardView) v.findViewById(R.id.card_talk_at);
        final TextView atName = (TextView) cardAt.findViewById(R.id.position_txt);
        final TextView atTime = (TextView) cardAt.findViewById(R.id.time_txt);
        final TextView atAlt = (TextView) cardAt.findViewById(R.id.alt_txt);

        CardView cardNext = (CardView) v.findViewById(R.id.card_talk_expect);
        final TextView expName = (TextView) cardNext.findViewById(R.id.position_txt);
        final TextView expTime = (TextView) cardNext.findViewById(R.id.time_txt);
        final TextView expAlt = (TextView) cardNext.findViewById(R.id.alt_txt);

        CardView cardThen = (CardView) v.findViewById(R.id.card_talk_then);
        final TextView thenName = (TextView) cardThen.findViewById(R.id.position_txt);

        Button btnOK = (Button) v.findViewById(R.id.talk_btn);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss this screen
                getActivity().onBackPressed();
            }
        });


        // SegmentIndex values
        // 0 has WP from 0 - 1
        // 1 has WP from 1 - 2
        // n has WP from n - (n+1)
        //
        // markerList.size
        // with 1 segment  2 WPs, markerList.size=2
        // with 2 segments 3 WPs, markerList.size=3
        // with n segment (n+1) WPs, markerList.size=(n+1)
        //
        // segmentIndex < size-2: At least 3 WP's left to report (2 segments)
        // segmentIndex < size-1: At least 2 WP's left to report on (1 segment)


        if (segmentIndex==markerList.size()-2) {
            Log.d(TAG, "onCreateView: Last segment");

            // At Card
            MarkerObject thisWP = markerList.get(segmentIndex);
            atName.setText(thisWP.getText());
            if (segmentIndex==0) {
                atTime.setText(String.format("%02d", (int)thisWP.getETO()));
            } else {
                atTime.setText(String.format("%02d", (int)thisWP.getATO()));
            }
            atAlt.setText(String.format("%.0f feet",thisWP.getALT()));


            // Expect Card
            MarkerObject expWP = markerList.get(segmentIndex+1);
            expName.setText(expWP.getText());
            expTime.setText(String.format("%02d", (int)expWP.getRETO()));
            expAlt.setText(String.format(Locale.ENGLISH, "%d feet", (int)expWP.getALT()));


            // Since we are at the end
            cardThen.removeAllViews();
            cardThen.setVisibility(View.INVISIBLE);

        } else  {

            // fill in the values
            Log.d(TAG, "onCreateView: at least 2 segments to go");

            // At Card
            MarkerObject thisWP = markerList.get(segmentIndex);
            atName.setText(thisWP.getText());
            if (segmentIndex==0) {
                atTime.setText(String.format("%02d", (int)thisWP.getETO()));
            } else {
                atTime.setText(String.format("%02d", (int)thisWP.getATO()));
            }
            atAlt.setText(String.format("%.0f feet",thisWP.getALT()));


            // Expect Card
            MarkerObject expWP = markerList.get(segmentIndex+1);
            expName.setText(expWP.getText());
            if (segmentIndex==0) {
                expTime.setText(String.format("%02d", (int)expWP.getETO()));
            } else {
                expTime.setText(String.format("%02d", (int)expWP.getRETO()));
            }
            Log.d(TAG, "onCreateView: expWP");
            Log.d(TAG, "onCreateView: getText: "+expWP.getText());
            Log.d(TAG, "onCreateView: getETO: "+expWP.getETO());
            Log.d(TAG, "onCreateView: getRETO: "+expWP.getRETO());
            Log.d(TAG, "onCreateView: getATO: "+expWP.getATO());
            expAlt.setText(String.format(Locale.ENGLISH, "%d feet", (int)expWP.getALT()));

            // Then Card
            // make it visible, just in case...
            cardThen.setVisibility(View.VISIBLE);
            MarkerObject thenWP = markerList.get(segmentIndex+2);
            Log.d(TAG, "onCreateView: thenWP");
            Log.d(TAG, "onCreateView: getText: "+expWP.getText());
            Log.d(TAG, "onCreateView: getETO: "+thenWP.getETO());
            Log.d(TAG, "onCreateView: getRETO: "+thenWP.getRETO());
            Log.d(TAG, "onCreateView: getATO: "+thenWP.getATO());

            thenName.setText(thenWP.getText());
        }





        /*



        if (segmentIndex+2<markerList.size()) {
            // In here, we have at least two segments to work on

            // start with the Expect fields
            MarkerObject nextWP = markerList.get(segmentIndex+1);
            tvPositionNextName.setText(nextWP.getText());

            // Use ETO for first point instead of RETO
            if (segmentIndex==0) {
                tvPositionNextTime.setText(String.format("%02d",(int)nextWP.getETO()));
            } else {
                tvPositionNextTime.setText(String.format("%02d",(int)nextWP.getRETO()));
            }

            tvPositionNextAlt.setText(String.format("%.0f",nextWP.getALT()));

            // Then info
            MarkerObject thenWP = markerList.get(segmentIndex+2);
            tvPositionThenName.setText(thenWP.getText());

        } else if (segmentIndex==markerList.size()-3) {
            // Just a report point and then final dest in the expect position of the layout

            // Hide the next time and alt fields and labels
            tvPositionNextAlt.setVisibility(View.INVISIBLE);
            tvPositionNextAltLabel.setVisibility(View.INVISIBLE);
            tvPositionNextTime.setVisibility(View.INVISIBLE);
            tvPositionNextTimeLabel.setVisibility(View.INVISIBLE);

            // change next label to be final destination "Then"
            tvPositionNextLabel.setText("Then");
            tvPositionNextName.setText(markerList.get(segmentIndex+2).getText());

            // hide label and textView for original Then field
            tvPositionThenName.setVisibility(View.INVISIBLE);
            tvPositionThenNameLabel.setVisibility(View.INVISIBLE);
        } else {
            // Only one point to report

            tvPositionName.setText("Expecting "+ thisWP.getText()+" at "+ String.format("%2d", (int)thisWP.getRETO()));

            // hide normal time
            tvPositionTime.setVisibility(View.INVISIBLE);


            // Hide altitude
            tvPositionAlt.setVisibility(View.INVISIBLE);

            // Hide the next time and alt fields and labels
            tvPositionNextName.setVisibility(View.INVISIBLE);
            tvPositionNextLabel.setVisibility(View.INVISIBLE);
            tvPositionNextAlt.setVisibility(View.INVISIBLE);
            tvPositionNextAltLabel.setVisibility(View.INVISIBLE);
            tvPositionNextTime.setVisibility(View.INVISIBLE);
            tvPositionNextTimeLabel.setVisibility(View.INVISIBLE);

            // hide label and textView for original Then field
            tvPositionThenName.setVisibility(View.INVISIBLE);
            tvPositionThenNameLabel.setVisibility(View.INVISIBLE);
            tvPositionFeetLabel.setVisibility(View.INVISIBLE);
            tvPositionPTLabel.setVisibility(View.INVISIBLE);

        }
*/

        return v;
    }



}
