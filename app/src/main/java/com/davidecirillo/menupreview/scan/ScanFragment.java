package com.davidecirillo.menupreview.scan;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.davidecirillo.menupreview.BaseFragment;
import com.davidecirillo.menupreview.R;
import com.davidecirillo.menupreview.base.BackPressListener;
import com.davidecirillo.menupreview.base.TabActivity;
import com.davidecirillo.menupreview.ocr.camera.CameraSource;
import com.davidecirillo.menupreview.ocr.camera.CameraSourcePreview;
import com.davidecirillo.menupreview.ocr.graphic.GraphicOverlay;
import com.davidecirillo.menupreview.ocr.graphic.MaskView;
import com.davidecirillo.menupreview.ocr.graphic.OcrDetectorProcessor;
import com.davidecirillo.menupreview.preview.Detection;
import com.davidecirillo.menupreview.preview.DetectionViewAdapter;
import com.davidecirillo.menupreview.searchengine.SearchResultHandler;
import com.davidecirillo.menupreview.searchengine.model.HeaderItem;
import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.davidecirillo.menupreview.utils.bus.events.OcrStatusChangedEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextRecognizer;
import com.mzelzoghbi.zgallery.ZGallery;
import com.mzelzoghbi.zgallery.entities.ZColor;

import java.io.IOException;
import java.util.ArrayList;

public class ScanFragment extends BaseFragment implements DetectionViewAdapter.Listener, ScanView, SearchResultAdapter.Callbacks, BackPressListener {

    private static final String TAG = ScanFragment.class.getSimpleName();

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<GraphicOverlay.Graphic> mOcrGraphicOverlay;
    private MaskView mMaskView;

    // Helper objects for detecting taps and pinches.
//    private GestureDetector mGestureDetector;

    // Bottom sheet recycler view
    private BottomSheetBehavior<RecyclerView> mBottomSheetBehavior;

    private SearchResultAdapter mResultAdapter;

    private View mDetectingRedDot;
    private FloatingActionButton mFab;
    private Animation mBlinkAnimation;
    private TextView mStatusText;
    private View mBlackScrim;
    private View mLoadingView;
    private View mBottomBar;
    private RecyclerView mPreviewRecyclerView;

    private ScanPresenter mScanPresenter;
    private DetectionViewAdapter mDetectionViewAdapter;

    public ScanFragment() {
        // Empty
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScanPresenter = new ScanPresenter(this, getResources(), mPreferenceManager);
        mScanPresenter.init();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        mPreview = (CameraSourcePreview) view.findViewById(R.id.preview);
        mOcrGraphicOverlay = (GraphicOverlay<GraphicOverlay.Graphic>) view.findViewById(R.id.ocr_graphic_overlay);
        mMaskView = (MaskView) view.findViewById(R.id.mask_overlay);
        mDetectingRedDot = view.findViewById(R.id.red_dot);
        mStatusText = (TextView) view.findViewById(R.id.status_text);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab_button);
        mBlackScrim = view.findViewById(R.id.scrim);
        mLoadingView = view.findViewById(R.id.loading_view);
        mBottomBar = view.findViewById(R.id.bottom_bar);

        mMaskView.setOverlay(mOcrGraphicOverlay);

        setUpBottomSheetRecyclerView(view);
        setUpPreviewRecyclerView(view);

//        view.setOnTouchListener(this);
        mFab.setOnClickListener(mScanPresenter.getStartProcessorClickListener());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mGestureDetector = new GestureDetector(getContext(), new OcrCaptureGestureListener(mOcrGraphicOverlay, this));

        loadAnimations();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(getActivity() != null){
            ((TabActivity) getActivity()).registerOnBackPressListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScanPresenter.onResume();

        createCameraSource();
        startCameraSource();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }

