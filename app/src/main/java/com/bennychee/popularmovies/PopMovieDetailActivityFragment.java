package com.bennychee.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bennychee.popularmovies.fragment.MovieDetailsFragment;
import com.bennychee.popularmovies.fragment.MovieReviewFragment;
import com.bennychee.popularmovies.fragment.MovieTrailerFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class PopMovieDetailActivityFragment extends Fragment {

    private static final String LOG_TAG = PopMovieDetailActivityFragment.class.getSimpleName();

    MovieDetailsFragment movieDetailsFragment;
    MovieReviewFragment movieReviewFragment;
    MovieTrailerFragment movieTrailerFragment;

    static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public PopMovieDetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, LOG_TAG);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(PopMovieDetailActivityFragment.DETAIL_URI);
        }

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailsFragment.DETAIL_URI, mUri);
            movieDetailsFragment = new MovieDetailsFragment();
            movieDetailsFragment.setArguments(args);

            args.putParcelable(MovieTrailerFragment.DETAIL_URI, mUri);
            movieTrailerFragment = new MovieTrailerFragment();
            movieTrailerFragment.setArguments(args);

            args.putParcelable(MovieReviewFragment.DETAIL_URI, mUri);
            movieReviewFragment = new MovieReviewFragment();
            movieReviewFragment.setArguments(args);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.tab_layout, container, false);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Details"));
        tabLayout.addTab(tabLayout.newTab().setText("Trailers"));
        tabLayout.addTab(tabLayout.newTab().setText("Reviews"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);

        if(mUri == Uri.EMPTY) {
            tabLayout.setVisibility(View.INVISIBLE);
            viewPager.setVisibility(View.INVISIBLE);
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
        }

        viewPager.setOffscreenPageLimit(tabLayout.getTabCount());

        MovieViewPagerAdapter movieViewPagerAdapter = new MovieViewPagerAdapter(this.getFragmentManager(), tabLayout.getTabCount());
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

        return rootView;
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }        shareIntent.setType("text/plain");

        if (mUri !=null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, Utility.fetchYoutubeKeyUrlFromUri(getContext(), mUri));
        } else {
            Log.d(LOG_TAG, "URI is null");

        }
        return shareIntent;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mUri != null) {
            Log.d(LOG_TAG, "Saving State for URI = " + mUri.toString());
            outState.putString("URI", mUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey("URI")) {
            mUri = Uri.parse(savedInstanceState.getString("URI"));
            Log.d(LOG_TAG, "Saved State URI = " + mUri.toString());
        }

        if (mUri != null) {
            movieId = Utility.fetchMovieIdFromUri(getContext(), mUri);
            Log.d(LOG_TAG, "URI: " + mUri.toString());
            Log.d(LOG_TAG, "Movie ID = " + movieId);
        }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.popmoviedetailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider !=null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "mShareActionProvider is null");
        }
    }
}