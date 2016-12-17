package com.example.tbrams.markerdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;


public class ETOActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ETOFragment();
    }
}
