package com.bennychee.popularmovies.event;

/**
 * Created by B on 21/01/2016.
 */
public class SyncStopEvent {

    public boolean isRetrofitCompleted;

    public SyncStopEvent(boolean isRetrofitCompleted) {
        this.isRetrofitCompleted = isRetrofitCompleted;
    }
}
