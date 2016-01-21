package com.bennychee.popularmovies.event;

/**
 * Created by B on 21/01/2016.
 */
public class ReviewEvent {
    public boolean isRetrofitCompleted;

    public ReviewEvent (boolean isRetrofitCompleted) {
        this.isRetrofitCompleted = isRetrofitCompleted;
    }
}
