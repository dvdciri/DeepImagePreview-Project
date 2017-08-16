package com.davidecirillo.menupreview.base;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.davidecirillo.menupreview.R;
import com.davidecirillo.menupreview.scan.ScanFragment;
import com.davidecirillo.menupreview.utils.bus.RxBus;
import com.davidecirillo.menupreview.utils.bus.events.PageChangedEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TabActivity extends FragmentActivity {

    public static final String TAG = TabActivity.class.getSimpleName();

    private int mOldPage;
    private int mCurrentPage;
    private CompositeDisposable mCompositeDisposable;
    private List<BackPressListener> mBackPressListeners;

    public static Intent getIntent(Context context) {
        return new Intent(context, TabActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        mBackPressListeners = new ArrayList<>();

        mOldPage = -1;
        mCurrentPage = 0;

        mCompositeDisposable = new CompositeDisposable();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        setViewPager(viewPager);
        setTabs(tabLayout, viewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerWithEventBusOnMainThread();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        unregisterFromEventBus();
    }

    @Override
    public void onBackPressed() {
        boolean handled = false;
        for (BackPressListener backPressListener : mBackPressListeners) {
            handled = backPressListener.onBackPressed();
            if (handled) {
                break;
            }
        }

        // If no one picked it up, then handle it
        if (!handled){
            super.onBackPressed();
        }
    }

    public void registerOnBackPressListener(BackPressListener backPressListener) {
        mBackPressListeners.add(backPressListener);
    }

    private void setTabs(TabLayout tabLayout, ViewPager viewPager) {
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera_white_24dp);
    }

    private void setViewPager(ViewPager viewPager) {
        final FragmentTabAdapter tabAdapter = new FragmentTabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new ScanFragment());
        viewPager.setAdapter(tabAdapter);

        // Avoid re-creating the camera fragment every time, it's too expensive and it's best to keep it alive when swiping
        viewPager.setOffscreenPageLimit(2); // Update this if needed

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected: " + position);

                mOldPage = mCurrentPage;
                mCurrentPage = position;

                PageChangedEvent event = new PageChangedEvent(mOldPage, mCurrentPage);
                event.setCurrentShownFragment(tabAdapter.getItem(position).getClass());
                RxBus.getInstance().postToEventBus(event);
            }
        });
    }

    private void registerWithEventBusOnMainThread() {
        Disposable disposable = RxBus.getInstance().toObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                });

        mCompositeDisposable.add(disposable);
    }

    private void unregisterFromEventBus() {
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
    }
}
