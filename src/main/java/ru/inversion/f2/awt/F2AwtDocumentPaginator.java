package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.F2PageBreakReason;
import ru.inversion.f2.print.F2PageLayout;
import ru.inversion.f2.print.F2PhysicalPage;
import ru.inversion.f2.print.F2PrintPageSetup;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Преобразует логические страницы документа
 * в физическую раскладку с учётом printable area.
 *
 * Явные page break сохраняются как границы логических страниц.
 * Если логическая страница слишком высокая, она делится между строками.
 */
public final class F2AwtDocumentPaginator {

    private static final double EPSILON_PT = 0.01d;

    private final F2AwtLineRenderer lineRenderer = new F2AwtLineRenderer();
    private final F2AwtContentScaleResolver contentScaleResolver =
            new F2AwtContentScaleResolver();

    /**
     * Строит лёгкую физическую раскладку, не копируя logical document.
     */
    public F2PageLayout layout(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup
    ) {
        if (document == null)
            throw new IllegalArgumentException("document is null");

        if (pageSetup == null)
            throw new IllegalArgumentException("pageSetup is null");

        if (document.isEmpty())
            return new F2PageLayout(
                    document,
                    Collections.<F2PhysicalPage>emptyList()
            );

        List<F2PhysicalPage> resultPages =
                new ArrayList<F2PhysicalPage>();

        BufferedImage image = new BufferedImage(
                1,
                1,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = image.createGraphics();

        try {
            configureGraphics(graphics);

            for (int logicalPageIndex = 0;
                 logicalPageIndex < document.pageCount();
                 logicalPageIndex++) {
                layoutPage(
                        graphics,
                        document.pages().get(logicalPageIndex),
                        logicalPageIndex,
                        pageSetup,
                        resultPages
                );
            }
        }
        finally {
            graphics.dispose();
        }

        return new F2PageLayout(document, resultPages);
    }

    /**
     * Старый API оставлен как адаптер для внешнего кода.
     * Новый preview/print pipeline использует layout().
     */
    @Deprecated
    public F2StyledDocument paginate(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup
    ) {
        return layout(document, pageSetup).asStyledDocument();
    }

    private void layoutPage(
            Graphics2D graphics,
            F2StyledPage sourcePage,
            int logicalPageIndex,
            F2PrintPageSetup pageSetup,
            List<F2PhysicalPage> resultPages
    ) {
        F2AwtPageRenderConfig config =
                F2AwtPageRenderConfig
                        .fromPrintPageSetup(
                                pageSetup,
                                sourcePage,
                                72.0d,
                                false
                        )
                        .withShrinkToFit(true);

        if (sourcePage.isEmpty()) {
            finishPage(
                    resultPages,
                    sourcePage,
                    logicalPageIndex,
                    0,
                    0,
                    1.0d,
                    F2PageBreakReason.LOGICAL_PAGE_END
            );
            return;
        }

        double availableHeightPt = config.imageableHeightPt();

        int firstLineIndex = 0;
        int lineCount = 0;
        double usedHeightPt = 0.0d;
        double maxRightPt = 0.0d;
        double currentScale = 1.0d;

        for (int lineIndex = 0;
             lineIndex < sourcePage.lineCount();
             lineIndex++) {
            F2StyledLine line = sourcePage.lines().get(lineIndex);

            F2AwtLineMetrics metrics =
                    lineRenderer.measure(graphics, line);

            double lineHeightPt = Math.max(
                    line.lineStepPt(),
                    metrics.heightPt()
            );

            double lineRightPt = Math.max(
                    0.0d,
                    line.leftIndentPt() + metrics.widthPt()
            );

            double candidateHeightPt = usedHeightPt + lineHeightPt;
            double candidateRightPt = Math.max(maxRightPt, lineRightPt);
            double candidateScale = resolveScale(candidateRightPt, config);

            boolean pageOverflow =
                    lineCount > 0
                            && candidateHeightPt * candidateScale
                            > availableHeightPt + EPSILON_PT;

            if (pageOverflow) {
                finishPage(
                        resultPages,
                        sourcePage,
                        logicalPageIndex,
                        firstLineIndex,
                        lineCount,
                        currentScale,
                        F2PageBreakReason.HEIGHT_OVERFLOW
                );

                firstLineIndex = lineIndex;
                lineCount = 1;
                usedHeightPt = lineHeightPt;
                maxRightPt = lineRightPt;
                currentScale = resolveScale(maxRightPt, config);
            }
            else {
                lineCount++;
                usedHeightPt = candidateHeightPt;
                maxRightPt = candidateRightPt;
                currentScale = candidateScale;
            }
        }

        finishPage(
                resultPages,
                sourcePage,
                logicalPageIndex,
                firstLineIndex,
                lineCount,
                currentScale,
                F2PageBreakReason.LOGICAL_PAGE_END
        );
    }

    /**
     * Единый аналог старого EndPage(): сюда сходятся
     * и синтетическое переполнение, и конец logical page.
     */
    private static void finishPage(
            List<F2PhysicalPage> resultPages,
            F2StyledPage sourcePage,
            int logicalPageIndex,
            int firstLineIndex,
            int lineCount,
            double contentScale,
            F2PageBreakReason breakReason
    ) {
        resultPages.add(
                new F2PhysicalPage(
                        sourcePage,
                        logicalPageIndex,
                        firstLineIndex,
                        lineCount,
                        contentScale,
                        breakReason
                )
        );
    }

    private double resolveScale(
            double contentRightPt,
            F2AwtPageRenderConfig config
    ) {
        return contentScaleResolver
                .resolveWidthScale(contentRightPt, config)
                .finalScale();
    }

    private static void configureGraphics(Graphics2D graphics) {
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
    }
}
