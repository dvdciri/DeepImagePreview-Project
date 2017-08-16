package com.davidecirillo.menupreview.scan;


import com.davidecirillo.menupreview.preview.Detection;
import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.davidecirillo.menupreview.utils.bus.events.OcrStatusChangedEvent;

public interface ScanView {

    void handleOrcStatusChangedEvent(OcrStatusChangedEvent statusChangedEvent);

    void handleNewDetectionFound(Detection detection);

    void showPreviewResults(SearchResultContainer container, String query);

    void setLoading(boolean loading);
}
