package com.example.tbrams.markerdemo;


import android.support.v4.app.Fragment;
import android.util.Log;

public class GlobalNavValActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new GlobalNavValFragment();
    }

    @Override
    public void onBackPressed() {
        Log.d("TBR:", "onBackPressed...");
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }
    }
}
