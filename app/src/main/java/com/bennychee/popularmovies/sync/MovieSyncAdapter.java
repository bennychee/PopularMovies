package com.bennychee.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.MainActivity;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.Utility;
import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.popmovies.PopMovieModel;
import com.bennychee.popularmovies.api.models.popmovies.PopMovieResult;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.review.Result;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;
import com.bennychee.popularmovies.event.SyncStartEvent;
import com.bennychee.popularmovies.event.SyncStopEvent;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 60 * 10; // 10 hours
    private static final int MOVIE_NOTIFICATION_ID = 1001;
    private static long lastSyncTime = 0L;

    public MovieService service;
    final String apiKey = BuildConfig.MOVIE_DB_API_TOKEN;

    int reviewCount = 0;
    int trailerCount = 0;
    int runtimeCount = 0;
    static final int RETRY_COUNT = 5;
    static final int SLEEP_TIME = 10000;
    static final int RETRY_TIME = 30000;

    ProgressDialog progressDialog;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called " + syncResult.stats.numEntries);

        if (Utility.isOneDayLater(lastSyncTime) && initialSync()) {

            String sortOrder = Utility.getPreferredSortOrder(getContext());
            Log.d(LOG_TAG, "Sort Order: " + sortOrder);

            String baseUrl = BuildConfig.API_BASE_URL;

            Log.d(LOG_TAG, "Base URL = " + baseUrl);
            Log.d(LOG_TAG, "API Key = " + apiKey);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(MovieService.class);

            Call<PopMovieModel> popMovieModelCall = service.getPopMovies(apiKey, sortOrder);
            popMovieModelCall.enqueue(new Callback<PopMovieModel>() {
                @Override
                public void onResponse(Response<PopMovieModel> response) {
                    Log.d(LOG_TAG, "Response Status: " + response.code());
                    if (!response.isSuccess()) {
                        Log.d(LOG_TAG, "Unsuccessful Call for Pop Movie Model" + response.errorBody().toString());
                    } else {
                        List<PopMovieResult> movieResultList = response.body().getResults();
                        Utility.storeMovieList(getContext(), movieResultList);

                        int size = movieResultList.size();
                        int count = 0;
                        for (final PopMovieResult movie : movieResultList) {
                            if (count == size) {}
                            count++;
                            Handler reviewHandler = new Handler();
                            Runnable rvr = new Runnable() {
                                @Override
                                public void run() {
                                    MovieReview(getContext(), movie.getId(), apiKey, service);
                                }
                            };
                            reviewHandler.postDelayed(rvr, SLEEP_TIME);


                            MovieReview(getContext(), movie.getId(), apiKey, service);

                            Handler trailerHandler = new Handler();
                            Runnable tr = new Runnable() {
                                @Override
                                public void run() {
                                    MovieTrailers(getContext(), movie.getId(), apiKey, service);
                                }
                            };
                            trailerHandler.postDelayed(tr, SLEEP_TIME);

                            Handler runtimeHandler = new Handler();
                            Runnable rr = new Runnable() {
                                @Override
                                public void run() {
                                    MovieRuntime(getContext(), movie.getId(), apiKey, service);
                                }
                            };
                            runtimeHandler.postDelayed(rr, SLEEP_TIME);
                        }

/*
                        EventBus.getDefault().post(new SyncStopEvent(true));
                        Log.d(LOG_TAG, "Sync Stop EventBus posted");
*/
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(LOG_TAG, "Movie Error: " + t.getMessage());
                }
            });

            notifyMovie();
        }
    }

    private void MovieRuntime(final Context context, final int movieId, final String apiKey, final MovieService service) {
        Call<MovieRuntime> movieRuntimeCall = service.getMovieRuntime(movieId, apiKey);
        movieRuntimeCall.enqueue(new Callback<MovieRuntime>() {
            @Override
            public void onResponse(Response<MovieRuntime> response) {
                Log.d(LOG_TAG, "Movie Runtime Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Runtime " + movieId + " Response: " + response.errorBody().toString());

                    if (runtimeCount < RETRY_COUNT) {
                        //Retry 3 times
                        Handler rHandler = new Handler();
                        Runnable rvr = new Runnable() {
                            @Override
                            public void run() {
                                MovieRuntime(context, movieId, apiKey, service);                            }
                        };
                        rHandler.postDelayed(rvr, RETRY_TIME);

                        Log.d(LOG_TAG, "Retry Retrofit service #" + runtimeCount);
                        //MovieRuntime(context, movieId, apiKey, service);
                        runtimeCount++;
                    }

                } else {
                    int runtime = response.body().getRuntime();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Runtime: " + runtime);
                    Utility.updateMovieWithRuntime(context, movieId, runtime);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Runtime Error: " + t.getMessage());
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
                        //Retry RETRY_COUNT times

                        Handler rHandler = new Handler();
                        Runnable rvr = new Runnable() {
                            @Override
                            public void run() {
                                MovieTrailers(context, movieId, apiKey, service);
                            }
                        };
                        rHandler.postDelayed(rvr, RETRY_TIME);



                        Log.d(LOG_TAG, "Retry Retrofit service #" + trailerCount);
//                        MovieTrailers(context, movieId, apiKey, service);
                        trailerCount++;
                    }
                } else {
                    trailerCount = 0;
                    List<com.bennychee.popularmovies.api.models.trailers.Result> trailersResultList = response.body().getResults();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Trailers Added: " + trailersResultList.size());
                    Utility.storeTrailerList(context, movieId, trailersResultList);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Trailer Error: " + t.getMessage());
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

                        Handler rHandler = new Handler();
                        Runnable rvr = new Runnable() {
                            @Override
                            public void run() {
                                MovieReview(context, movieId, apiKey, service);
                            }
                        };
                        rHandler.postDelayed(rvr, RETRY_TIME);

                        Log.d(LOG_TAG, "Retry Retrofit service #" + reviewCount);
//                        MovieReview(context, movieId, apiKey, service);
                        reviewCount++;
                    }
                } else {
                    reviewCount = 0;
                    List<Result> reviewResultList = response.body().getResults();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Reviews Added: " + reviewResultList.size());
                    Utility.storeCommentList(context, movieId, reviewResultList);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Review Error: " + t.getMessage());
            }
        });
    }

    private boolean initialSync () {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = context.getSharedPreferences("isInitialSync", 0);
        SharedPreferences.Editor editor = prefs.edit();

        boolean syncOrNot = prefs.getBoolean("firstSync", true);
        Log.d(LOG_TAG, "First Sync = " + syncOrNot);

        if (syncOrNot) {
            editor.putBoolean("firstSync", false);
        } else {
            editor.putBoolean("firstSync", true);
        }

        editor.commit();
        return syncOrNot;
    }


    private void notifyMovie() {

        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (displayNotifications) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            lastSyncTime = prefs.getLong(lastNotificationKey, 0);

            if (Utility.isOneDayLater(lastSyncTime)) {

                int iconId = R.mipmap.ic_launcher;
                Bitmap largeIcon = BitmapFactory.decodeResource(
                        getContext().getResources(),
                        R.mipmap.ic_launcher);

                String title = context.getString(R.string.app_name);
                String contentText = context.getString(R.string.notification_content);

                //build your notification here.
                // NotificationCompatBuilder is a very convenient way to build backward-compatible
                // notifications.  Just throw in some data.
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(iconId)
                                .setLargeIcon(largeIcon)
                                .setContentTitle(title)
                                .setContentText(contentText);

                // Make something interesting happen when the user clicks on the notification.
                // In this case, opening the app is sufficient.
                Intent resultIntent = new Intent(context, MainActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                mNotificationManager.notify(MOVIE_NOTIFICATION_ID, mBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.apply();
            }
        }
    }


    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(
                getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            // schedule the sync adapter
            ContentResolver.addPeriodicSync(newAccount,
                    context.getString(R.string.content_authority),
                    Bundle.EMPTY,
                    SYNC_INTERVAL);

            ContentResolver.setSyncAutomatically(newAccount,
                    context.getString(R.string.content_authority),
                    true);

            syncImmediately(context);

        }
        return newAccount;
    }
}