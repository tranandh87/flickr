package com.playground.android.flickr.handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.playground.android.flickr.network.FlickrFetcher;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ImageDownloader<T> extends HandlerThread {
    private static final String TAG = "ImageDownloader";

    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> requestMap = new ConcurrentHashMap<>();
    private Handler responseHandler;
    private ImageDownloadListener<T> imageDownloadListener;

    // Simple BitMap LRU memory Cache
    public LruCache<String, Bitmap> memoryCache;

    public interface ImageDownloadListener<T> {
        void onImageDownloaded(T target, Bitmap thumbnail);

        void onCachedImage(T target, Bitmap thumbnail);
    }

    public void setImageDownloadListener(ImageDownloadListener<T> listener) {
        imageDownloadListener = listener;
    }

    public ImageDownloader(Handler responseHandler, LruCache<String, Bitmap> memoryCacheObject) {
        super(TAG);
        this.responseHandler = responseHandler;
        memoryCache = memoryCacheObject;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + requestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            requestMap.remove(target);
        } else {
            requestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private void handleRequest(final T target) {
        try {
            final String url = requestMap.get(target);
            if (url == null)
                return;

            final Bitmap bitmapFromMemCache = getBitmapFromMemoryCache(String.valueOf(url));
            if (bitmapFromMemCache != null) {
                Log.i(TAG, "Got cached image");
                responseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageDownloadListener.onCachedImage(target, bitmapFromMemCache);
                    }
                });
            } else {
                byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
                final Bitmap bitmap = BitmapFactory
                        .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                Log.i(TAG, "Bitmap created");

                responseHandler.post(new Runnable() {
                    public void run() {
                        String mapKey = requestMap.get(target);
                        if (mapKey != null && !mapKey.equals(url)) {
                            return;
                        }
                        requestMap.remove(target);
                        imageDownloadListener.onImageDownloaded(target, bitmap);
                        BitmapMemoryCacheWorkerTask task = new BitmapMemoryCacheWorkerTask(bitmap);
                        task.execute(url);
                    }
                });
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (memoryCache != null)
            if (getBitmapFromMemoryCache(key) == null) {
                memoryCache.put(key, bitmap);
            }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        if (memoryCache != null)
            return memoryCache.get(key);
        return null;
    }

    // Asynch task to add bitmap to memory cache in background thread
    private class BitmapMemoryCacheWorkerTask extends AsyncTask<String, Void, Bitmap> {
        Bitmap bitMap;

        BitmapMemoryCacheWorkerTask(Bitmap bitMap) {
            this.bitMap = bitMap;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            addBitmapToMemoryCache(String.valueOf(params[0]), bitMap);
            return bitMap;
        }
    }
}
