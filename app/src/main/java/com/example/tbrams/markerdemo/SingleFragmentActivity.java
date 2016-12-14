package com.example.tbrams.markerdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.tbrams.markerdemo.R;

/**
 * This Class is used for creating fragment activities the smart way.
 *
 * Make this one the superclass of your new Fragment Actitity class. For example
 * like this
 *
 * public class MyFragmentActivity extends SingleFramentActivity {
 *    ...
 *
 * Make sure you implement the createFragment() function returning a sample of
 * your specialised fragment to be included on this activity. For example:
 *
 * public static MyFragment newInstance() {
 *    MyFragment fragment=new MyFragment();
 *    return fragment;
 * }
 *
 * MyFragment above is the Fragment you want to include in the Activity Fragment
 * called MyFragmentActivity in this example.
 *
 * When you create a new Fragment class like MyFragment, you just need to
 * extend the Fragment class.
 *
 * This Super Class will look for the layout called "activity_fragment" and a
 * placeholder inside the layout called "fragment_container" and will then attach
 * your fragment to the placeholder using the transaction below.
 *
 * The layout is typically designed like this
 *
 * <FrameLayout
 *    xmlns:android="http://schemas.android.com/apk/res/android"
 *    android:layout_height="match_parent"
 *    android:layout_width="match_parent"
 *    android:id="@+id/fragment_container"/>
 *
 */

public abstract class SingleFragmentActivity extends AppCompatActivity{

    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment=fm.findFragmentById(R.id.fragment_container);
        if (fragment==null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container,fragment)
                    .commit();
        }
    }
}