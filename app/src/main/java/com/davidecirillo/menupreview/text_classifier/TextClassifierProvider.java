package com.davidecirillo.menupreview.text_classifier;


import android.support.annotation.Nullable;

public interface TextClassifierProvider {

    @Nullable
    TextClassifier getClassifier();
}
