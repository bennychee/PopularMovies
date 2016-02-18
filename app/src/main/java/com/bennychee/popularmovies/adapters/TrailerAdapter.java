package com.bennychee.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.YoutubeLightBox;
import com.bennychee.popularmovies.data.MovieContract;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by B on 21/01/2016.
 */
public class TrailerAdapter extends CursorAdapter implements YouTubeThumbnailView.OnInitializedListener {

    public static final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    private String youtubeKey;
    YouTubeThumbnailView youTubeThumbnailView;
    YouTubeThumbnailLoader youTubeThumbnailLoader;
    private boolean isNotInit = true;

    private final Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailLoaderMap;

    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        thumbnailLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(LOG_TAG, LOG_TAG);
        return LayoutInflater.from(context).inflate(R.layout.item_trailer, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        Log.d(LOG_TAG, "binding view: " + view.toString());

        TextView trailerNameTextView = (TextView) view.findViewById(R.id.trailer_name);
//        ImageView imageView = (ImageView) view.findViewById(R.id.youtube_thumbnail);

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
        String youtubeImageUrl = "http://img.youtube.com/vi/";

        Uri imageUri = Uri.parse(youtubeImageUrl).buildUpon()
                .appendPath(youtubeKey)
                .appendPath("default.jpg")
                .build();

        Picasso.with(context)
                .load(imageUri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .tag(context)
                .into(imageView);
*/

        youTubeThumbnailView = new YouTubeThumbnailView(context);
        youTubeThumbnailView = (YouTubeThumbnailView) view.findViewById(R.id.youtube_thumbnail);
        youTubeThumbnailLoader = thumbnailLoaderMap.get(youTubeThumbnailView);

        if (view == null) {
            //Case 1 - We need to initialize the loader
            youTubeThumbnailView.initialize(BuildConfig.YOUTUBE_API_TOKEN, this);
            Log.d(LOG_TAG, "Youtube Thumbnail Initialized - Cursor is First");
            youTubeThumbnailView.setTag(youtubeKey);
            isNotInit = false;
        } else {
            if (youTubeThumbnailLoader == null) {
                // Case 3 - The loader is currently initializing
                youTubeThumbnailView.setTag(youtubeKey);
                Log.d(LOG_TAG, "Youtube Thumbnail Loader Initializing - Loader == NULL");
            } else {
                // Case 2 - The loader is already initialized
                Log.d(LOG_TAG, "Youtube Thumbnail Initialized");
//                youTubeThumbnailLoader.setVideo(youtubeKey);
            }
        }

        youTubeThumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String youtubeKey = (String) v.getTag();
                final Intent lightboxIntent = new Intent(context, YoutubeLightBox.class);
                lightboxIntent.putExtra(YoutubeLightBox.KEY_VIDEO_ID, youtubeKey);
                context.startActivity(lightboxIntent);
            }
        });

        Log.d(LOG_TAG, "Cursor Count = " + cursor.getCount() + " Position = " + cursor.getPosition() + "Youtube ID = " + youtubeKey);

    }

    public void release (){
        for (YouTubeThumbnailLoader loader : thumbnailLoaderMap.values()) {
            loader.release();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader thumbnailLoader) {

        youTubeThumbnailLoader = thumbnailLoader;
        thumbnailLoader.setOnThumbnailLoadedListener(new ThumbnailListener());
        thumbnailLoaderMap.put(youTubeThumbnailView, thumbnailLoader);
        youTubeThumbnailLoader.setVideo(youTubeThumbnailView.getTag().toString());

        Log.d(LOG_TAG, "Thumbnail Initialized Success - " + youTubeThumbnailView.getTag().toString());

    }

    private final class ThumbnailListener implements
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView thumbnail, String videoId) {
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView thumbnail,
                                     YouTubeThumbnailLoader.ErrorReason reason) {   }
    }


    @Override
    public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

    }

}
