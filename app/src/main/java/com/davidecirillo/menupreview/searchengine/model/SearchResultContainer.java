package com.davidecirillo.menupreview.searchengine.model;


import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SearchResultContainer {

    private List<SearchItem> items;
    private String mColor;
    private long mCreatedAt;
    private String mQuery;

    public SearchResultContainer(List<SearchItem> items) {
        this.items = items;
    }

    public SearchResultContainer() {
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String color) {
        mColor = color;
    }

    public void setCreatedAt(long timestamp){
        mCreatedAt = timestamp;
    }

    public Long getCreatedAt() {
        return mCreatedAt;
    }

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    public ArrayList<ItemImage> getImages() {
        ArrayList<ItemImage> result = new ArrayList<>();
        for (SearchItem searchItem : items) {
            result.add(searchItem.getImage());
        }
        return result;
    }

    @Nullable
    public String getFirstThumbnailLink() {
        String result = null;
        if (!getImages().isEmpty()) {
            result = getImages().get(0).getThumbnailLink();
        }
        return result;
    }

    @Override
    public String toString() {
        return "SearchResultContainer{" +
                "items=" + items +
                ", mColor='" + mColor + '\'' +
                ", mCreatedAt=" + mCreatedAt +
                ", mQuery='" + mQuery + '\'' +
                '}';
    }

    public ArrayList<String> toImageLinkList() {
        ArrayList<String> result = new ArrayList<>();
        for (ItemImage itemImage : getImages()) {
            result.add(itemImage.getThumbnailLink());
        }
        return result;
    }
}
