package com.davidecirillo.menupreview.utils.bus.events;


public class PageChangedEvent {

    private int mOldPage;
    private int mNewPage;
    private Class mCurrentShownFragment;

    public PageChangedEvent(int oldPage, int newPage) {
        mOldPage = oldPage;
        mNewPage = newPage;
    }

    public int getNewPage() {
        return mNewPage;
    }

    public int getOldPage() {
        return mOldPage;
    }

    public Class getCurrentShownFragment() {
        return mCurrentShownFragment;
    }

    public void setCurrentShownFragment(Class currentShownFragment) {
        mCurrentShownFragment = currentShownFragment;
    }

    @Override
    public String toString() {
        return "PageChangedEvent{" +
                "mOldPage=" + mOldPage +
                ", mNewPage=" + mNewPage +
                '}';
    }
}
