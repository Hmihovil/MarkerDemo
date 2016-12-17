package com.example.tbrams.markerdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.NavAids;

import java.util.List;


public class ETOFragment extends Fragment {

    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();
    NavAids navaids = NavAids.get(getActivity());
    List<NavAid> vorList = navaids.getList();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.eto_layout, container, false);
        final EditText editETO = (EditText) v.findViewById(R.id.editETO);

        final Button btnOK = (Button) v.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TBR:", "OK Clicked");

                if (!String.valueOf(editETO.getText()).matches("\\d{2}")) {
                    editETO.setError("Please write ETO in minutes, e.g. 07");
                    return;
                }

                // Use the Estimated Time of Departure to calculate ETO for all points
                int ETOD=Integer.parseInt(String.valueOf(editETO.getText()));
                for (int i = 0; i < markerList.size() ; i++) {
                    double t=markerList.get(i).getTIME();
                    markerList.get(i).setETO(t+ETOD);
                }
            }
        });

        Button btnNow = (Button) v.findViewById(R.id.btnNow);
        btnNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TBR:", "OK Button clicked");
            }
        });


        Button btnQuit = (Button) v.findViewById(R.id.btnQuit);
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TBR:", "Quit Button clicked");
                getActivity().onBackPressed();
            }
        });

        return v;
    }
}
