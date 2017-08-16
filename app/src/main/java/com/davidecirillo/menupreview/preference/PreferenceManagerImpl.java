package com.davidecirillo.menupreview.preference;


import android.content.Context;
import android.support.annotation.Nullable;

import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class PreferenceManagerImpl implements PreferenceManager {

    private Context mContext;
    private Gson mGson;

    public PreferenceManagerImpl(Context context) {
        mContext = context;
        mGson = new Gson();
    }

    @Override
    public void saveSearchResults(SearchResultContainer searchResultContainer, String detectedText) {
        Prefs.savePreference(mContext, detectedText, mGson.toJson(searchResultContainer));
    }

    @Override
    @Nullable
    public SearchResultContainer getSearchResult(String detectedText) {
        String stringPreference = Prefs.getStringPreference(mContext, detectedText);
        if (stringPreference != null) {
            return mGson.fromJson(stringPreference, SearchResultContainer.class);
        }
        return null;
    }

    @Override
    public Map<String, SearchResultContainer> getSearchResultList() {
        Map<String, SearchResultContainer> map = new HashMap<>();
        Map<String, ?> all = Prefs.getAll(mContext);
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            map.put(entry.getKey(), mGson.fromJson(((String) entry.getValue()), SearchResultContainer.class));
        }
        return map;
    }
}
