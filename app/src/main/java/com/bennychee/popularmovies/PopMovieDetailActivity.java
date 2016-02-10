package com.bennychee.popularmovies;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.bennychee.popularmovies.event.ReviewEvent;
import com.bennychee.popularmovies.event.TrailerEvent;
import com.bennychee.popularmovies.fragment.MovieDetailsFragment;
import com.bennychee.popularmovies.fragment.MovieReviewFragment;
import com.bennychee.popularmovies.fragment.MovieTrailerFragment;
import com.bennychee.popularmovies.fragment.LoadMovieRetrofitFragment;

public class PopMovieDetailActivity extends AppCompatActivity {

    MovieDetailsFragment movieDetailsFragment;
    MovieReviewFragment movieReviewFragment;
    MovieTrailerFragment movieTrailerFragment;
    LoadMovieRetrofitFragment loadMovieRetrofitFragment;

    public final String LOG_TAG = PopMovieDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pop_movie_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailsFragment.DETAIL_URI, getIntent().getData());
            arguments.putParcelable(MovieTrailerFragment.DETAIL_URI, getIntent().getData());
            arguments.putParcelable(MovieReviewFragment.DETAIL_URI, getIntent().getData());
            arguments.putParcelable(LoadMovieRetrofitFragment.DETAIL_URI, getIntent().getData());

            movieDetailsFragment = new MovieDetailsFragment();
            movieDetailsFragment.setArguments(arguments);

            movieReviewFragment = new MovieReviewFragment();
            movieReviewFragment.setArguments(arguments);

            movieTrailerFragment = new MovieTrailerFragment();
            movieTrailerFragment.setArguments(arguments);

            loadMovieRetrofitFragment = new LoadMovieRetrofitFragment();
            loadMovieRetrofitFragment.setArguments(arguments);
        }

        int movieId = Utility.fetchMovieIdFromUri(this, getIntent().getData());
        loadMovieRetrofitFragment.LoadMovieRetrofit(getApplicationContext(), movieId);

        setContentView(R.layout.tab_layout);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Details"));
        tabLayout.addTab(tabLayout.newTab().setText("Trailers"));
        tabLayout.addTab(tabLayout.newTab().setText("Reviews"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        MovieViewPagerAdapter movieViewPagerAdapter = new MovieViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(movieViewPagerAdapter);


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override    public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public void onEvent(ReviewEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Event Message - Retrofit done, load the review loader!");
        } else {
            Log.d(LOG_TAG, "Event Message - " + event.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
     //   getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    public class MovieViewPagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        public MovieViewPagerAdapter(FragmentManager fm, int numOfTabs) {
            super(fm);
            this.mNumOfTabs = numOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return movieDetailsFragment;
                case 1:
                    return movieTrailerFragment;
                case 2:
                    return movieReviewFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}