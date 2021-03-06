package com.bennychee.popularmovies;

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
    protected void onCreate(final Bundle savedInstanceState) {
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

        navigationSpinner = new Spinner(getSupportActionBar().getThemedContext());
        navigationSpinner.setAdapter(spinnerAdapter);
        toolbar.addView(navigationSpinner, 0);

        if (savedInstanceState != null) {
            navigationSpinner.setSelection(savedInstanceState.getInt("NavigationSpinner", 0));
        }

            navigationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    MainActivityFragment mainActivityFragment;
                    HighRatedActivityFragment highRatedActivityFragment;
                    FavActivityFragment favActivityFragment;

                    switch (position) {
                        case 0:
                            Log.d(LOG_TAG, "Popular Movies Selected from Spinner");
                            mainActivityFragment = new MainActivityFragment();
                            if (savedInstanceState == null) {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_movies, mainActivityFragment, MAINFRAGMENT_TAG)
                                        .commit();
                            } else {
                                mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentByTag(MAINFRAGMENT_TAG);
                                if (mainActivityFragment == null) {
                                    mainActivityFragment = new MainActivityFragment();
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_movies, mainActivityFragment, MAINFRAGMENT_TAG)
                                            .commit();
                                }
                            }
                            break;
                        case 1:

                            Log.d(LOG_TAG, "High Rated Selected from Spinner");
                            if (savedInstanceState == null) {
                                highRatedActivityFragment = new HighRatedActivityFragment();
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_movies, highRatedActivityFragment, HIGHRATEDFRAGMENT_TAG)
                                        .commit();
                            } else {
                                highRatedActivityFragment = (HighRatedActivityFragment) getSupportFragmentManager().findFragmentByTag(HIGHRATEDFRAGMENT_TAG);
                                if (highRatedActivityFragment == null) {
                                    highRatedActivityFragment = new HighRatedActivityFragment();
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_movies, highRatedActivityFragment, HIGHRATEDFRAGMENT_TAG)
                                            .commit();
                                }
                            }
                            break;
                        case 2:
                            Log.d(LOG_TAG, "Favorites Selected from Spinner");
                            if (savedInstanceState == null) {
                                favActivityFragment = new FavActivityFragment();
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_movies, favActivityFragment, FAVFRAGMENT_TAG)
                                        .commit();
                            } else {
                                favActivityFragment = (FavActivityFragment) getSupportFragmentManager().findFragmentByTag(FAVFRAGMENT_TAG);
                                if (favActivityFragment == null) {
                                    favActivityFragment = new FavActivityFragment();
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_movies, favActivityFragment, FAVFRAGMENT_TAG)
                                            .commit();
                                }
                            }
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
                      } else
                            getSupportFragmentManager().findFragmentByTag(POPMOVIEFRAGMENT_TAG);
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
        Log.d(LOG_TAG, this.toString() + " Inside onResume");
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
