package com.davidecirillo.menupreview.utils;


import android.content.Context;

import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public final class ImageLoaderHelper {

    public static void loadImagePreviewFromCache(Context context, SearchResultContainer container, int imageSize, Target target){
        Picasso.with(context)
                .load(container.getFirstThumbnailLink())
                .resize(imageSize, imageSize)
                .transform(new CircleTransform())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(target);
    }
}
