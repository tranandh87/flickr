package com.playground.android.flickr.espresso.large;

import android.content.pm.ActivityInfo;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.playground.android.flickr.R;
import com.playground.android.flickr.activities.ImageActivity;
import com.playground.android.flickr.espresso.large.idling.ElapsedTimeIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FlickrImageLoadingTest {

    @Rule
    public ActivityTestRule<ImageActivity> home = new ActivityTestRule<>(ImageActivity.class, true, false);

    @Before
    public void setUp() {
        home.launchActivity(null);
        IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.MINUTES);
    }

    // Validate continuous click through image button renders image and title correctly
    @Test
    public void testImageActivity() {
        final int initialIdealTime = 5;
        final int imageLoadingIdleTime = 2;
        for (int count = 0; count < 150; count++) {
            int idleTime;
            if (count % 99 == 0)
                idleTime = initialIdealTime;
            else
                idleTime = imageLoadingIdleTime;

            IdlingResource idle = new ElapsedTimeIdlingResource(TimeUnit.SECONDS.toMillis(idleTime));
            Espresso.registerIdlingResources(idle);
            onView(withId(R.id.imageButton))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.imageTitle))
                    .check(matches(isDisplayed()));
            doneWaitingForActivity(idle);

            onView(withId(R.id.imageButton)).perform(click());
        }
    }

    // Validate orientation changes renders image and title orrectly
    @Test
    public void testImageActivityOrientationChanges() {
        home.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        IdlingResource idle = new ElapsedTimeIdlingResource(TimeUnit.SECONDS.toMillis(5));
        Espresso.registerIdlingResources(idle);
        onView(withId(R.id.imageButton))
                .check(matches(isDisplayed()));
        onView(withId(R.id.imageTitle))
                .check(matches(isDisplayed()));
        doneWaitingForActivity(idle);
    }

    @After
    public void tearDown() {

    }

    public void doneWaitingForActivity(IdlingResource idle) {
        if (idle != null) {
            if (!idle.isIdleNow())
                throw new IllegalStateException(idle.getClass().getSimpleName() + "" +
                        " attempt to unregister when not idle");

            Espresso.unregisterIdlingResources(idle);
        }
    }

}
