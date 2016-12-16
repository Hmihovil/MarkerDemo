package com.example.tbrams.markerdemo;


import android.support.v4.app.Fragment;

public class GlobalNavValActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new GlobalNavValFragment();
    }
}
