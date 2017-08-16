package com.davidecirillo.menupreview.utils.bus.events;


import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;

public class SearchResultReadyEvent {

    SearchResultContainer mSearchResultContainer;
    String query;

    public SearchResultReadyEvent(SearchResultContainer searchResultContainer, String query) {
        mSearchResultContainer = searchResultContainer;
        this.query = query;
    }

    public SearchResultContainer getSearchResultContainer() {
        return mSearchResultContainer;
    }

    public String getQuery() {
        return query;
    }
}
