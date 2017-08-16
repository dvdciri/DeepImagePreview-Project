package com.davidecirillo.menupreview.searchengine;


import com.davidecirillo.menupreview.BuildConfig;
import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchApi {

    private static final String GOOGLE_API_BASE_URL = "https://www.googleapis.com";

    private static SearchApi sInstance;

    private SearchClient mSearchClient;

    public static SearchApi getsInstance() {
        if (sInstance == null) {
            sInstance = new SearchApi();
        }

        return sInstance;
    }

    private SearchApi() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        mSearchClient = new Retrofit.Builder()
                .baseUrl(GOOGLE_API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(SearchClient.class);
    }

    public Observable<SearchResultContainer> doImageSearch(String query) {
        return mSearchClient.doImageSearch(
                query,
                1,
                "medium",
                "image",
                BuildConfig.GCE_API_KEY,
                BuildConfig.MP_ENGINE_ID
        );
    }
}
