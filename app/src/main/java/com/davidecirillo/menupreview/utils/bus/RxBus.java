package com.davidecirillo.menupreview.utils.bus;


import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public final class RxBus {

    private static final String TAG = RxBus.class.getSimpleName();

    private final PublishSubject<Object> mBus = PublishSubject.create();
    private static RxBus sInstance;

    public static RxBus getInstance() {
        if (sInstance == null) {
            sInstance = new RxBus();
        }
        return sInstance;
    }

    public void postToEventBus(final Object event) {
        mBus.onNext(event);
    }

    public Observable<Object> toObservable() {
        return mBus;
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }
}