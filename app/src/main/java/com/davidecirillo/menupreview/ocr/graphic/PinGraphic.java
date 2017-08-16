package com.davidecirillo.menupreview.ocr.graphic;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.davidecirillo.menupreview.utils.ImageLoaderHelper;
import com.google.android.gms.vision.text.Text;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class PinGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = PinGraphic.class.getSimpleName();
    private static final int PREVIEW_IMAGE_SIZE = 80;

    private final Text mText;
    private final SearchResultContainer mSearchResult;
    private final Handler mMainHandler;
    private Paint mPinPaint;
    private Bitmap mImagePinBitmap;
    private RectF mPinRectF;
    private RectF mImagePreviewRect;
    private Bitmap mBitmapPreviewImage;

    public PinGraphic(GraphicOverlay overlay, Text text, Bitmap imagePinBitmap, SearchResultContainer searchResult, Handler mainHandler) {
        super(overlay);
        mText = text;
        mImagePinBitmap = imagePinBitmap;
        mSearchResult = searchResult;
        mMainHandler = mainHandler;

        init();

        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    private void init() {
        Log.d(TAG, "init: search result = " + mSearchResult);

        mPinPaint = new Paint();
        mPinPaint.setColorFilter(new PorterDuffColorFilter(
                Color.parseColor(mSearchResult.getColor()),
                PorterDuff.Mode.SRC_IN));

        Rect textBounding = mText.getBoundingBox();

        float pinWidth = 110;
        float pinHeight = 160;
        float pinLeft = translateX(textBounding.left) - (pinWidth / 2);
        float pinTop = translateY(textBounding.bottom) - pinHeight;
        float pinRight = translateX(textBounding.left) + pinWidth;
        float pinBottom = translateY(textBounding.bottom);

        float pinCenterX = pinRight - (pinWidth / 2);
        float imageLeft = pinCenterX - (PREVIEW_IMAGE_SIZE / 1.2f);
        float imageTop = pinTop + 15;
        float imageRight = imageLeft + PREVIEW_IMAGE_SIZE;
        float imageBottom = imageTop + PREVIEW_IMAGE_SIZE;

        //Create the rect that will contain the pointer
        mPinRectF = new RectF(pinLeft, pinTop, pinRight, pinBottom);
        mImagePreviewRect = new RectF(imageLeft, imageTop, imageRight, imageBottom);

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {

                ImageLoaderHelper.loadImagePreviewFromCache(mOverlay.getContext(), mSearchResult, PREVIEW_IMAGE_SIZE,
                        new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                mBitmapPreviewImage = bitmap;
                                postInvalidate();
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImagePinBitmap = null;
        mBitmapPreviewImage = null;
    }

    public SearchResultContainer getSearchResult() {
        return mSearchResult;
    }

    public Text getText() {
        return mText;
    }

    public boolean contains(float x, float y) {
        boolean contains = false;
        if (mPinRectF != null) {
            contains = mPinRectF.left < x && mPinRectF.right > x && mPinRectF.top < y && mPinRectF.bottom > y;
        }
        return contains;
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {

        // Draw the pointer on the canvas with the rect and the paint
        canvas.drawBitmap(mImagePinBitmap, null, mPinRectF, mPinPaint);

        if (mBitmapPreviewImage != null) {
            canvas.drawBitmap(mBitmapPreviewImage, null, mImagePreviewRect, null);
        }
    }
}
