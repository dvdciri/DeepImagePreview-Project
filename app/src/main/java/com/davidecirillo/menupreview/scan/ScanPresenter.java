package com.davidecirillo.menupreview.scan;


import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.davidecirillo.menupreview.ocr.graphic.OcrDetectorProcessor;
import com.davidecirillo.menupreview.preference.PreferenceManager;
import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.davidecirillo.menupreview.text_classifier.ClassifierConfiguration;
import com.davidecirillo.menupreview.text_classifier.TextClassifier;
import com.davidecirillo.menupreview.text_classifier.TextClassifierProvider;
import com.davidecirillo.menupreview.utils.bus.RxBus;
import com.davidecirillo.menupreview.utils.bus.events.NewDetectionFoundEvent;
import com.davidecirillo.menupreview.utils.bus.events.OcrStatusChangedEvent;
import com.davidecirillo.menupreview.utils.bus.events.PageChangedEvent;
import com.davidecirillo.menupreview.utils.bus.events.SearchResultReadyEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ScanPresenter implements TextClassifierProvider {

    private static final String TAG = ScanPresenter.class.getSimpleName();

    private TextClassifier mTextClassifier;
    private ScanView mScanView;
    private Resources mResources;
    private PreferenceManager mPreferenceManager;
    private CompositeDisposable mCompositeDisposable;
    private OcrDetectorProcessor mProcessor;

    public ScanPresenter(ScanView scanView, Resources resources, PreferenceManager preferenceManager) {
        mScanView = scanView;
        mResources = resources;
        mPreferenceManager = preferenceManager;
    }

    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mTextClassifier == null) {
                    mTextClassifier = createTextClassifier();
                }
            }
        }).start();
    }

    public void setProcessor(OcrDetectorProcessor processor) {
        mProcessor = processor;
    }

    public void onResume() {
        // Create a new composite disposal every resume as we need to be able to re-dispose it (you can't re-use it after you've dispose it)
        mCompositeDisposable = new CompositeDisposable();

        registerMainThreadEvents();
        registerEvents();
    }

    public void onPause() {
        // Dispose subscriptions
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
    }

    public void onDestroy() {
        mProcessor.onDestroy();
    }

    public void loadOrShowResults(String query) {
        mProcessor.stop(); // DETECTING - OFF

        SearchResultContainer searchResult = mPreferenceManager.getSearchResult(query);

        // Load or show if in memory
        if (searchResult != null && !searchResult.getImages().isEmpty()) {
            mScanView.showPreviewResults(searchResult, query);
        } else {
            mScanView.setLoading(true);
            mProcessor.loadResults(query);
        }
    }

    public void onBottomSheetHided() {
        // TODO: 19/07/2017 Do we need to start it again here? how is the UX?
    }


    public void onBottomSheetShown() {
        mProcessor.stop();
    }

    public Collection<String> getOrderedSearchHistory() {
        List<String> result = new ArrayList<>();

        // Sort the entry list by the createdAt timestamp
        List<Map.Entry<String, SearchResultContainer>> sortedList = new ArrayList<>(mPreferenceManager.getSearchResultList().entrySet());
        Collections.sort(sortedList, new Comparator<Map.Entry<String, SearchResultContainer>>() {
            @Override
            public int compare(Map.Entry<String, SearchResultContainer> m1, Map.Entry<String, SearchResultContainer> m2) {
                return m1.getValue().getCreatedAt().compareTo(m2.getValue().getCreatedAt());
            }
        });

        // Filter out the results without images and return them to the result linked map in order
        for (Map.Entry<String, SearchResultContainer> entry : sortedList) {
            SearchResultContainer searchResultContainer = entry.getValue();

            // Filter out the ones without images and sort them by createdAt timestamp
            if (searchResultContainer != null && !searchResultContainer.getImages().isEmpty()) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    public View.OnClickListener getStartProcessorClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mProcessor != null) {
                    mProcessor.start();
                }
            }
        };
    }

    public View.OnClickListener getStopProcessorClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mProcessor != null) {
                    mProcessor.stop();
                }
            }
        };
    }

    public View.OnClickListener getStopLoadingPreviewClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScanView.setLoading(false);
                if (mProcessor != null) {
                    // Always stop, even if is not operating
                    mProcessor.stop();
                }
            }
        };
    }


    @Nullable
    @Override
    public TextClassifier getClassifier() {
        return mTextClassifier;
    }

    /* PRIVATE METHODS */
    private void registerMainThreadEvents() {
        Disposable disposable = RxBus.getInstance()
                .toObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                        if (o instanceof OcrStatusChangedEvent) {

                            mScanView.handleOrcStatusChangedEvent(((OcrStatusChangedEvent) o));
                        } else if (o instanceof NewDetectionFoundEvent) {

                            mScanView.handleNewDetectionFound(((NewDetectionFoundEvent) o).getDetection());
                        } else if (o instanceof SearchResultReadyEvent) {

                            SearchResultReadyEvent event = (SearchResultReadyEvent) o;
                            mScanView.showPreviewResults(event.getSearchResultContainer(), event.getQuery());
                        }
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private void registerEvents() {
        Disposable disposable = RxBus.getInstance()
                .toObservable()
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                        if (o instanceof PageChangedEvent) {
                            handlePageChangedEvent((PageChangedEvent) o);
                        }
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    private TextClassifier createTextClassifier() {
        try {
            Type type = new TypeToken<HashMap<String, Integer>>() {
            }.getType();
            Gson gson = new Gson();
            AssetManager assets = mResources.getAssets();

            // Load config data
            InputStreamReader inAuxData = new InputStreamReader(assets.open("aux_data.json"));
            BufferedReader auxDataReader = new BufferedReader(inAuxData);
            ClassifierConfiguration configuration = gson.fromJson(auxDataReader, ClassifierConfiguration.class);
            auxDataReader.close();
            inAuxData.close();

            // Load train positive file
            InputStreamReader inTrainPositive = new InputStreamReader(assets.open("train_positive.json"));
            BufferedReader trainPositiveReader = new BufferedReader(inTrainPositive);
            Map<String, Integer> positiveWords = gson.fromJson(trainPositiveReader, type);
            trainPositiveReader.close();
            inTrainPositive.close();

            // Load train negative file
            InputStreamReader inTrainNegative = new InputStreamReader(assets.open("train_negative.json"));
            BufferedReader trainNegativeReader = new BufferedReader(inTrainNegative);
            Map<String, Integer> negativeWords = gson.fromJson(trainNegativeReader, type);
            trainNegativeReader.close();
            inTrainNegative.close();

            // Load stop words file
            InputStreamReader inStopWords = new InputStreamReader(assets.open("stop_words.txt"));
            BufferedReader stopWordsReader = new BufferedReader(inStopWords);
            Set<String> stopWords = new HashSet<>();
            String line = stopWordsReader.readLine();
            while (line != null) {
                stopWords.add(line);
                line = stopWordsReader.readLine();
            }
            stopWordsReader.close();
            inStopWords.close();

            return new TextClassifier(configuration, positiveWords, negativeWords, stopWords);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handlePageChangedEvent(PageChangedEvent pageChangedEvent) {
        Log.d(TAG, "handlePageChangedEvent: " + pageChangedEvent);

        int oldPage = pageChangedEvent.getOldPage();
        if (oldPage == 0) {
            if (mProcessor.isOperating()) {
                mProcessor.stop();
            }
        }
    }
}
