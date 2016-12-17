package com.example.tbrams.markerdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class TalkActivity extends SingleFragmentActivity {
    private static final String EXTRA_MARKER_ID = "com.example.tbrams.markerdemo.marker_id";

    public static Intent newIntent(Context packageContext, int markerIndex) {
        Intent intent = new Intent(packageContext, TalkActivity.class);
        intent.putExtra(EXTRA_MARKER_ID, markerIndex);
        return intent;
    }

    @Override
    protected Fragment createFragment() {

        // Get markerIndex from argument passed to activity and pass it on to Fragment
        // Get markerIndex from arguments
        int markerIndex = (int) getIntent().getSerializableExtra(EXTRA_MARKER_ID);
        Log.d("TBR:","TalkActivity/onCreate - intent/markerIndex: "+markerIndex);

        return TalkFragment.newInstance(markerIndex);
    }
}
