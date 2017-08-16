package com.davidecirillo.menupreview.scan;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.davidecirillo.menupreview.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class ScanPresenterTest {

    private ScanPresenter mCut;

    @Before
    public void setUp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        mCut = new ScanPresenter(mock(ScanView.class), appContext.getResources(), mock(PreferenceManager.class));
    }

    @Test
    public void whenInitThenTextClassifierNotNull() throws Exception {
        mCut.init();

        assertNotNull("Classifier should be not null", mCut.getClassifier());
    }
}