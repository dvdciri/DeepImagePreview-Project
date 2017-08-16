package com.davidecirillo.menupreview.searchengine;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.davidecirillo.menupreview.R;
import com.davidecirillo.menupreview.preference.PreferenceManager;
import com.davidecirillo.menupreview.searchengine.model.ItemImage;
import com.davidecirillo.menupreview.searchengine.model.SearchItem;
import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.davidecirillo.menupreview.utils.bus.RxBus;
import com.davidecirillo.menupreview.utils.bus.events.SearchResultReadyEvent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchResultHandler {

    private static final String TAG = SearchResultHandler.class.getSimpleName();

    private Context mContext;
    private PreferenceManager mPreferenceManager;
    private String[] mColors;
    private Map<String, Disposable> mLoadingDetections;
    private Random mRandom;

    public SearchResultHandler(Context context, PreferenceManager preferenceManager, String[] colors) {
        mContext = context;
        mPreferenceManager = preferenceManager;
        mColors = colors;
        mLoadingDetections = new HashMap<>();
        mRandom = new Random();
    }

    public void loadImages(final String text) {

        Log.d(TAG, "loadImages: text=" + text + ", mLoadingDetections.size=" + mLoadingDetections.size());

        // If is not already loading the load it
        if (!mLoadingDetections.containsKey(text)) {

//            fakeImageSearch()
            SearchApi.getsInstance().doImageSearch(text)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Observer<SearchResultContainer>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.d(TAG, "loadImages: onSubscribe: saving disposable inside loading detections, text=" + text);
                            // Save disposable and associated text in order to kill the process if needed
                            mLoadingDetections.put(text, d);
                        }

                        @Override
                        public void onNext(SearchResultContainer container) {
                            Log.d(TAG, "loadImages: onNext: text=" + text + ", container=" + container);

                            // We always want to save items even if is empty, so it won't do another
                            // fetch for the next time but just fail silently
                            saveResultAndLoadPreview(container, text);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError: doImageSearch = " + e.getMessage());

                            Toast.makeText(mContext, R.string.result_error_message, Toast.LENGTH_SHORT).show();

                            mLoadingDetections.remove(text);
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete: doImageSearch");
                        }
                    });
        }
    }

    private void saveResultAndLoadPreview(final SearchResultContainer container, final String text) {

        // If i don't have items then just save it into the config, it will be ignored on the screen
        if (container != null && !container.getImages().isEmpty()) {
            container.setCreatedAt(System.currentTimeMillis());

            // Load first image as bitmap and save search results
            Picasso.with(mContext)
                    .load(container.getFirstThumbnailLink())
                    .resize(50, 50)
                    .centerCrop()
                    .fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            saveSearchResultAndRemoveFromLoadingDetections(container, text);
                        }

                        @Override
                        public void onError() {
                            saveSearchResultAndRemoveFromLoadingDetections(container, text);
                        }
                    });
        } else {
            saveSearchResultAndRemoveFromLoadingDetections(new SearchResultContainer(), text);
        }
    }

    private void saveSearchResultAndRemoveFromLoadingDetections(SearchResultContainer container, String text) {
        String color = mColors[mRandom.nextInt(mColors.length)];
        container.setColor(color);

        mPreferenceManager.saveSearchResults(container, text);

        // Removing the item as saved into prefs
        mLoadingDetections.remove(text);

        RxBus.getInstance().postToEventBus(new SearchResultReadyEvent(container, text));
    }

    public void onDestroy() {
        clearQueue();
        mContext = null;
    }

    public void clearQueue() {
        Log.d(TAG, "clearQueue: size to be cleared=" + mLoadingDetections.size());
        // For each of the loading detections dispose it and remove it. Fuck em off
        Iterator<Map.Entry<String, Disposable>> iterator = mLoadingDetections.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Disposable> entry = iterator.next();

            Disposable disposable = entry.getValue();
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
            iterator.remove();
        }
    }

    private Observable<SearchResultContainer> fakeImageSearch() {
        return Observable.defer(new Callable<ObservableSource<? extends SearchResultContainer>>() {
            @Override
            public ObservableSource<? extends SearchResultContainer> call() throws Exception {
                ArrayList<SearchItem> items = new ArrayList<>();

                items.add(new SearchItem(new ItemImage("http://media.cntraveler.com/photos/542333488614701924e124a9/master/w_775," +
                        "c_limit/italy-food-intro-1.jpg")));
                items.add(new SearchItem(new ItemImage("http://i.ndtvimg.com/i/2016-03/bruschetta-625_625x350_41459344513.jpg")));
                items.add(new SearchItem(new ItemImage("https://s-media-cache-ak0.pinimg" +
                        ".com/originals/bd/1f/89/bd1f899131e589e3e8ca30784e6320d4.jpg")));
                items.add(new SearchItem(new ItemImage("http://az616578.vo.msecnd.net/files/2016/10/01/636109138160187836348256689_food2.jpg")));
                items.add(new SearchItem(new ItemImage("http://media.cntraveler.com/photos/542333488614701924e124a8/master/w_775," +
                        "c_limit/italy-food-bigoli-2.jpg")));
                items.add(new SearchItem(new ItemImage("http://foodsofitalybedford.co.uk/communities/6/000/001/507/226//images/5770578.jpg")));
                items.add(new SearchItem(new ItemImage("http://www.mytorontoscoop.com/rsz_lasagna%20(2).jpg")));
                items.add(new SearchItem(new ItemImage("http://www.italian-feelings.com/wp-content/uploads/2016/02/nord-sud-940x625.jpg")));
                items.add(new SearchItem(new ItemImage("http://www.ndtv.com/cooks/images/pasta-carbonara-ritu-dalmia_article.jpg")));
                items.add(new SearchItem(new ItemImage("http://www.morningadvertiser.co" +
                        ".uk/var/plain_site/storage/images/publications/hospitality/morningadvertiser.co" +
                        ".uk/pub-food/good-food-guide-editor-calls-for-more-veggie-options-on-pub-menus/8422626-1-eng-GB/Good-Food-Guide-editor" +
                        "-calls-for-more-veggie-options-on-pub-menus.jpg")));
                items.add(new SearchItem(new ItemImage("https://static.independent.co" +
                        ".uk/s3fs-public/styles/article_small/public/thumbnails/image/2016/12/19/18/sush0istock-gkrphoto.jpg")));
                items.add(new SearchItem(new ItemImage("http://www.2sfg" +
                        ".com/globalassets/corporate/home-page/2sisters-food-group-roast-banner5.jpg")));
                items.add(new SearchItem(new ItemImage("http://www.oghmapartners.com/wp-content/uploads/2017/05/1417931965.jpg")));

                return Observable.just(new SearchResultContainer(items)).delay(3, TimeUnit.SECONDS);
            }
        });
    }
}
