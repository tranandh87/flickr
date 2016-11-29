package com.playground.android.flickr.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playground.android.flickr.R;
import com.playground.android.flickr.handler.ImageDownloader;
import com.playground.android.flickr.model.FlickrImage;
import com.playground.android.flickr.network.FlickrFetcher;

import java.util.List;

public class ImageFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ImageFragment";
    private static final String SEARCH_TAG = "cat";
    public static final int LOADER_ID = 0;
    public static final int API_RESULT_COUNT = 100;
    private static final String KEY_POSITION = "key_position";
    private int position = 0;

    // Views
    ImageButton image;
    TextView title;
    ProgressDialog progressDialog;

    private boolean isLandScape;
    private List<FlickrImage> mFlickrImages;

    private ImageDownloader<ImageButton> imageDownloader;

    LruCache<String, Bitmap> memoryCache;

    public static ImageFragment newInstance() {
        return new ImageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLandScape = getActivity().getResources().getConfiguration().orientation == 2;

        Handler responseHandler = new Handler();

        imageDownloader = new ImageDownloader<>(responseHandler, memoryCache);

        imageDownloader.setImageDownloadListener(new BitMapImageLoaderListener());
        imageDownloader.start();
        imageDownloader.getLooper();

        if (savedInstanceState != null)
            position = savedInstanceState.getInt(KEY_POSITION, 0);

        Log.i(TAG, "Background thread started");

        initLoader();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image, container, false);
        image = (ImageButton) v.findViewById(R.id.imageButton);
        image.setOnClickListener(this);
        image.setImageResource(android.R.drawable.sym_def_app_icon);
        title = ((TextView) v.findViewById(R.id.imageTitle));

        if (isLandScape)
            customizeUIForlandscape(v);

        return v;
    }

    /**
     * In landscape programmatically change LinearLayout orientation to Horizontal and adjust
     * view layout params.
     * This is done to avoid having another layout file for landscape orientation
     * since we are just dealing with two view in here
     */
    private void customizeUIForlandscape(View v) {
        LinearLayout imageContainer = (LinearLayout) v.findViewById(R.id.imageContainer);
        imageContainer.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams layoutParams;

        int width = 300;
        int height = 200;
        layoutParams = new LinearLayout.LayoutParams(width, height * 3, 1f);
        layoutParams.setMargins(100, 0, 0, 0);
        image.setLayoutParams(layoutParams);

        layoutParams = new LinearLayout.LayoutParams(width, height, 1f);
        title.setLayoutParams(layoutParams);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(KEY_POSITION, position);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopProgress();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
        progressDialog = null;
    }

    private void initLoader() {
        getLoaderManager().initLoader(LOADER_ID, null, new FlickrImageLoaderListener(SEARCH_TAG));
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, new FlickrImageLoaderListener(SEARCH_TAG));
    }

    private void renderScreen(List<FlickrImage> images) {
        stopProgress();
        Log.i(TAG, "Current position = " + position);

        if (images.size() > position) {
            FlickrImage currentImage = images.get(position);
            if (currentImage != null) {
                if (imageDownloader.memoryCache == null)
                    tryToSetImageDownloaderMemCache(getActivity().getSupportFragmentManager());

                if (currentImage.getUrl_s() != null) {
                    showProgress(getString(R.string.progess_title_image), getString(
                            R.string.progess_message_image, currentImage.getUrl_s()));
                    imageDownloader.queueThumbnail(image, currentImage.getUrl_s());
                }
                else
                    image.setImageResource(android.R.drawable.sym_def_app_icon);

                String title = currentImage.getTitle();
                this.title.setText(TextUtils.isEmpty(title) ? getString(R.string.no_title) : title);
                Log.i(TAG, "Title : " + title);
                Log.i(TAG, "URL : " + currentImage.getUrl_s());
            }
        }
    }

    // Set ImageDownloader memory cache from Retain Fragment retained cache
    private void tryToSetImageDownloaderMemCache(FragmentManager supportFragmentManager) {
        RetainFragment retainFragment =
                RetainFragment.findOrCreateRetainFragment(supportFragmentManager);
        if (imageDownloader != null && retainFragment != null)
            imageDownloader.memoryCache = retainFragment.retainedCache;
    }

    private void renderScreen() {
        if (mFlickrImages != null)
            renderScreen(mFlickrImages);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.imageButton:
                if (position >= API_RESULT_COUNT - 1) {
                    restartLoader();
                    position = 0;
                } else
                    position++;

                renderScreen();
        }
    }

    public void showProgress(String title, String message) {
        if (progressDialog != null)
            stopProgress();

        progressDialog = ProgressDialog.show(getActivity(), title, message);
    }

    private void stopProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    //Class for Implementing flickr image loader (FlickImageLoader) call back methods
    private class FlickrImageLoaderListener implements LoaderManager.LoaderCallbacks<List<FlickrImage>> {
        String tagValues = null;

        FlickrImageLoaderListener(String tagValues) {
            this.tagValues = tagValues;
        }

        @Override
        public Loader<List<FlickrImage>> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.
            showProgress(getString(R.string.progess_title_api),
                    getString(R.string.progess_message_api, SEARCH_TAG));
            Log.i(TAG, "On Create Loader");
            return new FlickrImageTaskLoader(getActivity(), tagValues);
        }

        @Override
        public void onLoadFinished(Loader<List<FlickrImage>> loader, List<FlickrImage> images) {
            Log.i(TAG, "On Loader Finished");
            if (images != null) {
                mFlickrImages = images;
                renderScreen(images);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<FlickrImage>> loader) {
            Log.i(TAG, " On Loader Reset");
        }
    }

    //Async Loader for getting flickr images
    private static class FlickrImageTaskLoader extends AsyncTaskLoader<List<FlickrImage>> {

        List<FlickrImage> mFlickrImages;
        String tagValues;

        FlickrImageTaskLoader(Context context, String tagValues) {
            super(context);
            this.tagValues = tagValues;

            Log.i(TAG, "On Loader Constructor");
        }

        @Override
        public List<FlickrImage> loadInBackground() {
            Log.i(TAG, "Starting loader background thread");
            if (!TextUtils.isEmpty(tagValues))
                return new FlickrFetcher().searchImagesByTags(ImageFragment.SEARCH_TAG);
            return null;
        }

        @Override
        protected void onStartLoading() {
            Log.i(TAG, "On start Loading");
            if (mFlickrImages != null) {
                deliverResult(mFlickrImages);
            }

            if (takeContentChanged() || mFlickrImages == null) {
                forceLoad();
            }
        }

        @Override
        public void deliverResult(List<FlickrImage> images) {
            Log.i(TAG, "On Loader delivering result");
            mFlickrImages = images;

            if (isStarted()) {
                super.deliverResult(images);
            }
        }

        @Override
        protected void onStopLoading() {
            Log.i(TAG, "On stop Loading");
            cancelLoad();
        }

        @Override
        public void onCanceled(List<FlickrImage> images) {
            Log.i(TAG, "On Loader Cancelled");
            super.onCanceled(images);
        }

        @Override
        protected void onReset() {
            Log.i(TAG, "On Loader Reset");
            super.onReset();

            onStopLoading();

            if (mFlickrImages != null) {
                mFlickrImages = null;
            }
        }
    }

    // Listener class for implementing ImageLoader interface to receive call back
    private class BitMapImageLoaderListener implements ImageDownloader.ImageDownloadListener<ImageButton>{
        @Override
        public void onImageDownloaded(ImageButton imageButton, Bitmap bitmap) {
            setImageButtonResource(imageButton, bitmap);
        }

        @Override
        public void onCachedImage(ImageButton imageButton, Bitmap bitmap) {
            setImageButtonResource(imageButton, bitmap);
        }

        private void setImageButtonResource(ImageButton imageButton, Bitmap bitmap) {
            if (isAdded()) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                imageButton.setImageDrawable(drawable);
                stopProgress();
            }
        }
    }
}
