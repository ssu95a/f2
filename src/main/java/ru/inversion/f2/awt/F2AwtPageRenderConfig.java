package ru.inversion.f2.awt;

import java.awt.print.PageFormat;
import java.awt.print.Paper;

public final class F2AwtPageRenderConfig {

    private static final double DEFAULT_DPI = 144.0d;

    private final double paperWidthPt;
    private final double paperHeightPt;

    private final double imageableXPt;
    private final double imageableYPt;
    private final double imageableWidthPt;
    private final double imageableHeightPt;

    private final double dpi;

    private final boolean debugOverlay;

    private final double contentScale;
    private final boolean shrinkToFit;

    private F2AwtPageRenderConfig(
            double paperWidthPt,
            double paperHeightPt,
            double imageableXPt,
            double imageableYPt,
            double imageableWidthPt,
            double imageableHeightPt,
            double dpi,
            boolean debugOverlay,
            double contentScale,
            boolean shrinkToFit
    ) {

        if( paperWidthPt <= 0.0d )
            throw new IllegalArgumentException("paperWidthPt <= 0");

        if( paperHeightPt <= 0.0d )
            throw new IllegalArgumentException("paperHeightPt <= 0");

        if( imageableWidthPt <= 0.0d )
            throw new IllegalArgumentException("imageableWidthPt <= 0");

        if( imageableHeightPt <= 0.0d )
            throw new IllegalArgumentException("imageableHeightPt <= 0");

        if( dpi <= 0.0d )
            throw new IllegalArgumentException("dpi <= 0");

        if( contentScale <= 0.0d )
            throw new IllegalArgumentException("contentScale <= 0");

        this.paperWidthPt = paperWidthPt;
        this.paperHeightPt = paperHeightPt;
        this.imageableXPt = imageableXPt;
        this.imageableYPt = imageableYPt;
        this.imageableWidthPt = imageableWidthPt;
        this.imageableHeightPt = imageableHeightPt;
        this.dpi = dpi;
        this.debugOverlay = debugOverlay;
        this.contentScale = contentScale;
        this.shrinkToFit = shrinkToFit;
    }

    public F2AwtPageRenderConfig(
            double paperWidthPt,
            double paperHeightPt,
            double imageableXPt,
            double imageableYPt,
            double imageableWidthPt,
            double imageableHeightPt,
            double dpi,
            boolean debugOverlay
    )
    {
        this(
                paperWidthPt,
                paperHeightPt,
                imageableXPt,
                imageableYPt,
                imageableWidthPt,
                imageableHeightPt,
                dpi,
                debugOverlay,
                1.0d,
                false
        );
    }

    public double contentScale() {
        return contentScale;
    }

    public boolean shrinkToFit() {
        return shrinkToFit;
    }

    public F2AwtPageRenderConfig withContentScale(double value) {
        return new F2AwtPageRenderConfig(
                paperWidthPt,
                paperHeightPt,
                imageableXPt,
                imageableYPt,
                imageableWidthPt,
                imageableHeightPt,
                dpi,
                debugOverlay,
                value,
                shrinkToFit
        );
    }

    public F2AwtPageRenderConfig withShrinkToFit(boolean value) {
        return new F2AwtPageRenderConfig(
                paperWidthPt,
                paperHeightPt,
                imageableXPt,
                imageableYPt,
                imageableWidthPt,
                imageableHeightPt,
                dpi,
                debugOverlay,
                contentScale,
                value
        );
    }
    public static F2AwtPageRenderConfig a4Portrait() {
        /*
         * A4: 210 x 297 mm.
         * 1 inch = 25.4 mm.
         * 1 pt = 1/72 inch.
         */
        double widthPt = mmToPt(210.0d);
        double heightPt = mmToPt(297.0d);

        double marginPt = mmToPt(5.0d);

        return new F2AwtPageRenderConfig(
                widthPt,
                heightPt,
                marginPt,
                marginPt,
                widthPt - marginPt * 2.0d,
                heightPt - marginPt * 2.0d,
                DEFAULT_DPI,
                false
        );
    }

