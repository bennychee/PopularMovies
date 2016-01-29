package com.bennychee.popularmovies;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import com.bennychee.popularmovies.fragment.MovieDetailsFragment;
import com.bennychee.popularmovies.fragment.MovieReviewFragment;
import com.bennychee.popularmovies.fragment.MovieTrailerFragment;


public class PopMovieDetailActivity extends AppCompatActivity {


    MovieDetailsFragment movieDetailsFragment;
    MovieReviewFragment movieReviewFragment;
    MovieTrailerFragment movieTrailerFragment;

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

/*
            PopMovieDetailActivityFragment fragment = new PopMovieDetailActivityFragment();
            fragment.setArguments(arguments);
*/

            movieDetailsFragment = new MovieDetailsFragment();
            movieDetailsFragment.setArguments(arguments);

            movieReviewFragment = new MovieReviewFragment();
            movieReviewFragment.setArguments(arguments);

            movieTrailerFragment = new MovieTrailerFragment();
            movieTrailerFragment.setArguments(arguments);

/*
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_details, movieDetailsFragment)
                    .commit();
*/

        }

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

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

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
/*                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_details, movieDetailsFragment)
                            .commit();*/
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

