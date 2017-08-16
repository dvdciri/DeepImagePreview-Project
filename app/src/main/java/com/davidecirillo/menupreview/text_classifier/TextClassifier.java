package com.davidecirillo.menupreview.text_classifier;

import android.util.Log;

import java.util.Map;
import java.util.Set;

public class TextClassifier {

    private static final float ALPHA = 0.0f;
    private static final String TAG = TextClassifier.class.getSimpleName();

    private final ClassifierConfiguration conf;
    private final Map<String, Integer> positiveWords;
    private final Map<String, Integer> negativeWords;
    private final Set<String> stopWords;

    public TextClassifier(ClassifierConfiguration conf,
                          Map<String, Integer> positiveWords,
                          Map<String, Integer> negativeWords,
                          Set<String> stopWords) {

        this.conf = conf;
        this.positiveWords = positiveWords;
        this.negativeWords = negativeWords;
        this.stopWords = stopWords;
    }

    public boolean isFoodRelated(String text) {
        String cleanText = cleanText(text);

        float probTextFood = conf.probA * conditionalText(cleanText, true);
        float probTextNotFood = conf.probNotA * conditionalText(cleanText, false);
        Log.d(TAG, "\t\tprob Food => " + probTextFood);
        Log.d(TAG, "\t\tprob Not Food => " + probTextNotFood);
        return probTextFood > probTextNotFood;
    }

    private String cleanText(String text) {
        // Remove stopwords
        String[] words = text.split("\\s+");
        StringBuffer sb = new StringBuffer();
        for (String token : words) {
            String word = token.toLowerCase();
            if (!stopWords.contains(word) && isWordInVocab(word)) {
                sb.append(word).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private boolean isWordInVocab(String word) {
        return positiveWords.get(word) != null || negativeWords.get(word) != null;
    }

    private float conditionalText(String text, boolean isFood) {
        float result = 1.0f;
        String[] words = text.split("\\s+");
        for (String word : words) {
            result *= conditionalWord(word, isFood);
        }
        return result;
    }

    // TODO So good to refactor, eh...
    private float conditionalWord(String word, boolean isFood) {
        float result;
        Integer valueOfWord;
        if (isFood) {
            valueOfWord = positiveWords.get(word);
            if (valueOfWord == null) {
                valueOfWord = 0;
            }
            result = (valueOfWord + ALPHA) / (conf.totalPositive + ALPHA * conf.numWords);
        } else {
            valueOfWord = negativeWords.get(word);
            if (valueOfWord == null) {
                valueOfWord = 0;
            }
            result = (valueOfWord + ALPHA) / (conf.totalNegative + ALPHA * conf.numWords);
        }
        return result;
    }
}
