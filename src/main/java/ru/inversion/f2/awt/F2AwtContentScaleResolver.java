package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.utils.Checks;

import java.awt.Graphics2D;

/**
 * Единая политика горизонтального scale для paginator и painter.
 *
 * После появления физической пагинации высота больше не участвует
 * в выборе scale: paginator уже отвечает за перенос строк по страницам.
 */
public final class F2AwtContentScaleResolver {

    private static final double DEFAULT_MIN_CONTENT_SCALE = 0.80d;

    private final F2AwtLineRenderer lineRenderer =
            new F2AwtLineRenderer();

    /** */
    public Result resolveWidthScale(
            Graphics2D graphics,
            F2StyledPage page,
            F2AwtPageRenderConfig config
    ) {
        Checks.Require.objects(
                graphics,
                "graphics",
                page,
                "page",
                config,
                "config"
        );

        return resolveWidthScale(
                measureContentWidthPt(
                        graphics,
                        page
                ),
                config
        );
    }

    /** */
    public Result resolveWidthScale(
            double contentWidthPt,
            F2AwtPageRenderConfig config
    ) {
        Checks.Require.object(
                config,
                "config"
        );

        double normalizedContentWidthPt =
                Math.max(
                        0.0d,
                        contentWidthPt
                );

        double configuredScale =
                config.contentScale();

        double widthFitScale =
                fitScale(
                        normalizedContentWidthPt,
                        config.imageableWidthPt()
                );

        if (!config.shrinkToFit()) {
            return new Result(
                    normalizedContentWidthPt,
                    widthFitScale,
                    configuredScale,
                    configuredScale,
                    configuredScale,
                    false,
                    false
            );
        }

        double requestedScale =
                Math.min(
                        configuredScale,
                        widthFitScale
                );

        double finalScale =
                Math.max(
                        requestedScale,
                        DEFAULT_MIN_CONTENT_SCALE
                );

        return new Result(
                normalizedContentWidthPt,
                widthFitScale,
                configuredScale,
                requestedScale,
                finalScale,
                widthFitScale < configuredScale,
                finalScale > requestedScale
        );
    }

    /** */
    public double measureContentWidthPt(
            Graphics2D graphics,
            F2StyledPage page
    ) {
        Checks.Require.objects(
                graphics,
                "graphics",
                page,
                "page"
        );

        double maxRightPt = 0.0d;

        for (F2StyledLine line : page.lines()) {
            maxRightPt =
                    Math.max(
                            maxRightPt,
                            measureLineRightPt(
                                    graphics,
                                    line
                            )
                    );
        }

        return maxRightPt;
    }

    /** */
    public double measureLineRightPt(
            Graphics2D graphics,
            F2StyledLine line
    ) {
        Checks.Require.object(
                graphics,
                "graphics"
        );

        if (line == null)
            return 0.0d;

        F2AwtLineMetrics metrics =
                lineRenderer.measure(
                        graphics,
                        line
                );

        return Math.max(
                0.0d,
                line.leftIndentPt()
                        + metrics.widthPt()
        );
    }

    /** */
    private static double fitScale(
            double contentSizePt,
            double imageableSizePt
    ) {
        if (contentSizePt <= 0.0d)
            return 1.0d;

        double scale =
                imageableSizePt
                        / contentSizePt;

        return scale > 1.0d
                ? 1.0d
                : scale;
    }

    /** */
    public static final class Result {

        private final double contentWidthPt;
        private final double widthFitScale;
        private final double configuredScale;
        private final double requestedScale;
        private final double finalScale;
        private final boolean shrinkRequired;
        private final boolean minimumScaleApplied;

        private Result(
                double contentWidthPt,
                double widthFitScale,
                double configuredScale,
                double requestedScale,
                double finalScale,
                boolean shrinkRequired,
                boolean minimumScaleApplied
        ) {
            this.contentWidthPt = contentWidthPt;
            this.widthFitScale = widthFitScale;
            this.configuredScale = configuredScale;
            this.requestedScale = requestedScale;
            this.finalScale = finalScale;
            this.shrinkRequired = shrinkRequired;
            this.minimumScaleApplied = minimumScaleApplied;
        }

        public double contentWidthPt() {
            return contentWidthPt;
        }

        public double widthFitScale() {
            return widthFitScale;
        }

        public double configuredScale() {
            return configuredScale;
        }

        public double requestedScale() {
            return requestedScale;
        }

        public double finalScale() {
            return finalScale;
        }

        public boolean shrinkRequired() {
            return shrinkRequired;
        }

        public boolean minimumScaleApplied() {
            return minimumScaleApplied;
        }
    }
}