    public static F2AwtPageRenderConfig fromPageFormat(
            PageFormat pageFormat,
            double dpi,
            boolean debugOverlay
    ) {
        if (pageFormat == null)
            throw new IllegalArgumentException("pageFormat is null");

        return new F2AwtPageRenderConfig(
                pageFormat.getWidth(),
                pageFormat.getHeight(),
                pageFormat.getImageableX(),
                pageFormat.getImageableY(),
                pageFormat.getImageableWidth(),
                pageFormat.getImageableHeight(),
                dpi,
                debugOverlay
        );
    }

    public static F2AwtPageRenderConfig fromPaper(
            Paper paper,
            double dpi,
            boolean debugOverlay
    ) {
        if (paper == null)
            throw new IllegalArgumentException("paper is null");

        return new F2AwtPageRenderConfig(
                paper.getWidth(),
                paper.getHeight(),
                paper.getImageableX(),
                paper.getImageableY(),
                paper.getImageableWidth(),
                paper.getImageableHeight(),
                dpi,
                debugOverlay
        );
    }

    public F2AwtPageRenderConfig withDpi(double value) {
        return new F2AwtPageRenderConfig(
                paperWidthPt,
                paperHeightPt,
                imageableXPt,
                imageableYPt,
                imageableWidthPt,
                imageableHeightPt,
                value,
                debugOverlay,
                contentScale,
                shrinkToFit
        );
    }

    public F2AwtPageRenderConfig withDebugOverlay(boolean value) {
        return new F2AwtPageRenderConfig(
                paperWidthPt,
                paperHeightPt,
                imageableXPt,
                imageableYPt,
                imageableWidthPt,
                imageableHeightPt,
                dpi,
                value,
                contentScale,
                shrinkToFit
        );
    }

    public double paperWidthPt() {
        return paperWidthPt;
    }

    public double paperHeightPt() {
        return paperHeightPt;
    }

    public double imageableXPt() {
        return imageableXPt;
    }

    public double imageableYPt() {
        return imageableYPt;
    }

    public double imageableWidthPt() {
        return imageableWidthPt;
    }

    public double imageableHeightPt() {
        return imageableHeightPt;
    }

    public double dpi() {
        return dpi;
    }

    public boolean debugOverlay() {
        return debugOverlay;
    }

    public double scale() {
        return dpi / 72.0d;
    }

    public int imageWidthPx() {
        return ceilPtToPx(paperWidthPt);
    }

    public int imageHeightPx() {
        return ceilPtToPx(paperHeightPt);
    }

    public int ptToPx(double pt) {
        return (int) Math.round(pt * scale());
    }

    public double pxToPt(int px) {
        return ((double) px) / scale();
    }

    public PageFormat toPageFormat() {
        Paper paper = new Paper();

        paper.setSize(paperWidthPt, paperHeightPt);
        paper.setImageableArea(
                imageableXPt,
                imageableYPt,
                imageableWidthPt,
                imageableHeightPt
        );

        PageFormat pf = new PageFormat();
        pf.setPaper(paper);

        return pf;
    }

    private int ceilPtToPx(double pt) {
        return (int) Math.ceil(pt * scale());
    }

    private static double mmToPt(double mm) {
        return mm * 72.0d / 25.4d;
    }

    @Override
    public String toString() {
        return "F2AwtPageRenderConfig{"
                + "paperWidthPt=" + paperWidthPt
                + ", paperHeightPt=" + paperHeightPt
                + ", imageableXPt=" + imageableXPt
                + ", imageableYPt=" + imageableYPt
                + ", imageableWidthPt=" + imageableWidthPt
                + ", imageableHeightPt=" + imageableHeightPt
                + ", dpi=" + dpi
                + ", debugOverlay=" + debugOverlay
                + '}';
    }
}