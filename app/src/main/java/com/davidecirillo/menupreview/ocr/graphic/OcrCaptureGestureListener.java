package com.davidecirillo.menupreview.ocr.graphic;


import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.google.android.gms.vision.text.Text;

public class OcrCaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

    public static final String TAG = OcrCaptureGestureListener.class.getSimpleName();

    private GraphicOverlay<GraphicOverlay.Graphic> mGraphicOverlay;
    private Callback mCallback;

    public OcrCaptureGestureListener(GraphicOverlay<GraphicOverlay.Graphic> graphicOverlay, Callback callback) {
        mGraphicOverlay = graphicOverlay;
        mCallback = callback;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "onSingleTapConfirmed: " + e);
        return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    /**
     * onTap is called to capture the first TextBlock under the tap location and return it to
     * the Initializing Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        Log.d(TAG, "onTap: rawX=" + rawX + ", rawY=" + rawY);

        GraphicOverlay.Graphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);

        SearchResultContainer searchResult = null;

        if (graphic instanceof PinGraphic) {
            PinGraphic pinGraphic = (PinGraphic) graphic;
            searchResult = pinGraphic.getSearchResult();

            Log.d(TAG, "onTap: click on pin with searchResults = " + searchResult);

            mCallback.onPinClicked(searchResult, pinGraphic.getText());

        } else {
            Log.d(TAG, "onTap: graphic not handled");
        }
        return searchResult != null;
    }

    public interface Callback {
        void onPinClicked(SearchResultContainer searchResultContainer, Text text);
    }
}