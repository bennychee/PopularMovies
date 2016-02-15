package com.bennychee.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.YoutubeLightBox;
import com.bennychee.popularmovies.data.MovieContract;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

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
    private final Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailLoaderMap;

    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        thumbnailLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
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

        youTubeThumbnailView = new YouTubeThumbnailView(context);
        youTubeThumbnailView = (YouTubeThumbnailView) view.findViewById(R.id.youtube_thumbnail);

        youTubeThumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String youtubeKey = (String) v.getTag();
                final Intent lightboxIntent = new Intent(context, YoutubeLightBox.class);
                lightboxIntent.putExtra(YoutubeLightBox.KEY_VIDEO_ID, youtubeKey);
                context.startActivity(lightboxIntent);
            }
        });

        youTubeThumbnailView.setTag(youtubeKey);
        if (view == null) {
            youTubeThumbnailView.initialize(BuildConfig.YOUTUBE_API_TOKEN, this);
        }
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
        thumbnailLoaderMap.put(youTubeThumbnailView,thumbnailLoader);
        youTubeThumbnailLoader.setVideo(youTubeThumbnailView.getTag().toString());
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
