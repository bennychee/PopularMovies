package com.bennychee.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bennychee.popularmovies.adapters.ReviewAdapter;
import com.bennychee.popularmovies.adapters.TrailerAdapter;
import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;
import com.bennychee.popularmovies.api.models.review.Result;
import com.bennychee.popularmovies.data.MovieContract;
import com.bennychee.popularmovies.data.MovieContract.MovieEntry;
import com.bennychee.popularmovies.data.MovieContract.TrailerEntry;
import com.bennychee.popularmovies.data.MovieContract.ReviewEntry;

import com.bennychee.popularmovies.event.ReviewEvent;
import com.bennychee.popularmovies.event.RuntimeEvent;
import com.bennychee.popularmovies.event.TrailerEvent;
import com.bennychee.popularmovies.fragment.LoadMovieRetrofitFragment;
import com.bennychee.popularmovies.fragment.MovieDetailsFragment;
import com.bennychee.popularmovies.fragment.MovieReviewFragment;
import com.bennychee.popularmovies.fragment.MovieTrailerFragment;
import com.commonsware.cwac.merge.MergeAdapter;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PopMovieDetailActivityFragment extends Fragment {

    private static final String LOG_TAG = PopMovieDetailActivityFragment.class.getSimpleName();

    MovieDetailsFragment movieDetailsFragment;
    MovieReviewFragment movieReviewFragment;
    MovieTrailerFragment movieTrailerFragment;
    LoadMovieRetrofitFragment loadMovieRetrofitFragment;

    static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private ListView mListView;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public PopMovieDetailActivityFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

            args.putParcelable(LoadMovieRetrofitFragment.DETAIL_URI, mUri);
            loadMovieRetrofitFragment = new LoadMovieRetrofitFragment();
            loadMovieRetrofitFragment.setArguments(args);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.tab_layout, container, false);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Details"));
        tabLayout.addTab(tabLayout.newTab().setText("Trailers"));
        tabLayout.addTab(tabLayout.newTab().setText("Reviews"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        int movieId = Utility.fetchMovieIdFromUri(getActivity(), mUri);
        loadMovieRetrofitFragment.LoadMovieRetrofit(getContext(), movieId, mUri);
        super.onActivityCreated(savedInstanceState);

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

