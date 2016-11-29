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
import com.playground.android.flickr.model.FlickrPhoto;
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
    private List<FlickrPhoto> flickrPhotos;

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

        imageDownloader.setImageDownloadListener(
                new ImageDownloader.ImageDownloadListener<ImageButton>() {
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
                });

        if (savedInstanceState != null)
            position = savedInstanceState.getInt(KEY_POSITION, 0);

        imageDownloader.start();
        imageDownloader.getLooper();
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

        if (isLandScape) {
            LinearLayout imageContainer = (LinearLayout) v.findViewById(R.id.imageContainer);
            imageContainer.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams layoutParams;

            int width = 300;
            int height = 200;
            layoutParams = new LinearLayout.LayoutParams(0, height * 2, 1f);
            layoutParams.setMargins(100, 0, 0, 0);
            image.setLayoutParams(layoutParams);

            layoutParams = new LinearLayout.LayoutParams(width, height, 1f);
            title.setLayoutParams(layoutParams);

        }
        return v;
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
        getLoaderManager().initLoader(LOADER_ID, null, new FlickrPhotoLoaderListener(SEARCH_TAG));
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, new FlickrPhotoLoaderListener(SEARCH_TAG));
    }

    private void renderScreen(List<FlickrPhoto> photos) {
        stopProgress();
        Log.i(TAG, "Current position = " + position);

        if (photos.size() > position) {
            FlickrPhoto currentPhoto = photos.get(position);
            if (currentPhoto != null) {
                if (imageDownloader.memoryCache == null)
                    tryToSetImageDownloaderMemCache(getActivity().getSupportFragmentManager());

                showProgress(getString(R.string.progess_title_image), getString(
                        R.string.progess_message_image, currentPhoto.getUrl_s()));
                imageDownloader.queueThumbnail(image, currentPhoto.getUrl_s());
                String title = currentPhoto.getTitle();
                this.title.setText(TextUtils.isEmpty(title) ? getString(R.string.no_title) : title);
                Log.i(TAG, "Title : " + title);
                Log.i(TAG, "URL : " + currentPhoto.getUrl_s());
            }
        }
    }

    private void tryToSetImageDownloaderMemCache(FragmentManager supportFragmentManager) {
        RetainFragment retainFragment =
                RetainFragment.findOrCreateRetainFragment(supportFragmentManager);
        if (imageDownloader != null && retainFragment != null)
            imageDownloader.memoryCache = retainFragment.retainedCache;
    }

    private void renderScreen() {
        if (flickrPhotos != null)
            renderScreen(flickrPhotos);
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

    //Class for Implementing flickr photo loader (FlickPhotoLoader) call back methods
    private class FlickrPhotoLoaderListener implements LoaderManager.LoaderCallbacks<List<FlickrPhoto>> {
        String tagValues = null;

        FlickrPhotoLoaderListener(String tagValues) {
            this.tagValues = tagValues;
        }

        @Override
        public Loader<List<FlickrPhoto>> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.
            showProgress(getString(R.string.progess_title_api),
                    getString(R.string.progess_message_api, SEARCH_TAG));
            Log.i(TAG, "On Create Loader");
            return new FlickrPhotoTaskLoader(getActivity(), tagValues);
        }

        @Override
        public void onLoadFinished(Loader<List<FlickrPhoto>> loader, List<FlickrPhoto> photos) {
            Log.i(TAG, "On Loader Finished");
            if (photos != null) {
                flickrPhotos = photos;
                renderScreen(photos);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<FlickrPhoto>> loader) {
            Log.i(TAG, " On Loader Reset");
        }
    }

    //Async Loader for getting flickr photos
    private static class FlickrPhotoTaskLoader extends AsyncTaskLoader<List<FlickrPhoto>> {

        List<FlickrPhoto> flickrPhotos;
        String tagValues;

        FlickrPhotoTaskLoader(Context context, String tagValues) {
            super(context);
            this.tagValues = tagValues;

            Log.i(TAG, "On Loader Constructor");
        }

        @Override
        public List<FlickrPhoto> loadInBackground() {
            Log.i(TAG, "Starting loader background thread");
            if (!TextUtils.isEmpty(tagValues))
                return new FlickrFetcher().searchPhotosByTags(ImageFragment.SEARCH_TAG);
            return null;
        }

        @Override
        protected void onStartLoading() {
            Log.i(TAG, "On start Loading");
            if (flickrPhotos != null) {
                deliverResult(flickrPhotos);
            }

            if (takeContentChanged() || flickrPhotos == null) {
                forceLoad();
            }
        }

        @Override
        public void deliverResult(List<FlickrPhoto> photos) {
            Log.i(TAG, "On Loader delivering result");
            flickrPhotos = photos;

            if (isStarted()) {
                super.deliverResult(photos);
            }
        }

        @Override
        protected void onStopLoading() {
            Log.i(TAG, "On stop Loading");
            cancelLoad();
        }

        @Override
        public void onCanceled(List<FlickrPhoto> photos) {
            Log.i(TAG, "On Loader Cancelled");
            super.onCanceled(photos);
        }

        @Override
        protected void onReset() {
            Log.i(TAG, "On Loader Reset");
            super.onReset();

            onStopLoading();

            if (flickrPhotos != null) {
                flickrPhotos = null;
            }
        }
    }
}
