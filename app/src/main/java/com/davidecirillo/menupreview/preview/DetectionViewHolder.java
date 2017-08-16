package com.davidecirillo.menupreview.preview;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidecirillo.menupreview.R;
import com.davidecirillo.menupreview.utils.ImageLoaderHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class DetectionViewHolder extends RecyclerView.ViewHolder {

    private final ImageView mImageView;
    private TextView mTextView;

    public DetectionViewHolder(View itemView) {
        super(itemView);
        mTextView = ((TextView) itemView.findViewById(R.id.text));
        mImageView = ((ImageView) itemView.findViewById(R.id.preview));
    }

    public void bind(final Detection detection, final DetectionViewAdapter.Listener listener) {
        mTextView.setText(detection.getQuery());


        GradientDrawable drawable = (GradientDrawable) itemView.getBackground();
        if (detection.getContainer() != null) {
            drawable.setColor(Color.parseColor(detection.getContainer().getColor()));

            ImageLoaderHelper.loadImagePreviewFromCache(itemView.getContext(), detection.getContainer(), 80,
                    new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mImageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        } else {
            drawable.setColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
            mImageView.setImageBitmap(null);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClicked(detection.getQuery());
            }
        });
    }
}
