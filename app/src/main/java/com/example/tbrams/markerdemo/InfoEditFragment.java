package com.example.tbrams.markerdemo;

import android.support.v4.app.Fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

public class InfoEditFragment extends Fragment {

    private static final String ARG_CRIME_ID = "MARKER_ID";
    private String markerId;

    public static InfoEditFragment newInstance(String markerId) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, markerId);

        InfoEditFragment fragment=new InfoEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.info_window_edit, container, false);

        // Get marker id from argument bundle
        markerId= (String) getArguments().getSerializable(ARG_CRIME_ID);
        Log.d("TBR:","Now in InfoEditFragment - MarkerId: "+markerId);



        Button btnUpdate = (Button) v.findViewById(R.id.buttonUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText eTit = (EditText) v.findViewById(R.id.placeText);
                EditText eSnp = (EditText) v.findViewById(R.id.snippetText);
                String tit = String.valueOf(eTit.getText());
                String snp = String.valueOf(eSnp.getText());
                Log.d("TBR", "Update Btn clicked, tit ="+tit+", snp="+snp);
                MarkerDemoActivity.updateMarker(markerId, tit, snp);

            }
        });

        Button btnDelete = (Button) v.findViewById(R.id.buttonDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TBR", "Delete Btn clicked");
                AlertDialogCreate();

            }
        });


        Button btnCancel = (Button) v.findViewById(R.id.buttonCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TBR", "Cancel Btn clicked");
                getActivity().onBackPressed();

            }
        });

        return v;
    }

    public void AlertDialogCreate(){

        new AlertDialog.Builder(getActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Alert Dialog Box Title")
                .setMessage("Are you sure( Alert Dialog Message )")
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Log.d("TBR","Dialog Request accepted with OK");
                        MarkerDemoActivity.DeleteMarker(markerId);
                        getActivity().onBackPressed();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Log.d("TBR","Dialog Requet declined with Cancel");
                    }
                }).show();
    }
}
