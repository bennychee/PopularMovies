package com.bennychee.popularmovies.event;

/**
 * Created by B on 21/01/2016.
 */
public class RuntimeEvent {

    public boolean isRetrofitCompleted;

    public RuntimeEvent(boolean isRetrofitCompleted) {
        this.isRetrofitCompleted = isRetrofitCompleted;
    }
}
