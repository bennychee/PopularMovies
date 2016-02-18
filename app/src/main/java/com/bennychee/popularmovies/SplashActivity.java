package com.bennychee.popularmovies;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bennychee.popularmovies.adapters.PopMovieAdapter;
import com.bennychee.popularmovies.data.MovieContract;
import com.bennychee.popularmovies.sync.MovieSyncAdapter;

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