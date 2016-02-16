package com.bennychee.popularmovies.event;

/**
 * Created by B on 21/01/2016.
 */
public class SyncStartEvent {

    public boolean isRetrofitCompleted;

    public SyncStartEvent(boolean isRetrofitCompleted) {
        this.isRetrofitCompleted = isRetrofitCompleted;
    }
}