        mScanPresenter.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBlinkAnimation = null;
//        mGestureDetector = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
        mScanPresenter.onDestroy();
        mResultAdapter = null;
    }

    /*@Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d(TAG, "onTouch: " + motionEvent);
        return mGestureDetector.onTouchEvent(motionEvent);
    }*/

    /* Image Adapter callbacks */
    @Override
    public void onImageClick(String text, ArrayList<String> imageList, int selectedPosition) {
        ZGallery.with(getActivity(), imageList)
                .setToolbarTitleColor(ZColor.WHITE)
                .setGalleryBackgroundColor(ZColor.WHITE)
                .setToolbarColorResId(R.color.colorPrimary)
                .setTitle(text)
                .setSelectedImgPosition(selectedPosition)
                .show();
    }

    @Override
    public void handleOrcStatusChangedEvent(OcrStatusChangedEvent statusChangedEvent) {
        Log.d(TAG, "onStatusChanged: status=" + statusChangedEvent);

        Status newStatus = statusChangedEvent.getNewStatus();

        boolean shouldAnimate = true;

        mStatusText.setText(newStatus.getDescription());

        int nextIcon = 0;
        View.OnClickListener clickListener = null;

        if (statusChangedEvent.isStoppingEvent()) {
            Log.d(TAG, "handleOrcStatusChangedEvent: isStoppingEvent");
            mDetectingRedDot.setVisibility(View.INVISIBLE);
            mDetectingRedDot.clearAnimation();

            nextIcon = R.drawable.ic_filter_center_focus_white_24dp;
            clickListener = mScanPresenter.getStartProcessorClickListener();

        } else if (statusChangedEvent.isStartingEvent()) {
            Log.d(TAG, "handleOrcStatusChangedEvent: isStartingEvent");
            mDetectingRedDot.setAlpha(1.0f);
            mDetectingRedDot.startAnimation(mBlinkAnimation);

            nextIcon = R.drawable.ic_stop_white_24dp;
            clickListener = mScanPresenter.getStopProcessorClickListener();

        } else if (newStatus == Status.LOADING) {
            Log.d(TAG, "handleOrcStatusChangedEvent: LOADING");
            nextIcon = R.drawable.ic_clear_white_24dp;
            clickListener = mScanPresenter.getStopLoadingPreviewClickListener();
            shouldAnimate = false;
        }

        if (nextIcon != 0) {
            final int finalNextIcon = nextIcon;
            FloatingActionButton.OnVisibilityChangedListener listener = new FloatingActionButton
                    .OnVisibilityChangedListener() {

                @Override
                public void onHidden(final FloatingActionButton fab) {
                    super.onHidden(fab);
                    fab.setImageDrawable(ContextCompat.getDrawable(getContext(), finalNextIcon));
                    fab.show();
                }
            };

            if (clickListener != null) {
                mFab.setOnClickListener(clickListener);
            }

            if (mFab.isShown() && shouldAnimate) {
                mFab.hide(listener);
            } else {
                listener.onHidden(mFab);
            }
        }
    }

    @Override
    public void handleNewDetectionFound(Detection detection) {
        mDetectionViewAdapter.addDetection(detection);
        mPreviewRecyclerView.getLayoutManager().scrollToPosition(mDetectionViewAdapter.getItemCount() - 1);
    }

    @Override
    public void showPreviewResults(SearchResultContainer container, String query) {
        refreshPreviewListWithNewResult(container, query);
        setLoading(false);

        // Expand bottom sheet and show results
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        if (mResultAdapter != null) {
            mResultAdapter.showResults(container, new HeaderItem(query));
        }
    }

    @Override
    public void setLoading(boolean loading) {
        mLoadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClicked(String query) {
        mScanPresenter.loadOrShowResults(query);
    }

    @Override
    public boolean onBackPressed() {
        boolean handled = false;
        if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            handled = true;
        }
        return handled;
    }

    /* PRIVATE METHODS */
    private void refreshPreviewListWithNewResult(SearchResultContainer container, String query) {
        Detection detection = new Detection(query);
        detection.setContainer(container);
        handleNewDetectionFound(detection);
    }

    private void setUpBottomSheetRecyclerView(View view) {
        // Bottom sheet recycler view and adapter
        mResultAdapter = new SearchResultAdapter(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bottom_sheet_recycler_view);

        mBottomSheetBehavior = BottomSheetBehavior.from(recyclerView);
        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mScanPresenter.onBottomSheetHided();
                    mBlackScrim.setAlpha(0f);
                } else {
                    mScanPresenter.onBottomSheetShown();
                    mBlackScrim.setAlpha(0.7f);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(mResultAdapter);
    }

    private void setUpPreviewRecyclerView(View view) {
        mDetectionViewAdapter = new DetectionViewAdapter(this);

        mPreviewRecyclerView = (RecyclerView) view.findViewById(R.id.preview_list);
        mPreviewRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        layout.setStackFromEnd(true);
        mPreviewRecyclerView.setLayoutManager(layout);
        mPreviewRecyclerView.setAdapter(mDetectionViewAdapter);
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, 0);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mOcrGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void createCameraSource() {

        // Create the TextRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getContext()).build();
        SearchResultHandler searchResultHandler = new SearchResultHandler(getContext(), mPreferenceManager, getResources().getStringArray(R.array.pin_colors));

        // Set the TextRecognizer's Processor.
        OcrDetectorProcessor ocrDetectorProcessor = new OcrDetectorProcessor(
                mOcrGraphicOverlay,
                mMaskView,
                mPreferenceManager,
                searchResultHandler,
                mScanPresenter);
        textRecognizer.setProcessor(ocrDetectorProcessor);
        mScanPresenter.setProcessor(ocrDetectorProcessor);

        // Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = getActivity().registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(getContext(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Create the mCameraSource using the TextRecognizer.
        mCameraSource = new CameraSource.Builder(getContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(15.0f)
//                .setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .build();
    }

    private void loadAnimations() {
        mBlinkAnimation = new AlphaAnimation(0.0f, 1.0f);
        mBlinkAnimation.setDuration(500);
        mBlinkAnimation.setRepeatMode(Animation.REVERSE);
        mBlinkAnimation.setRepeatCount(Animation.INFINITE);
    }

    // TODO: 19/07/2017 Show spinner somewhere while loading the results 
    // TODO: 19/07/2017 Create color when detecting and then use it in the list 
    // TODO: 19/07/2017
}
