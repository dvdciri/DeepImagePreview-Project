package com.davidecirillo.menupreview.preference;


import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;

import java.util.Map;

public interface PreferenceManager {

    void saveSearchResults(SearchResultContainer searchResultContainer, String detectedText);

    SearchResultContainer getSearchResult(String detectedText);

    /**
     * The search results returned by this method are not ordered
     */
    Map<String, SearchResultContainer> getSearchResultList();
}
