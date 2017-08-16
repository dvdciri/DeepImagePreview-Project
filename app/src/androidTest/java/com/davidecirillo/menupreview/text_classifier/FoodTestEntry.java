package com.davidecirillo.menupreview.text_classifier;


import com.google.gson.annotations.SerializedName;

public class FoodTestEntry {

    @SerializedName("text")
    private String mText;

    @SerializedName("is_food")
    private boolean mIsFood;

    public String getText() {
        return mText;
    }

    public boolean isFood() {
        return mIsFood;
    }
}
