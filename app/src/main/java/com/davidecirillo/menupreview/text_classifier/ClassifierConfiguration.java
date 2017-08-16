package com.davidecirillo.menupreview.text_classifier;

import com.google.gson.annotations.SerializedName;

public class ClassifierConfiguration {
    @SerializedName("negative_total")
    public int totalNegative;
    @SerializedName("positive_total")
    public int totalPositive;
    @SerializedName("pA")
    public float probA;
    @SerializedName("pNotA")
    public float probNotA;
    @SerializedName("num_words")
    public int numWords;


    @Override
    public String toString() {
        return "ClassifierConfiguration{" +
                "totalNegative=" + totalNegative +
                ", totalPositive=" + totalPositive +
                ", probA=" + probA +
                ", probNotA=" + probNotA +
                ", numWords=" + numWords +
                '}';
    }
}

