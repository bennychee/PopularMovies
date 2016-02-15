package com.bennychee.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bennychee.popularmovies.sync.MovieSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private boolean mTwoPane;
    private static final String POPMOVIEFRAGMENT_TAG = "PMTAG";
    private String LOG_TAG = MainActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, LOG_TAG);

/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
*/

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new PopMovieDetailActivityFragment(), POPMOVIEFRAGMENT_TAG)
//                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
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

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(PopMovieDetailActivityFragment.DETAIL_URI, contentUri);

            PopMovieDetailActivityFragment fragment = new PopMovieDetailActivityFragment();
            fragment.setArguments(args);

            Log.d(LOG_TAG, "Inside Callback - onItemSelected");

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, POPMOVIEFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, PopMovieDetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
