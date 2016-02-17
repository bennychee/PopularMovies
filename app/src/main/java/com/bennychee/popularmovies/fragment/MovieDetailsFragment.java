package com.bennychee.popularmovies.fragment;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.Utility;
import com.bennychee.popularmovies.data.MovieContract.MovieEntry;
import com.squareup.picasso.Picasso;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private TextView mDescription;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolbar;
    private ImageView posterImageView;
    private ImageView backdropImageView;
    private TextView mMovieRuntime;
    private TextView mMovieYear;
    private TextView mMovieRating;
    private TextView mVotes;

    private Toolbar backToolbar;

    private FloatingActionButton favButton;


    private static final int MOVIE_DETAIL_LOADER = 0;

    public static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private int count = 0;

    private ProgressDialog progressBar;


    private static final String[] MOVIE_DETAIL_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_DESCRIPTION,
            MovieEntry.COLUMN_IMAGE_URL,
            MovieEntry.COLUMN_BACKDROP_IMAGE_URL,
            MovieEntry.COLUMN_POPULARITY,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_FAVORITE
    };

    public MovieDetailsFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, LOG_TAG + "onActivityCreated");
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, LOG_TAG);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(MovieDetailsFragment.DETAIL_URI);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        mToolbar        = (CollapsingToolbarLayout) rootView.findViewById(R.id.details_toolbar_name);
        mTitleContainer = (LinearLayout) rootView.findViewById(R.id.main_linearlayout_title);
        mTitle          = (TextView) rootView.findViewById(R.id.main_textview_title);

        mDescription = (TextView) rootView.findViewById(R.id.movie_desc);

        posterImageView = (ImageView) rootView.findViewById(R.id.detail_poster_image);
        backdropImageView = (ImageView) rootView.findViewById(R.id.detail_backdrop_image);

        mMovieRating = (TextView) rootView.findViewById(R.id.movie_rating);
        mMovieRuntime = (TextView) rootView.findViewById(R.id.movie_runtime);
        mMovieYear = (TextView) rootView.findViewById(R.id.movie_year);
        mVotes = (TextView) rootView.findViewById(R.id.movie_votes);

        backToolbar = (Toolbar) rootView.findViewById(R.id.flexible_example_toolbar);
        favButton = (FloatingActionButton) rootView.findViewById(R.id.movie_favorite);

        if (getResources().getBoolean(R.bool.dual_pane)) {
            backToolbar.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            if (id == MOVIE_DETAIL_LOADER) {
                Log.d(LOG_TAG, "Movie Details Loader Created");
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        MOVIE_DETAIL_COLUMNS,
                        null,
                        null,
                        null
                );
            }
        } else {
            Log.d(LOG_TAG, "Null onCreateLoader");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case MOVIE_DETAIL_LOADER:
                if (data != null && data.moveToFirst()) {
                    Log.d(LOG_TAG, "Inside onLoadFinished - Movie Details Adapter");
                    LoadMovieDetailView(data);
                }
                break;
        }
    }

    private void LoadMovieDetailView(Cursor data) {
            Log.d(LOG_TAG, "Inside LoadMovieDetailView");
            final int _ID = data.getInt(data.getColumnIndex(MovieEntry._ID));
            String title = data.getString(data.getColumnIndex(MovieEntry.COLUMN_TITLE));
            Log.d(LOG_TAG, "Title: " + title);
            mToolbar.setTitle(title);

            String desc = data.getString(data.getColumnIndex(MovieEntry.COLUMN_DESCRIPTION));
            mDescription.setText(desc);

            String moviePoster = data.getString(data.getColumnIndex(MovieEntry.COLUMN_IMAGE_URL));
            String backdropPoster = data.getString(data.getColumnIndex(MovieEntry.COLUMN_BACKDROP_IMAGE_URL));

            final int isFavMovie = data.getInt(data.getColumnIndex(MovieEntry.COLUMN_FAVORITE));

            if (isFavMovie == 1) {
                //Change color of fav icon to like
                favButton.setImageResource(R.drawable.ic_favorite);
            } else {
                favButton.setImageResource(R.drawable.ic_favorite_not);
            }

            Uri imageUri = Uri.parse(BuildConfig.IMAGE_BASE_URL).buildUpon()
                    .appendPath(getActivity().getString(R.string.image_size_medium))
                    .appendPath(moviePoster.substring(1))
                    .build();

            Uri backdropUri = Uri.parse(BuildConfig.IMAGE_BASE_URL).buildUpon()
                    .appendPath(getActivity().getString(R.string.image_size_large))
                    .appendPath(backdropPoster.substring(1))
                    .build();

            Picasso.with(getActivity())
                    .load(imageUri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .tag(getActivity())
                    .into(posterImageView);

            Picasso.with(getActivity())
                    .load(backdropUri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .tag(getActivity())
                    .into(backdropImageView);

            mMovieYear.setText(Utility.getReleaseYear(data.getString(data.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE))));
            mMovieRuntime.setText(getActivity().getString(R.string.runtime_mins, data.getString(data.getColumnIndex(MovieEntry.COLUMN_RUNTIME))));
            mMovieRating.setText(getActivity().getString(R.string.ratings_ten, data.getString(data.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE))));
            mVotes.setText(getActivity().getString(R.string.votes, data.getString(data.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT))));

            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(isFavMovie) {
                        case 0: {
                            ContentValues addFav  = new ContentValues();
                            addFav.put(MovieEntry.COLUMN_FAVORITE, 1);

                            int updateFavRow = getActivity().getContentResolver().update(
                              MovieEntry.CONTENT_URI,
                                    addFav,
                                    MovieEntry._ID + "= ?",
                                    new String[]{String.valueOf(_ID)}
                            );

                            if (updateFavRow <= 0) {
                                Log.d(LOG_TAG, "Movie not marked as favorite");
                            } else {
                                Log.d(LOG_TAG, "Movie marked as favorite");
                            }
                        }
                        break;

                        case 1: {
                            ContentValues rmFav  = new ContentValues();
                            rmFav.put(MovieEntry.COLUMN_FAVORITE, 0);

                            int updateFavRow = getActivity().getContentResolver().update(
                                    MovieEntry.CONTENT_URI,
                                    rmFav,
                                    MovieEntry._ID + "= ?",
                                    new String[]{String.valueOf(_ID)}
                            );

                            if (updateFavRow < 0) {
                                Log.d(LOG_TAG, "Movie not removed as favorite");
                            } else {
                                Log.d(LOG_TAG, "Movie removed as favorite");
                            }
                        }
                        break;
                    }
                }
            });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case MOVIE_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoaderReset - Movie Details Adapter");
//                loader.abandon();
                loader.reset();
                break;
        }
    }
}
