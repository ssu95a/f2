package ru.inversion.f2.control;

public final class F2ControlState implements F2ControlSink {

    private boolean pageEndRequested;
    private int lineFeedCount;

    @Override
    public void pageEnd() {
        pageEndRequested = true;
    }

    @Override
    public void lineFeed() {
        lineFeedCount++;
    }

    public boolean pageEndRequested() {
        return pageEndRequested;
    }

    public int lineFeedCount() {
        return lineFeedCount;
    }

    public boolean hasSignals() {
        return pageEndRequested || lineFeedCount > 0;
    }

    public void clear() {
        pageEndRequested = false;
        lineFeedCount = 0;
    }
}