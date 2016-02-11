package com.bennychee.popularmovies;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.bennychee.popularmovies.sync.MovieSyncAdapter;

public class MainActivity extends AppCompatActivity {

    private boolean mTwoPane;
    private static final String POPMOVIEFRAGMENT_TAG = "PMTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
*/
        if (findViewById(R.id.tab_layout) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.tab_layout, new PopMovieDetailActivityFragment(), POPMOVIEFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        MovieSyncAdapter.initializeSyncAdapter(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PopMovieDetailActivityFragment popMovieDetailActivityFragment = (PopMovieDetailActivityFragment)getSupportFragmentManager().findFragmentByTag(POPMOVIEFRAGMENT_TAG);
    }
}
