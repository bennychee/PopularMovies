package com.bennychee.popularmovies;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.bennychee.popularmovies.adapters.PopMovieAdapter;
import com.bennychee.popularmovies.sync.MovieSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback, FavActivityFragment.Callback, HighRatedActivityFragment.Callback{

    private boolean mTwoPane;
    private static final String POPMOVIEFRAGMENT_TAG = "PMTAG";
    private static final String MAINFRAGMENT_TAG = "MFTAG";
    private String LOG_TAG = MainActivity.class.getSimpleName();

    private Toolbar toolbar = null;
    private String[] category = null;

    private Spinner navigationSpinner;
//    private SyncReceiver myReceiver;
    private ProgressDialog ringProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*
        myReceiver = new SyncReceiver();
        IntentFilter intentFilter = new IntentFilter("com.bennychee.syncstatus");
        registerReceiver(myReceiver, intentFilter);
*/

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

/*
        ringProgressDialog = new ProgressDialog(this);
*/

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
                                    .replace(R.id.fragment_movies, new HighRatedActivityFragment())
                                    .commit();
                            break;
                        case 2:
                            Log.d(LOG_TAG, "Favorites Selected from Spinner");
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_movies, new FavActivityFragment())
                                    .commit();
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

/*
        ringProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
*/

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

/*
    public class SyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Receiving Broadcast");
            Bundle extras = intent.getExtras();
            String syncStatus;
            if (extras != null) {
                syncStatus = extras.getString("SYNCING_STATUS");
                Log.d(LOG_TAG, "SyncStatus = " + syncStatus);
                //if (syncStatus == "RUNNING") {
                if (syncStatus.equals("RUNNING")) {
                    Log.d(LOG_TAG, "Intent Running");
                    ringProgressDialog.setTitle("Please wait....");
                    ringProgressDialog.setMessage("Loading Movies....");
                    ringProgressDialog.setIndeterminate(true);
                    ringProgressDialog.show();
                } else if (syncStatus.equals("STOPPING")) {
                    Log.d(LOG_TAG, "Intent Stopping");
                    ringProgressDialog.dismiss();
                }
            }
        }
    }

*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("NavigationSpinner", navigationSpinner.getSelectedItemPosition());
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
