package com.example.tbrams.markerdemo;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
        final EditText editTextALT = (EditText) v.findViewById(R.id.editTextALT);
        final EditText editTextWIND = (EditText) v.findViewById(R.id.editTextWIND);

        // Make sure the keyboard is committing on the last field instead of moving to next
        editTextWIND.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Initialize fields with whatever global values we can find in first marker
        MarkerObject mo=markerList.get(0);
        if (mo.getTAS()!=0) {
            editTextTAS.setText(String.format("%.1f", mo.getTAS()));
            editTextALT.setText(String.format("%.1f", mo.getALT()));
            editTextWIND.setText(String.format("%.0f", mo.getWindDirection()) + "/" + String.format("%.0f", mo.getWindStrenght()));
        }

        v.findViewById(R.id.buttonNavValOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!String.valueOf(editTextTAS.getText()).matches("\\d+([.]\\d*)?")) {
                    editTextTAS.setError("You need to provide True Airspeed here");
                    return;
                }
                if (!String.valueOf(editTextALT.getText()).matches("\\d+([.]\\d*)?")) {
                    editTextALT.setError("You need to provide an altitude here");
                    return;
                }

                if (!String.valueOf(editTextWIND.getText()).matches("\\d{3}/\\d{2}")) {
                    editTextWIND.setError("You must provide wind info here for example 090/15");
                    return;
                }



                double tas = Double.parseDouble(String.valueOf(editTextTAS.getText()));
                double alt = Double.parseDouble(String.valueOf(editTextALT.getText()));
                String wind = String.valueOf(editTextWIND.getText());

                GlobalNavValData gnd=new GlobalNavValData(alt,tas,wind);
                double eto=0;
                for (int i=1;i<markerList.size();i++){
                    markerList.get(i).calcGS(gnd.getTAS(),gnd.getWINDfrom(), gnd.getWINDkts());
                    markerList.get(i).calcWCA(gnd.getTAS(),gnd.getWINDfrom(), gnd.getWINDkts());
                    markerList.get(i).calcIAS(gnd.getTAS(), gnd.getALT());
                    markerList.get(i).calcTH();
                    markerList.get(i).calcVAR();
                    markerList.get(i).calcMH();
                    markerList.get(i).calcTIME();
                    // calculate the accumulated ETO time and update each marker accordingly
                    eto+=markerList.get(i).getETO();
                    markerList.get(i).setETO(eto);
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


    @Override
    public void onPause() {
        Log.d("TBR:", "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("TBR:","onStop");
        super.onStop();
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
