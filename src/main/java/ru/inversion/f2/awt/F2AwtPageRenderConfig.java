package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.F2PrintPageSetup;

import java.awt.print.PageFormat;

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

    public static F2AwtPageRenderConfig fromPrintPageSetup(
            F2PrintPageSetup setup
    ) {
        return fromPrintPageSetup(
                setup,
                DEFAULT_DPI,
                false
        );
    }

    public static F2AwtPageRenderConfig fromPrintPageSetup(
            F2PrintPageSetup setup,
            F2StyledPage page
    ) {
        return fromPrintPageSetup(
                setup,
                page,
                DEFAULT_DPI,
                false
        );
    }

    public static F2AwtPageRenderConfig fromPrintPageSetup(
            F2PrintPageSetup setup,
            double dpi,
            boolean debugOverlay
    ) {
        if (setup == null)
            throw new IllegalArgumentException("setup is null");

        return new F2AwtPageRenderConfig(
                setup.pageWidthPt(),
                setup.pageHeightPt(),
                setup.imageableXPt(),
                setup.imageableYPt(),
                setup.imageableWidthPt(),
                setup.imageableHeightPt(),
                dpi,
                debugOverlay
        );
    }

    public static F2AwtPageRenderConfig fromPrintPageSetup(
            F2PrintPageSetup setup,
            F2StyledPage page,
            double dpi,
            boolean debugOverlay
    ) {
        F2AwtPageRenderConfig config = fromPrintPageSetup(
                setup,
                dpi,
                debugOverlay
        );

        if (page == null || !page.isLandscape())
            return config;

        return config.asLandscape();
    }

    private F2AwtPageRenderConfig asLandscape() {
        return new F2AwtPageRenderConfig(
                paperHeightPt,
                paperWidthPt,
                imageableYPt,
                imageableXPt,
                imageableHeightPt,
                imageableWidthPt,
                dpi,
                debugOverlay,
                contentScale,
                shrinkToFit
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

    private int ceilPtToPx(double pt) {
        return (int) Math.ceil(pt * scale());
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
                + ", contentScale=" + contentScale
                + ", shrinkToFit=" + shrinkToFit
                + '}';
    }
}
