package com.davidecirillo.menupreview.scan;


public enum Status {
    DETECTING("Detecting text.."),
    LOADING("Loading images.."),
    OFF("Press the button to scan!");

    private String mDescription;

    Status(String description) {
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }
}
