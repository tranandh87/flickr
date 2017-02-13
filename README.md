#FlickrImageFetcher Android App

Simple flickr image fetching app

##Project MinSdkVersion is 16

##Run and build instruction:
Use Gradle to build the project

##High level algorithm:

1. When app launches, a. Loader is initialized and hence AsynchLoader (FlickrImageTaskLoader) calls
flick search api (using tag="cat") b. Dedicated background HandlerThread (ImageDownloader -
BitMap image loader which uses LRU memory cache to cache downloaded bitmap) is created.
While rending UI, image title is updated and first image is downloaded using ImageDownloader.
Progress dialog is shown separately for search api and image download

2. When User click ImageButton, further images are downloaded as BitMap from search result url for
each photo object by on-demand basis. Downloaded images are cached using simple LRU
memory cache to avoid downloading images mainly on orientation changes

3. Once user clicks beyond the first set of search api result photos, new flickr search api call is made
with same query and above process is repeated


##Diving into implementation:

1. Used AsyncTaskLoader (FlickrImageTaskLoader - Abstract loader). Used loader especially to avoid
loading data on configuration changes in this use case along with other benefits of loaders
• Created dedicated background Handler Thread (ImageDownloader - Extends HandlerThread) for
downloading image. Image is cached using LRU memory cache

2. For memory cache to persist across orientation changes, using RetainFragment with LRUCache
instance. Setting RetainFragment setRetainInstance to true to persist the cache on orienation
changes

3. In landscape orientation, programmatically changing LinearLayout (containing ImageButton and
TextView) orientation to Horizontal and adjusting views layout params. This is done to avoid having
another layout file for landscape orientation since we are just dealing with two view in here

4. Implemented both Espresso test for UI automation and instrument test
Would like to mention about libraries used and not used

5. Used basic libraries like appcompat, gson, junit and espresso. I have not used any other libraries
like ButterKnife, Volley and image loading library like Picasso etc to accomplish this task for this
practice test.
