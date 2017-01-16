package com.example.tbrams.markerdemo;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class AlertDialogFragment extends DialogFragment {
    private final String TAG = "TBR:ADF";
    private SimpleDialogListener mHost;

    public interface SimpleDialogListener {
        public void onPositiveResult(DialogFragment dlg);
        public void onNegativeResult(DialogFragment dlg);
        public void onNeutralResult(DialogFragment dlg);
    }

    public static AlertDialogFragment newInstance(String title, int icon, String pos, String neg) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("icon", icon);
        args.putString("pos", pos);
        args.putString("neg", neg);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String title = getArguments().getString("title");
        int icon     = getArguments().getInt("icon");
        String pos = getArguments().getString("pos");
        String neg = getArguments().getString("neg");

        builder.setTitle(title);
        builder.setIcon(icon);
        builder.setPositiveButton(pos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "Positive button clicked");
                mHost.onPositiveResult(AlertDialogFragment.this);
            }
        });
        builder.setNegativeButton(neg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "Negative button clicked");
                mHost.onNegativeResult(AlertDialogFragment.this);
            }
        });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dlg) {
        super.onCancel(dlg);
        Log.i(TAG, "Dialog cancelled");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mHost = (SimpleDialogListener) getActivity();
    }

}
