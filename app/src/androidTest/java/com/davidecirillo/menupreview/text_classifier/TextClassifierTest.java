package com.davidecirillo.menupreview.text_classifier;

import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.fail;

public class TextClassifierTest {

    private TextClassifier mCut;

    @Before
    public void setUp() throws Exception {
        mCut = createTextClassifier(InstrumentationRegistry.getTargetContext().getResources());
    }

    @Test
    public void testGivenSampleDataWhenPredictThenCorrectResultReturned() throws Exception {

        // Load test data
        Type type = new TypeToken<ArrayList<FoodTestEntry>>() {
        }.getType();
        Resources resources = InstrumentationRegistry.getContext().getResources();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resources.getAssets().open("sample_classifier_test_data.json")));
        ArrayList<FoodTestEntry> foodTestEntries = new Gson().fromJson(bufferedReader, type);

        int failed = 0;

        // Predict and test
        for (FoodTestEntry foodTestEntry : foodTestEntries) {
            String textToPredict = foodTestEntry.getText();
            boolean isPredictionCorrect = mCut.isFoodRelated(textToPredict) == foodTestEntry.isFood();

            if (isPredictionCorrect) {
                System.out.println("SUCCESS Predicted [" + textToPredict + "] correctly!");
            } else {
                failed++;
                System.out.println("FAILED Predicted [" + textToPredict + "], expecting [" + foodTestEntry.isFood() + "] but was [" + mCut
                        .isFoodRelated(textToPredict) + "]");
            }
        }

        if (failed > 0) {

            int totalTestSize = foodTestEntries.size();
            float failedPerc = (float) failed * totalTestSize / 100f;

            fail(failed + " out of " + totalTestSize + " failed [" + failedPerc + "%]");
        }
    }

    private TextClassifier createTextClassifier(Resources resources) {
        try {
            Gson gson = new Gson();
            BufferedReader reader = new BufferedReader(new InputStreamReader(resources.getAssets().open("aux_data.json")));

            ClassifierConfiguration configuration = gson.fromJson(reader, ClassifierConfiguration.class);

            reader = new BufferedReader(new InputStreamReader(resources.getAssets().open("train_positive.json")));
            Type type = new TypeToken<HashMap<String, Integer>>() {
            }.getType();
            Map<String, Integer> positiveWords = gson.fromJson(reader, type);
            reader = new BufferedReader(new InputStreamReader(resources.getAssets().open("train_negative.json")));
            Map<String, Integer> negativeWords = gson.fromJson(reader, type);

            Set<String> stopWords = new HashSet<>();
            reader = new BufferedReader(new InputStreamReader(resources.getAssets().open("stop_words.txt")));
            String line = reader.readLine();
            while (line != null) {
                stopWords.add(line);
                line = reader.readLine();
            }

            return new TextClassifier(configuration, positiveWords, negativeWords, stopWords);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}