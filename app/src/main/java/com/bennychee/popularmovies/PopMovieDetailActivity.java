package com.bennychee.popularmovies;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

public class PopMovieDetailActivity extends AppCompatActivity {

    public final String LOG_TAG = PopMovieDetailActivity.class.getSimpleName();
    private static final String POPMOVIEFRAGMENT_TAG = "PMTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop_movie_detail);

        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            Uri mUri = getIntent().getData();
            arguments.putParcelable(PopMovieDetailActivityFragment.DETAIL_URI, mUri);
            PopMovieDetailActivityFragment popMovieDetailActivityFragment = new PopMovieDetailActivityFragment();
            popMovieDetailActivityFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, popMovieDetailActivityFragment, POPMOVIEFRAGMENT_TAG)
                    .commit();
        }

        Log.d(LOG_TAG, "App theme = " + getResources().getResourceEntryName(getApplicationInfo().theme));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
     //   getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

}