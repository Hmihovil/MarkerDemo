package com.example.tbrams.markerdemo;


import android.app.Activity;
import android.content.Intent;
import android.opengl.ETC1;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.tbrams.markerdemo.data.GlobalNavValData;
import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;

import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class GlobalNavValFragment extends Fragment {

    MarkerLab markerLab = MarkerLab.getMarkerLab(getActivity());
    List<MarkerObject> markerList = markerLab.getMarkers();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v= inflater.inflate(R.layout.global_nav_val_fragment,container, false);
        final EditText editTextTAS = (EditText) v.findViewById(R.id.editTextTAS);
        EditText editTextALT = (EditText) v.findViewById(R.id.editTextALT);
        EditText editTextWIND = (EditText) v.findViewById(R.id.editTextWIND);

        v.findViewById(R.id.buttonNavValOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double tas = Double.parseDouble(String.valueOf(editTextTAS.getText()));
                double alt = Double.parseDouble(String.valueOf(editTextTAS.getText()));
                String wind = String.valueOf(editTextTAS.getText());

                GlobalNavValData gnd=new GlobalNavValData(alt,tas,wind);

                for (int i=0;i<markerList.size();i++){
                    markerList.get(i).calcGS(gnd.getTAS(),gnd.getWINDfrom(), gnd.getWINDkts());
                    markerList.get(i).calcWCA(gnd.getTAS(),gnd.getWINDfrom(), gnd.getWINDkts());
                    markerList.get(i).calcIAS(gnd.getTAS(), gnd.getALT());
                    markerList.get(i).calcTH();
                    markerList.get(i).calcVAR();
                    markerList.get(i).calcMH();
                    markerList.get(i).calcTIME();
                }

                sendResult(RESULT_OK);

            }
        });

        v.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResult(RESULT_CANCELED);
            }
        });



        return v;
    }


    /*
     * Helper function used to send results back to the main activity where it can
     * be processed in the onActivityResult method
     */
    private void sendResult(int resultCode) {
        Intent intent = new Intent();
        getActivity().setResult(resultCode, intent);
        getActivity().onBackPressed();
    }



}
