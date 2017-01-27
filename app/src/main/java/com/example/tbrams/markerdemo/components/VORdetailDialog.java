package com.example.tbrams.markerdemo.components;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.tbrams.markerdemo.R;
import com.example.tbrams.markerdemo.data.NavAid;

import java.util.Locale;

public class VORdetailDialog extends DialogFragment {
    public static final String TAG="TBR:";
    private NavAid mVOR;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle("VOR Detail");


        final Morse morse = new Morse(getActivity());

        // Inflate our special dialog layout for the dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.card_vor_spec, null);
        builder.setView(dialogView);

        TextView header=(TextView) dialogView.findViewById(R.id.heading_label);
        TextView frequency=(TextView) dialogView.findViewById(R.id.freq_txt);
        TextView elevation=(TextView) dialogView.findViewById(R.id.elev_txt);
        TextView limitation=(TextView) dialogView.findViewById(R.id.limit_txt);
        TextView morseSignal=(TextView) dialogView.findViewById(R.id.morse_txt);

        header.setText(mVOR.getName());
        frequency.setText(mVOR.getFreq());
        elevation.setText(String.format(Locale.ENGLISH, "%.1f ft", mVOR.getElevation()));

        String limitString = String.format(Locale.ENGLISH, "%d NM/%d ft", mVOR.getMax_range(), mVOR.getMax_alt());
        limitation.setText(limitString);

        morseSignal.setText(morse.getMorseCode(mVOR.getIdent()));
        morseSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                morse.vibrate();
            }
        });


        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // not so much to do here....
                dialog.cancel();
            }
        });

        return builder.create();
    }


    public void activateVOR(NavAid vor) {
        mVOR = vor;
    }

}
