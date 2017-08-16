package com.davidecirillo.menupreview.utils.bus.events;


import com.davidecirillo.menupreview.scan.Status;

public class OcrStatusChangedEvent {

    private Status mOldStatus;
    private Status mNewStatus;

    public OcrStatusChangedEvent(Status oldStatus, Status newStatus) {
        mOldStatus = oldStatus;
        mNewStatus = newStatus;
    }

    public Status getOldStatus() {
        return mOldStatus;
    }

    public Status getNewStatus() {
        return mNewStatus;
    }

    public boolean isStartingEvent() {
        return mOldStatus == Status.OFF && mNewStatus == Status.DETECTING;
    }

    public boolean isStoppingEvent() {
        return (mOldStatus == Status.DETECTING || mOldStatus == Status.LOADING) && mNewStatus == Status.OFF;
    }

    public boolean isLoadingEvent() {
        return (mOldStatus == Status.DETECTING || mOldStatus == Status.OFF) && mNewStatus == Status.LOADING;
    }

    public boolean isLoadFinished() {
        return mOldStatus == Status.LOADING && mNewStatus == Status.OFF;
    }

    @Override
    public String toString() {
        return "OcrStatusChangedEvent{" +
                "mOldStatus=" + mOldStatus +
                ", mNewStatus=" + mNewStatus +
                '}';
    }
}
