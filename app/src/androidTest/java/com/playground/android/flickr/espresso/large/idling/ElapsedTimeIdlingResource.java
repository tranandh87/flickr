package com.playground.android.flickr.espresso.large.idling;

import android.os.SystemClock;
import android.support.test.espresso.IdlingResource;


public class ElapsedTimeIdlingResource implements IdlingResource {
    private final long doneTime;
    private ResourceCallback resourceCallback;
    private boolean isIdle;


    public ElapsedTimeIdlingResource(long waitingTime) {
        this.doneTime = SystemClock.elapsedRealtime() + waitingTime;
    }

    @Override
    public String getName() {
        return ElapsedTimeIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        if (!isIdle) {
            isIdle = SystemClock.elapsedRealtime() >= doneTime;
            if (isIdle)
                resourceCallback.onTransitionToIdle();
        }

        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }
}

