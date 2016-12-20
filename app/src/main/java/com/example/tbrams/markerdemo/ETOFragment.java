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
import android.widget.EditText;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.NavAids;

import java.util.List;


public class ETOFragment extends Fragment {

    private MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    private List<MarkerObject> markerList = markerLab.getMarkers();
    private NavAids navaids = NavAids.get(getActivity());
    private List<NavAid> vorList = navaids.getList();
    private Time mTime;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.eto_layout, container, false);
        final EditText editETO = (EditText) v.findViewById(R.id.editETO);
        mTime = new Time();


        // OK Button
        final Button btnOK = (Button) v.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TBR:", "ETOFragment - OK Clicked");

                if (!String.valueOf(editETO.getText()).matches("\\d\\d?")) {
                    editETO.setError("Please write ETO in minutes, e.g. 07");
                    return;
                }

                // Calculate RETO for all points
                updateETO(Integer.parseInt(String.valueOf(editETO.getText())));

                dumpETOs();  // Just for debugging...

                Intent intent = new Intent(getActivity(), TimeActivity.class);
                startActivity(intent);

                Log.d("TBR:", "TimeActivity Started...");

            }
        });


        // NOW Button
        Button btnNow = (Button) v.findViewById(R.id.btnNow);
        btnNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = mTime.getZuluTime();
                Log.d("TBR:", "Zulu Time is: "+time);

                String minutes = time.split(":")[1];
                Log.d("TBR:", "Minutes is: "+minutes);

                // use it to generate RETOs
                updateETO(Double.parseDouble(minutes));

                dumpETOs();  // Just for debugging...

                Intent intent = new Intent(getActivity(), TimeActivity.class);
                startActivity(intent);

                Log.d("TBR:", "TimeActivity Started...");


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


   /*
    * Debug only - dump all RETOs
    */
    private void dumpETOs() {
        for (int i=1;i<markerList.size();i++){
            Log.d("TBR:", "ETO for "+markerList.get(i).getText()+" is "+markerList.get(i).getETO());
        }
    }


   /*
    *  Update all ETOs to RETOs based on newTime parameter
    */
    private void updateETO(double newTime) {
        Log.d("TBR:", "updateETO("+Double.toString(newTime)+")");

        for (int i = 0; i < markerList.size() ; i++) {
            double originalTime=markerList.get(i).getETO();
            markerList.get(i).setETO(originalTime+newTime);
        }
    }


}
