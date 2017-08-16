package com.davidecirillo.menupreview.ocr.graphic;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.davidecirillo.menupreview.R;
import com.google.android.gms.vision.text.Text;

public class MaskView extends View implements Container {

    public static final int TEXT_SIZE = 45;

    private RectF mMask;
    private Paint mBackgroundPaint;
    private Paint mTransparentPaint;
    private Paint mTextPaint;
    private GraphicOverlay mOverlay;

    public MaskView(Context context) {
        super(context);
        init();
    }

    public MaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mTransparentPaint == null) {
            mTransparentPaint = new Paint();
            mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        if (mBackgroundPaint == null) {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundPaint.setAlpha(100);
        }
        if (mTextPaint == null) {
            mTextPaint = new Paint();
            mTextPaint.setColor(Color.WHITE);
            mTextPaint.setTextSize(TEXT_SIZE);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
        }
    }

    public void setOverlay(GraphicOverlay overlay) {
        mOverlay = overlay;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMask = new RectF();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMask = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        canvas.drawPaint(mBackgroundPaint);

        float rectHeight = 550;
        float rectWidth = 1000;
        float maskLeft = (canvas.getWidth() - rectWidth) / 2;
        float maskRight = canvas.getWidth() - maskLeft;
        float maskTop = (canvas.getHeight() - rectHeight) / 2;
        float maskBottom = canvas.getHeight() - maskTop;

        // Draw a rect in the center of the overlay
        mMask.set(maskLeft, maskTop, maskRight, maskBottom);
//        canvas.drawRect(mMask, mTransparentPaint);

        // Draw text below centered of the rect
        float textLeft = canvas.getWidth() / 2;
        int textSizeOffset = TEXT_SIZE * 2;
        float textTop = rectHeight + ((canvas.getHeight() - rectHeight) / 2) + textSizeOffset;
        canvas.drawText(getContext().getString(R.string.mask_text), textLeft, textTop, mTextPaint);
    }

    @Override
    public boolean contains(Text text) {
        boolean contains = false;

        if (mMask != null) {
            Rect boundingBox = text.getBoundingBox();

            contains = mMask.contains(
                    mOverlay.translateX(boundingBox.left),
                    mOverlay.translateY(boundingBox.top),
                    mOverlay.translateX(boundingBox.right),
                    mOverlay.translateY(boundingBox.bottom)
            );
        }
        return contains;
    }
}
