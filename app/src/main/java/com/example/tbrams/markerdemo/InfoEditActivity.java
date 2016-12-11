package com.example.tbrams.markerdemo;

import android.support.v4.app.Fragment;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

public class InfoEditActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return InfoEditFragment.newInstance(MarkerDemoActivity.currentMarkerId);
    }
}