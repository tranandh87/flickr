package com.playground.android.flickr;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.playground.android.flickr.model.FlickrPhoto;
import com.playground.android.flickr.network.FlickrFetcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FlickrFetcherInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.playground.android.flickr", appContext.getPackageName());
    }

    private FlickrFetcher flickrFetcher;

    @Before
    public void setUp() {
        flickrFetcher = new FlickrFetcher();
    }

    /**
     * Test to validate FlickrFetcher and parsing JSON to object
     */
    @Test
    public void testFlickrFetcher() {
        List<FlickrPhoto> flickrPhotos = flickrFetcher.searchPhotosByTags("cat");
        assertEquals(flickrPhotos.size(), 100);
        for (FlickrPhoto flickrPhoto : flickrPhotos) {
            assertNotNull(flickrPhoto);
            assertThat("Checking whether photo has id associated with it",
                    !TextUtils.isEmpty(flickrPhoto.getId()), is(true));
        }
    }
}
