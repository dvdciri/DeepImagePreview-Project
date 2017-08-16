package com.davidecirillo.menupreview.utils.bus.events;


import com.davidecirillo.menupreview.preview.Detection;

public class NewDetectionFoundEvent {

    Detection mDetection;

    public NewDetectionFoundEvent(Detection detection) {
        mDetection = detection;
    }

    public Detection getDetection() {
        return mDetection;
    }
}
