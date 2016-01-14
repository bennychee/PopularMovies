package com.bennychee.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 60 * 10; // 10 hours
    private static final int MOVIE_NOTIFICATION_ID = 1001;
    private static long lastSyncTime = 0L;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        if (Utility.isOneDayLater(lastSyncTime)) {
            String sortOrder = Utility.getPreferredSortOrder(getContext());

            String baseUrl = "http://api.themoviedb.org/3";
            final String apiKey = BuildConfig.MOVIE_DB_API_TOKEN;

            Log.d(LOG_TAG, apiKey);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .build();

            final MovieService service = retrofit.create(MovieService.class);

            Call<PopMovieModel> popMovieModelCall = service.getPopMovies(apiKey, sortOrder);

            popMovieModelCall.enqueue(new Callback<PopMovieModel>() {
                                          @Override
                                          public void onResponse(Response<PopMovieModel> response) {
                                              //Get result from response.body()

                                              List<PopMovieResult> movieResultList = response.body().getResults();
                                              Utility.storeMovieList(getContext(), movieResultList);

                                            for (final PopMovieResult movie : movieResultList) {

                                                Call<MovieRuntime> movieRuntimeCall = service.getMovieRuntime(apiKey, movie.getId());
                                                movieRuntimeCall.enqueue(new Callback<MovieRuntime>() {
                                                    @Override
                                                    public void onResponse(Response<MovieRuntime> response) {
                                                        int runtime = response.body().getRuntime();
                                                        Utility.updateMovieWithRuntime(getContext(), movie.getId(), runtime);
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable t) {

                                                    }
                                                });

                                                Call<MovieReviews> movieReviewsCall = service.getMovieReview(apiKey, movie.getId());
                                                movieReviewsCall.enqueue(new Callback<MovieReviews>() {
                                                    @Override
                                                    public void onResponse(Response<MovieReviews> response) {
                                                        List<Result> reviewResultList = response.body().getResults();
                                                        Utility.storeCommentList(getContext(),movie.getId(),reviewResultList);
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable t) {

                                                    }
                                                });

                                                Call<MovieTrailers> movieTrailersCall = service.getMovieTrailer(apiKey, movie.getId());
                                                movieTrailersCall.enqueue(new Callback<MovieTrailers>() {
                                                    @Override
                                                    public void onResponse(Response<MovieTrailers> response) {
                                                        List<com.bennychee.popularmovies.api.models.trailers.Result> trailersResultList = response.body().getResults();
                                                        Utility.storeTrailerList(getContext(),movie.getId(),trailersResultList);
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable t) {

                                                    }
                                                });
                                            }

                                            notifyMovie();

                                          }

                                          @Override
                                          public void onFailure(Throwable t) {

                                          }
                                      }
            );

        }
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