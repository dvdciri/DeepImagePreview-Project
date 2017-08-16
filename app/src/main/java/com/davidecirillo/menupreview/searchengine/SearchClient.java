package com.davidecirillo.menupreview.searchengine;


import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchClient {

    @GET("/customsearch/v1")
    Observable<SearchResultContainer> doImageSearch(
            @Query("q") String q,
            @Query("start") int start,
            @Query("imgSize") String imgSize,
            @Query("searchType") String searchType,
            @Query("key") String key,
            @Query("cx") String cx
    );
}
