package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.F2PrintPageSetup;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Преобразует логические страницы документа
 * в физические страницы с учётом printable area.
 *
 * Явные page break сохраняются.
 * Если логическая страница слишком высокая,
 * она делится между строками.
 */

public final class F2AwtDocumentPaginator {

        private static final double EPSILON_PT = 0.01d;
        private final F2AwtLineRenderer lineRenderer = new F2AwtLineRenderer();
        private final F2AwtContentScaleResolver contentScaleResolver =
                new F2AwtContentScaleResolver();

        public F2StyledDocument paginate(
                F2StyledDocument document,
                F2PrintPageSetup pageSetup
        ) {
            if (document == null) {
                throw new IllegalArgumentException(
                        "document is null"
                );
            }

            if (pageSetup == null) {
                throw new IllegalArgumentException(
                        "pageSetup is null"
                );
            }

            if (document.isEmpty()) {
                return document;
            }

            List<F2StyledPage> resultPages =
                    new ArrayList<F2StyledPage>();

            BufferedImage image =
                    new BufferedImage(
                            1,
                            1,
                            BufferedImage.TYPE_INT_ARGB
                    );

            Graphics2D graphics =
                    image.createGraphics();

            try {
                configureGraphics(
                        graphics
                );

                for (F2StyledPage sourcePage
                        : document.pages()) {
                    paginatePage(
                            graphics,
                            sourcePage,
                            pageSetup,
                            resultPages
                    );
                }
            }
            finally {
                graphics.dispose();
            }

            return new F2StyledDocument(
                    resultPages
            );
        }

        private void paginatePage(
                Graphics2D graphics,
                F2StyledPage sourcePage,
                F2PrintPageSetup pageSetup,
                List<F2StyledPage> resultPages
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

            double availableHeightPt =
                    config.imageableHeightPt();

            List<F2StyledLine> currentLines =
                    new ArrayList<F2StyledLine>();

            double usedHeightPt =
                    0.0d;

            double maxRightPt =
                    0.0d;

            for (F2StyledLine line
                    : sourcePage.lines()) {
                F2AwtLineMetrics metrics =
                        lineRenderer.measure(
                                graphics,
                                line
                        );

                double lineHeightPt =
                        Math.max(
                                line.lineStepPt(),
                                metrics.heightPt()
                        );

                double lineRightPt =
                        Math.max(
                                0.0d,
                                line.leftIndentPt()
                                        + metrics.widthPt()
                        );

                double candidateHeightPt =
                        usedHeightPt
                                + lineHeightPt;

                double candidateRightPt =
                        Math.max(
                                maxRightPt,
                                lineRightPt
                        );

                double candidateScale =
                        contentScaleResolver
                                .resolveWidthScale(
                                        candidateRightPt,
                                        config
                                )
                                .finalScale();

                /*
                 * Painter применяет тот же width scale ко всему содержимому
                 * страницы, поэтому сравниваем уже масштабированную высоту.
                 */
                boolean pageOverflow =
                        !currentLines.isEmpty()
                                && candidateHeightPt
                                * candidateScale
                                > availableHeightPt
                                + EPSILON_PT;

                if (pageOverflow) {
                    resultPages.add(
                            new F2StyledPage(
                                    currentLines,
                                    sourcePage.orientation()
                            )
                    );

                    currentLines =
                            new ArrayList<F2StyledLine>();

                    usedHeightPt =
                            0.0d;

                    maxRightPt =
                            0.0d;
                }

                currentLines.add(
                        line
                );

                usedHeightPt +=
                        lineHeightPt;

                maxRightPt =
                        Math.max(
                                maxRightPt,
                                lineRightPt
                        );
            }

            /*
             * Сохраняем даже пустую явно заданную страницу.
             */
            if (!currentLines.isEmpty()
                    || sourcePage.lines().isEmpty()) {
                resultPages.add(
                        new F2StyledPage(
                                currentLines,
                                sourcePage.orientation()
                        )
                );
            }
        }

        private static void configureGraphics(
                Graphics2D graphics
        ) {
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