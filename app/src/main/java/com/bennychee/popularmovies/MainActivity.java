package com.bennychee.popularmovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.bennychee.popularmovies.sync.MovieSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback, FavActivityFragment.Callback, HighRatedActivityFragment.Callback{

    private boolean mTwoPane;
    private static final String POPMOVIEFRAGMENT_TAG = "PMTAG";
    private static final String MAINFRAGMENT_TAG = "MFTAG";
    private static final String HIGHRATEDFRAGMENT_TAG = "HRFTAG";
    private static final String FAVFRAGMENT_TAG = "FFTAG";

    private String LOG_TAG = MainActivity.class.getSimpleName();

    private Toolbar toolbar = null;
    private String[] category = null;

    private Spinner navigationSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, LOG_TAG);

        category = getResources().getStringArray(R.array.category);

        toolbar = (Toolbar) findViewById(R.id.spin_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(
                getApplicationContext(),
                R.array.category,
                R.layout.spinner_dropdown_item
        );

        Log.d(LOG_TAG, "App theme = " + getResources().getResourceEntryName(getApplicationInfo().theme));

        navigationSpinner = new Spinner(getSupportActionBar().getThemedContext());
        navigationSpinner.setAdapter(spinnerAdapter);
        toolbar.addView(navigationSpinner, 0);

        if (savedInstanceState != null) {
            navigationSpinner.setSelection(savedInstanceState.getInt("NavigationSpinner", 0));
        }

            navigationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            Log.d(LOG_TAG, "Popular Movies Selected from Spinner");
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_movies, new MainActivityFragment(), MAINFRAGMENT_TAG)
                                    .commit();
                            break;
                        case 1:
                            Log.d(LOG_TAG, "High Rated Selected from Spinner");
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_movies, new HighRatedActivityFragment(), HIGHRATEDFRAGMENT_TAG)
                                    .commit();
                            break;
                        case 2:
                            Log.d(LOG_TAG, "Favorites Selected from Spinner");
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_movies, new FavActivityFragment(), FAVFRAGMENT_TAG)
                                    .commit();
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
                        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, new PopMovieDetailActivityFragment(), POPMOVIEFRAGMENT_TAG)
                    .commit();
                      }
        } else {
            mTwoPane = false;
        }

        MovieSyncAdapter.initializeSyncAdapter(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("NavigationSpinner", navigationSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    .commitAllowingStateLoss();
        } else {
            Intent intent = new Intent(this, PopMovieDetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

    @Override
    public void onFavItemSelected(Uri contentUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(PopMovieDetailActivityFragment.DETAIL_URI, contentUri);

            PopMovieDetailActivityFragment fragment = new PopMovieDetailActivityFragment();
            fragment.setArguments(args);

            Log.d(LOG_TAG, "Inside Callback - onFavItemSelected");

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, POPMOVIEFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, PopMovieDetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

    @Override
    public void onHighRatedItemSelected(Uri contentUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(PopMovieDetailActivityFragment.DETAIL_URI, contentUri);

            PopMovieDetailActivityFragment fragment = new PopMovieDetailActivityFragment();
            fragment.setArguments(args);

            Log.d(LOG_TAG, "Inside Callback - onHighRatedItemSelected");

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
