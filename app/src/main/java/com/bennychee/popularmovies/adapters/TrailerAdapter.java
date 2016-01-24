package com.bennychee.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.data.MovieContract;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by B on 21/01/2016.
 */
public class TrailerAdapter extends CursorAdapter implements YouTubePlayer.OnInitializedListener {

    public static final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    private String youtubeKey;

    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_trailer, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        Log.d(LOG_TAG, "binding view: " + view.toString());



        TextView trailerNameTextView = (TextView) view.findViewById(R.id.trailer_name);

        String trailerName = cursor.getString(
                cursor.getColumnIndex(
                        MovieContract
                                .TrailerEntry
                                .COLUMN_TITLE)
        );

        trailerNameTextView.setText(trailerName);

        youtubeKey = cursor.getString(
                cursor.getColumnIndex(
                        MovieContract
                            .TrailerEntry
                            .COLUMN_YOUTUBE_KEY)
        );

/*
                Uri youtubeUri = Uri.parse(BuildConfig.YOUTUBE_TRAILER_URL + youtubeKey);
                Log.d(LOG_TAG, "Youtube URL: " + youtubeUri.toString());
*/

        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) view.findViewById(R.id.youtube_player);
        youTubePlayerView.initialize(BuildConfig.YOUTUBE_API_TOKEN, this);
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        /** add listeners to YouTubePlayer instance **/
        youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
        youTubePlayer.setPlaybackEventListener(playbackEventListener);

        /** Start buffering **/
        if (!b) {
            youTubePlayer.cueVideo(youtubeKey);
        }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    private PlaybackEventListener playbackEventListener = new PlaybackEventListener() {

        @Override
        public void onBuffering(boolean arg0) {
        }

        @Override
        public void onPaused() {
        }

        @Override
        public void onPlaying() {
        }

        @Override
        public void onSeekTo(int arg0) {
        }

        @Override
        public void onStopped() {
        }

    };

    private PlayerStateChangeListener playerStateChangeListener = new PlayerStateChangeListener() {

        @Override
        public void onAdStarted() {
        }

        @Override
        public void onError(ErrorReason arg0) {
        }

        @Override
        public void onLoaded(String arg0) {
        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {
        }

        @Override
        public void onVideoStarted() {
        }
    };

}
