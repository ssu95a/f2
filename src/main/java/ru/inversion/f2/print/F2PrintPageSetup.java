package ru.inversion.f2.print;

import ru.inversion.utils.Checks;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import java.awt.print.PageFormat;

public final class F2PrintPageSetup {

    private final PrintService printService;
    private final PrintRequestAttributeSet attributes;
    private final PageFormat pageFormat;
    private final MediaPrintableArea mediaPrintableArea;

    private final boolean matrixPrinter;
    private final boolean safeFallbackApplied;
    private final boolean verticalMarginsNormalized;

    private F2PrintPageSetup( Builder b )
    {
        this.printService = Checks.Require.object(b.printService, "printService");

        this.attributes =
                b.attributes == null
                        ? new HashPrintRequestAttributeSet()
                        : new HashPrintRequestAttributeSet(b.attributes);

        this.pageFormat =
                (PageFormat) Checks.Require
                        .object(b.pageFormat, "pageFormat")
                        .clone();

        this.mediaPrintableArea = b.mediaPrintableArea;

        this.matrixPrinter      = b.matrixPrinter;

        this.safeFallbackApplied = b.safeFallbackApplied;

        this.verticalMarginsNormalized = b.verticalMarginsNormalized;
    }

    public static Builder builder() {
        return new Builder();
    }

    public PrintService printService() {
        return printService;
    }

    public PrintRequestAttributeSet attributesCopy() {
        return new HashPrintRequestAttributeSet(attributes);
    }

    public PageFormat pageFormat() {
        return (PageFormat) pageFormat.clone();
    }

    public MediaPrintableArea mediaPrintableArea() {
        return mediaPrintableArea;
    }

    public boolean matrixPrinter() {
        return matrixPrinter;
    }

    public boolean safeFallbackApplied() {
        return safeFallbackApplied;
    }

    public boolean verticalMarginsNormalized() {
        return verticalMarginsNormalized;
    }

    public double pageWidthPt() {
        return pageFormat.getWidth();
    }

    public double pageHeightPt() {
        return pageFormat.getHeight();
    }

    public double imageableXPt() {
        return pageFormat.getImageableX();
    }

    public double imageableYPt() {
        return pageFormat.getImageableY();
    }

    public double imageableWidthPt() {
        return pageFormat.getImageableWidth();
    }

    public double imageableHeightPt() {
        return pageFormat.getImageableHeight();
    }

    public double imageableRightPt() {
        return imageableXPt() + imageableWidthPt();
    }

    public double imageableBottomPt() {
        return imageableYPt() + imageableHeightPt();
    }

    public int orientation() {
        return pageFormat.getOrientation();
    }

    public String geometryToString() {
        return "page=[" + pageWidthPt() + "x" + pageHeightPt() + "]"
                + ", imageable=[x=" + imageableXPt()
                + ", y=" + imageableYPt()
                + ", w=" + imageableWidthPt()
                + ", h=" + imageableHeightPt()
                + "]"
                + ", orientation=" + orientation();
    }

    public static final class Builder {

        private PrintService printService;
        private PrintRequestAttributeSet attributes;
        private PageFormat pageFormat;
        private MediaPrintableArea mediaPrintableArea;

        private boolean matrixPrinter;
        private boolean safeFallbackApplied;
        private boolean verticalMarginsNormalized;

        private Builder() {
        }

        public Builder printService(PrintService v) {
            this.printService = v;
            return this;
        }

        public Builder attributes(PrintRequestAttributeSet v) {
            this.attributes = v;
            return this;
        }

        public Builder pageFormat(PageFormat v) {
            this.pageFormat = v;
            return this;
        }

        public Builder mediaPrintableArea(MediaPrintableArea v) {
            this.mediaPrintableArea = v;
            return this;
        }

        public Builder matrixPrinter(boolean v) {
            this.matrixPrinter = v;
            return this;
        }

        public Builder safeFallbackApplied(boolean v) {
            this.safeFallbackApplied = v;
            return this;
        }

        public Builder verticalMarginsNormalized(boolean v) {
            this.verticalMarginsNormalized = v;
            return this;
        }

        public F2PrintPageSetup build() {
            return new F2PrintPageSetup(this);
        }
    }
}