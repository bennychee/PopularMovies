package com.bennychee.popularmovies;

import android.content.Context;
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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.review.Result;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;
import com.bennychee.popularmovies.fragment.MovieDetailsFragment;
import com.bennychee.popularmovies.fragment.MovieReviewFragment;
import com.bennychee.popularmovies.fragment.MovieTrailerFragment;

import java.util.List;

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

    private int reviewCount = 0;
    private int runtimeCount = 0;
    private int trailerCount = 0;
    private final int RETRY_COUNT = 5;

    static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private ListView mListView;

    private MovieService service;

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

/*
        Toolbar toolbar;

        if(!(getResources().getBoolean(R.bool.dual_pane))) {
            toolbar = (Toolbar) rootView.findViewById(R.id.pop_movie_toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar = (Toolbar) rootView.findViewById(R.id.pop_movie_toolbar);
            toolbar.setVisibility(View.INVISIBLE);
            toolbar.invalidate();
        }
*/

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

        Log.d(LOG_TAG, "App theme = " + getResources().getResourceEntryName(getContext().getApplicationInfo().theme));

        return rootView;
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }        shareIntent.setType("text/plain");

        Log.d(LOG_TAG, "Inside Share Forecast Intent");

        if (mUri !=null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, Utility.fetchYoutubeKeyUrlFromUri(getContext(), mUri));
        } else {
            Log.d(LOG_TAG, "URI is null");

        }
        return shareIntent;
    }

    @Override
    public void onPause() {
        //EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //EventBus.getDefault().unregister(this);
        super.onDestroy();
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
            int movieId = Utility.fetchMovieIdFromUri(getContext(), mUri);
            Log.d(LOG_TAG, "URI: " + mUri.toString());
            Log.d(LOG_TAG, "Movie ID = " + movieId);

/*
            String apiKey = BuildConfig.MOVIE_DB_API_TOKEN;
            String baseUrl = BuildConfig.API_BASE_URL;
*/

/*
            Log.d(LOG_TAG, "Base URL = " + baseUrl);
            Log.d(LOG_TAG, "API Key = " + apiKey);

*/

/*
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(MovieService.class);
            Log.d(LOG_TAG, "Retrofit Service: Created");
*/

/*
            if (Utility.checkRuntimeFromUri(getContext(), mUri) <= 0) {
                MovieRuntime(getContext(), movieId, apiKey, service);
                Log.d(LOG_TAG, "Movie ID: " + movieId + " Runtime NOT in DB");
            } else {
                Log.d(LOG_TAG, "Movie ID: " + movieId + " Runtime FOUND in DB");
            }
*/

//            if (Utility.checkTrailerFromUri(getContext(), mUri) <= 0) {
//                MovieTrailers(getContext(), movieId, apiKey, service);
            //               Log.d(LOG_TAG, "Movie ID: " + movieId + " Trailer not found in DB");
            //           }

//            if (Utility.checkReviewFromUri(getContext(), mUri) <= 0) {
//                MovieReview(getContext(), movieId, apiKey, service);
//                Log.d(LOG_TAG, "Movie ID: " + movieId + " Review not found in DB");
//            }
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
                    Log.d(LOG_TAG, "Tab - movieDetailsFragment");
                    return movieDetailsFragment;
                case 1:
                    Log.d(LOG_TAG, "Tab - movieTrailerFragment");
                    return movieTrailerFragment;
                case 2:
                    Log.d(LOG_TAG, "Tab - movieReviewFragment");
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
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Inside onResume");
    }

    private void MovieRuntime(final Context context, final int movieId, final String apiKey, final MovieService service) {
        Call<MovieRuntime> movieRuntimeCall = service.getMovieRuntime(movieId, apiKey);
        movieRuntimeCall.enqueue(new Callback<MovieRuntime>() {
            @Override
            public void onResponse(Response<MovieRuntime> response) {
                Log.d(LOG_TAG, "Movie Runtime Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Runtime " + movieId + " Response: " + response.errorBody().toString());
                    if (runtimeCount< RETRY_COUNT) {
                        //Retry 3 times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + runtimeCount);
                        MovieRuntime(context, movieId, apiKey, service);
                        runtimeCount++;
                    }
                } else {
                    int runtime = response.body().getRuntime();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Runtime: " + runtime);
                    Utility.updateMovieWithRuntime(context, movieId, runtime);
/*
                    EventBus.getDefault().post(new RuntimeEvent(true));
                    Log.d(LOG_TAG, "Runtime EventBus posted");
*/
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Runtime Error: " + t.getMessage());
                //EventBus.getDefault().post(new RuntimeEvent(false));
            }
        });
    }

    private void MovieTrailers(final Context context, final int movieId, final String apiKey, final MovieService service) {
        Call<MovieTrailers> movieTrailersCall = service.getMovieTrailer(movieId, apiKey);
        movieTrailersCall.enqueue(new Callback<MovieTrailers>() {
            @Override
            public void onResponse(Response<MovieTrailers> response) {
                Log.d(LOG_TAG, "Movie Trailers Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Trailer " + movieId + " Response: " + response.errorBody().toString());
                    if (trailerCount < RETRY_COUNT) {
                        //Retry 3 times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + trailerCount);
                        MovieTrailers(context, movieId, apiKey, service);
                        trailerCount++;
                    }
                } else {
                    List<com.bennychee.popularmovies.api.models.trailers.Result> trailersResultList = response.body().getResults();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Trailers Added: " + trailersResultList.size());
                    Utility.storeTrailerList(context, movieId, trailersResultList);
                    //EventBus.getDefault().post(new TrailerEvent(true));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Trailer Error: " + t.getMessage());
                //EventBus.getDefault().post(new TrailerEvent(false));
            }
        });
    }

    private void MovieReview (final Context context, final int movieId, final String apiKey, final MovieService service) {
        Call<MovieReviews> movieReviewsCall = service.getMovieReview(movieId, apiKey);
        movieReviewsCall.enqueue(new Callback<MovieReviews>() {
            @Override
            public void onResponse(Response<MovieReviews> response) {
                Log.d(LOG_TAG, "Movie Reviews Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Reviews " + movieId + " Response: " + response.errorBody().toString());
                    if (reviewCount < RETRY_COUNT) {
                        //Retry RETRY_COUNT times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + reviewCount);
                        MovieReview(context, movieId, apiKey, service);
                        reviewCount++;
                    }
                } else {
                    List<Result> reviewResultList = response.body().getResults();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Reviews Added: " + reviewResultList.size());
                    Utility.storeCommentList(context, movieId, reviewResultList);
                    reviewResultList.clear();
                    //EventBus.getDefault().post(new ReviewEvent(true));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Review Error: " + t.getMessage());
                //EventBus.getDefault().post(new ReviewEvent(false));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(LOG_TAG, "Inside onCreateOptionsMenu");
        inflater.inflate(R.menu.popmoviedetailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider !=null) {
            Log.d(LOG_TAG, "mShareActionProvider set Share Intent");
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "mShareActionProvider is null");
        }
    }
}