package com.bennychee.popularmovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SplashActivity extends AppCompatActivity{

    private final static String LOG_TAG = SplashActivity.class.getSimpleName();
    private ProgressDialog ringProgressDialog;
    private boolean mLoadFinished = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG, LOG_TAG);

        super.onCreate(savedInstanceState);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}