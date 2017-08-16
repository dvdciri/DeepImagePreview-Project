package com.davidecirillo.menupreview;

import android.app.Application;
import android.util.Log;

import com.davidecirillo.menupreview.utils.bus.RxBus;
import com.squareup.picasso.Picasso;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MenuApplication extends Application {

    private static MenuApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        Picasso picasso = new Picasso.Builder(this)
                .loggingEnabled(BuildConfig.DEBUG)
                .indicatorsEnabled(false)
                .build();
        Picasso.setSingletonInstance(picasso);


        RxBus.getInstance()
                .toObservable()
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Log.d("EVENT", "onNewEvent: " + o.getClass().getSimpleName() + " [" + o.toString() + "]");
                    }
                });
    }
}
