package ru.inversion.f2.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.utils.Checks;

import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.lang.invoke.MethodHandles;

public final class F2PrintPageSetupResolver {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final double SAFE_FALLBACK_MARGIN_PT = 5.0d * 72.0d / 25.4d;

    private static final double EPS_PT = 0.01d;

    public F2PrintPageSetup resolve( F2PrintSettings settings, boolean matrixPrinter ) throws Exception
    {
        Checks.Require.object(settings, "settings");

        PrintService printService = Checks.Require.object( settings.printService(), "settings.printService" );

        PrintRequestAttributeSet attrs =
                settings.attributesCopy();

        PrinterJob job =
                PrinterJob.getPrinterJob();

        job.setPrintService(printService);

        PageFormat pageFormat =
                job.getPageFormat(attrs);

        pageFormat =
                job.validatePage(pageFormat);

        PageFormatFallbackResult fallbackResult =
                applySafePageFormatFallbackIfNeeded(pageFormat);

        pageFormat =
                fallbackResult.pageFormat;

        PageFormatNormalizeResult normalizeResult =
                normalizeVerticalMargins(pageFormat);

        pageFormat =
                normalizeResult.pageFormat;

        MediaPrintableArea mediaPrintableArea =
                (MediaPrintableArea) attrs.get(MediaPrintableArea.class);

        F2PrintPageSetup setup =
                F2PrintPageSetup.builder()
                        .printService(printService)
                        .attributes(attrs)
                        .pageFormat(pageFormat)
                        .mediaPrintableArea(mediaPrintableArea)
                        .matrixPrinter(matrixPrinter)
                        .safeFallbackApplied(fallbackResult.applied)
                        .verticalMarginsNormalized(normalizeResult.normalized)
                        .build();

        log.info(
                "F2 resolved print page setup: printer={}, {}, matrix={}, safeFallbackApplied={}, verticalMarginsNormalized={}",
                printService.getName(),
                setup.geometryToString(),
                Boolean.valueOf(setup.matrixPrinter()),
                Boolean.valueOf(setup.safeFallbackApplied()),
                Boolean.valueOf(setup.verticalMarginsNormalized())
        );

        return setup;
    }

    private PageFormatFallbackResult applySafePageFormatFallbackIfNeeded(
            PageFormat pf
    ) {
        if (pf == null)
            return new PageFormatFallbackResult(null, false);

        if (!needsSafePageFormatFallback(pf))
            return new PageFormatFallbackResult(pf, false);

        Paper oldPaper =
                pf.getPaper();

        if (oldPaper == null)
            return new PageFormatFallbackResult(pf, false);

        double paperW = oldPaper.getWidth();
        double paperH = oldPaper.getHeight();

        if (paperW <= 0.0d || paperH <= 0.0d)
            return new PageFormatFallbackResult(pf, false);

        double imageableW =
                paperW - SAFE_FALLBACK_MARGIN_PT - SAFE_FALLBACK_MARGIN_PT;

        double imageableH =
                paperH - SAFE_FALLBACK_MARGIN_PT - SAFE_FALLBACK_MARGIN_PT;

        if (imageableW <= 0.0d || imageableH <= 0.0d)
            return new PageFormatFallbackResult(pf, false);

        Paper newPaper =
                new Paper();

        newPaper.setSize(
                paperW,
                paperH
        );

        newPaper.setImageableArea(
                SAFE_FALLBACK_MARGIN_PT,
                SAFE_FALLBACK_MARGIN_PT,
                imageableW,
                imageableH
        );

        PageFormat copy =
                (PageFormat) pf.clone();

        copy.setPaper(newPaper);

        return new PageFormatFallbackResult(copy, true);
    }

    private boolean needsSafePageFormatFallback(PageFormat pf) {

        return pf.getImageableX() <= EPS_PT
                || pf.getImageableY() <= EPS_PT;
    }

    private PageFormatNormalizeResult normalizeVerticalMargins(PageFormat pf) {

        if (pf == null)
            return new PageFormatNormalizeResult(null, false);

        Paper oldPaper =
                pf.getPaper();

        if (oldPaper == null)
            return new PageFormatNormalizeResult(pf, false);

        double paperW = oldPaper.getWidth();
        double paperH = oldPaper.getHeight();

        double left =
                oldPaper.getImageableX();

        double top =
                oldPaper.getImageableY();

        double bottom =
                paperH
                        - oldPaper.getImageableY()
                        - oldPaper.getImageableHeight();

        double safeVertical =
                Math.max(top, bottom);

        safeVertical =
                Math.max(safeVertical, SAFE_FALLBACK_MARGIN_PT);

        double imageableH =
                paperH - safeVertical - safeVertical;

        if (imageableH <= 0.0d)
            return new PageFormatNormalizeResult(pf, false);

        if (Math.abs(safeVertical - top) <= EPS_PT
                && Math.abs(imageableH - oldPaper.getImageableHeight()) <= EPS_PT) {
            return new PageFormatNormalizeResult(pf, false);
        }

        Paper newPaper =
                new Paper();

        newPaper.setSize(
                paperW,
                paperH
        );

        newPaper.setImageableArea(
                left,
                safeVertical,
                oldPaper.getImageableWidth(),
                imageableH
        );

        PageFormat copy =
                (PageFormat) pf.clone();

        copy.setPaper(newPaper);

        return new PageFormatNormalizeResult(copy, true);
    }

    private static final class PageFormatFallbackResult {

        private final PageFormat pageFormat;
        private final boolean applied;

        private PageFormatFallbackResult(
                PageFormat pageFormat,
                boolean applied
        ) {
            this.pageFormat = pageFormat;
            this.applied = applied;
        }
    }

    private static final class PageFormatNormalizeResult {

        private final PageFormat pageFormat;
        private final boolean normalized;

        private PageFormatNormalizeResult(
                PageFormat pageFormat,
                boolean normalized
        ) {
            this.pageFormat = pageFormat;
            this.normalized = normalized;
        }
    }
}