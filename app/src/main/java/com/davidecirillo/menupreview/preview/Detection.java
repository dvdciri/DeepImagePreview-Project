package com.davidecirillo.menupreview.preview;


import android.support.annotation.Nullable;

import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;

public class Detection {

    private String mQuery;
    private SearchResultContainer mContainer;

    public Detection(String query) {
        mQuery = query;
    }

    public String getQuery() {
        return mQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Detection detection = (Detection) o;

        return getQuery() != null ? getQuery().equals(detection.getQuery()) : detection.getQuery() == null;

    }

    @Override
    public int hashCode() {
        int result = getQuery() != null ? getQuery().hashCode() : 0;
        result = 31 * result;
        return result;
    }

    public void setContainer(SearchResultContainer container) {
        mContainer = container;
    }

    @Nullable
    public SearchResultContainer getContainer() {
        return mContainer;
    }
}
