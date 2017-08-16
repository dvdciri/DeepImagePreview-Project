package com.davidecirillo.menupreview.ocr.graphic;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.davidecirillo.menupreview.R;
import com.davidecirillo.menupreview.preference.PreferenceManager;
import com.davidecirillo.menupreview.preview.Detection;
import com.davidecirillo.menupreview.scan.Status;
import com.davidecirillo.menupreview.searchengine.SearchResultHandler;
import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.davidecirillo.menupreview.text_classifier.TextClassifierProvider;
import com.davidecirillo.menupreview.utils.bus.RxBus;
import com.davidecirillo.menupreview.utils.bus.events.NewDetectionFoundEvent;
import com.davidecirillo.menupreview.utils.bus.events.OcrStatusChangedEvent;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;

public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private static final String TAG = OcrDetectorProcessor.class.getSimpleName();

    private static final int MAX_TEXT_DETECTION_TO_PROCESS = 5;
    private final PreferenceManager mPreferenceManager;

    private Status mCurrentStatus;
    private Container mMaskViewContainer;
    private GraphicOverlay<GraphicOverlay.Graphic> mOcrGraphicOverlay;
    private SearchResultHandler mSearchResultHandler;
    private String[] mColors;
    private Bitmap mPinBitmap;
    private Handler mMainHandler;
    private boolean mOperating;
    private TextClassifierProvider mTextClassifierProvider;

    public OcrDetectorProcessor(GraphicOverlay<GraphicOverlay.Graphic> ocrGraphicOverlay,
                                Container maskViewContainer,
                                PreferenceManager preferenceManager,
                                SearchResultHandler searchResultHandler,
                                TextClassifierProvider textClassifierProvider) {
        mOcrGraphicOverlay = ocrGraphicOverlay;
        mMaskViewContainer = maskViewContainer;
        mPreferenceManager = preferenceManager;
        mSearchResultHandler = searchResultHandler;
        mPinBitmap = BitmapFactory.decodeResource(mOcrGraphicOverlay.getResources(), R.drawable.pin);
        mMainHandler = new Handler(Looper.getMainLooper());
        mCurrentStatus = null;
        mTextClassifierProvider = textClassifierProvider;
        sendOnStatusChanged(Status.OFF);
    }

    @Override
    public void release() {
        mOcrGraphicOverlay.clear();
    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        if (!mOperating) {
            if (mCurrentStatus != Status.LOADING) {
                sendOnStatusChanged(Status.OFF);
            }
            return;
        }

        mOcrGraphicOverlay.clear();

        List<Text> list = filterInvalidDetections(detections);

        Status newStatus = Status.DETECTING;

        for (Text text : list) {

            // Check if the text block is contained inside the oval graphic overlay
            if (mMaskViewContainer.contains(text)) {

                Log.d(TAG, "receiveDetections: text [" + text.getValue() + "]");

                /*SearchResultContainer searchResult = mPreferenceManager.getSearchResult(text.getValue());

                // If i've got results for that text in the preferences, show the rect
                if (searchResult != null && !searchResult.getImages().isEmpty()) {
                    Log.d(TAG, "receiveDetections: got results in memory");
//                    addNewPinGraphic(text, searchResult);
                    addRect(text);
                } else if (searchResult == null) {
                    Log.d(TAG, "receiveDetections: no results in memory, loading...");
                    // No UI here


                    newStatus = Status.LOADING;
                } else {
                    // NO OP
                    // If i have search results in the prefs but the item list is null or empty then just fail silently
                }*/

               /* SearchResultContainer searchResult = mPreferenceManager.getSearchResult(text.getValue());
                // If i've got results for that text in the preferences
                if (searchResult != null && !searchResult.getImages().isEmpty()) {
                }*/

                SearchResultContainer searchResult = mPreferenceManager.getSearchResult(text.getValue());

                Detection detection = new Detection(text.getValue());

                // If i've got results for that text in the preferences, show the rect
                if (searchResult != null && !searchResult.getImages().isEmpty()) {
                    detection.setContainer(searchResult);

                    addNewPinGraphic(text, searchResult);
                } else {
                    addRect(text);
                }


                RxBus.getInstance().postToEventBus(new NewDetectionFoundEvent(detection));
            }
        }

        sendOnStatusChanged(newStatus);
    }

    private List<Text> filterInvalidDetections(Detector.Detections<TextBlock> items) {
        List<Text> result = new ArrayList<>();
        SparseArray<TextBlock> detectedItems = items.getDetectedItems();

        for (int i = 0; i < detectedItems.size(); ++i) {
            TextBlock textBlock = detectedItems.valueAt(i);

            // Get sub-components and extract only lines
            List<? extends Text> components = textBlock.getComponents();

            for (Text component : components) {
                String value = component.getValue();
                if (component instanceof Line
                        && value != null
                        && !value.isEmpty()
                        && isFoodRelated((value))) {
                    result.add(component);
                } else {
                    Log.d(TAG, "filterInvalidDetections: sub-component is not a Line, should we go deeper?");
                }
            }
        }
        return result;
    }

    private boolean isFoodRelated(String textToCheck) {
        boolean result = false;
        if (mTextClassifierProvider.getClassifier() != null) {
            try {
                result = mTextClassifierProvider.getClassifier().isFoodRelated(textToCheck);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void onDestroy() {
        mPinBitmap.recycle();
        mPinBitmap = null;

        mSearchResultHandler.onDestroy();

        mMaskViewContainer = null;
        mOcrGraphicOverlay = null;

        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;

        mTextClassifierProvider = null;
    }

    public boolean isOperating() {
        return mOperating;
    }

    public void start() {
        mOperating = true;
        mSearchResultHandler.clearQueue();
        sendOnStatusChanged(Status.DETECTING);
    }

    public void stop() {
        mSearchResultHandler.clearQueue();
        mOcrGraphicOverlay.clear();
        mOperating = false;
        sendOnStatusChanged(Status.OFF);
    }

    public void loadResults(final String text) {
        sendOnStatusChanged(Status.LOADING);
        mSearchResultHandler.loadImages(text);
    }

    private void addRect(final Text text) {
        RectGraphic rectGraphic = new RectGraphic(mOcrGraphicOverlay, text);
        mOcrGraphicOverlay.add(rectGraphic);
    }

    private void addNewPinGraphic(Text text, SearchResultContainer searchResult) {
        PinGraphic pinGraphic = new PinGraphic(mOcrGraphicOverlay, text, mPinBitmap, searchResult, mMainHandler);
        mOcrGraphicOverlay.add(pinGraphic);
    }

    private void sendOnStatusChanged(final Status status) {
        if (mCurrentStatus != status) {
            RxBus.getInstance().postToEventBus(new OcrStatusChangedEvent(mCurrentStatus, status));
            mCurrentStatus = status;
        }
    }

}