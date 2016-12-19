package com.example.tbrams.markerdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class TalkActivity extends SingleFragmentActivity {
    public static final String EXTRA_SEGMENT_ID = "com.example.tbrams.markerdemo.segment_id";

    public static Intent newIntent(Context packageContext, int markerIndex) {
        Intent intent = new Intent(packageContext, TalkActivity.class);
        intent.putExtra(EXTRA_SEGMENT_ID, markerIndex);
        return intent;
    }

    @Override
    protected Fragment createFragment() {

        // Get segmentIndex from argument passed to activity and pass it on to Fragment
        int segmentIndex = (int) getIntent().getSerializableExtra(EXTRA_SEGMENT_ID);
        Log.d("TBR:","TalkActivity/onCreate - intent/segmentIndex: "+segmentIndex);

        return TalkFragment.newInstance(segmentIndex);
    }
}
