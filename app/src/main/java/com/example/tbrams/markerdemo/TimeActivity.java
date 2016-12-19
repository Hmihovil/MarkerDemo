package com.example.tbrams.markerdemo;


import android.support.v4.app.Fragment;
import android.util.Log;

public class TimeActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {

        Log.d("TBR:","TimeActivity/onCreate");
        return new TimeFragment();
    }
}
