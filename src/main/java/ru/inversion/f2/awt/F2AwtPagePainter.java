package ru.inversion.f2.awt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.utils.Checks;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.invoke.MethodHandles;

public final class F2AwtPagePainter {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final double DEFAULT_MIN_CONTENT_SCALE = 0.80d;

    private static final double DEFAULT_WARN_CONTENT_SCALE = 0.95d;

    private static final double PT_TO_MM = 25.4d / 72.0d;

    private static final class PageContentMetrics {

        private final double widthPt;
        private final double heightPt;

        private PageContentMetrics(
                double widthPt,
                double heightPt
        ) {
            this.widthPt = widthPt;
            this.heightPt = heightPt;
        }
    }

    private final F2AwtLineRenderer lineRenderer = new F2AwtLineRenderer();

    /** */
    public void paint( Graphics2D g, F2StyledPage page, F2AwtPageRenderConfig config )
    {
        Checks.Require.objects( g, "g", page, "page", config, "config" );

        paintPaper( g, config );

        if( config.debugOverlay() )
            paintDebugOverlay(g, config);

        paintScaledPageContent( g, page, config );
    }

    /** */
    private void paintScaledPageContent (
        Graphics2D g, F2StyledPage page, F2AwtPageRenderConfig config
    )
    {
        double scale = resolveContentScale(g, page, config);

        AffineTransform oldTransform = g.getTransform();

        try {
            g.translate(config.imageableXPt(), config.imageableYPt());
            g.scale(scale, scale);

            paintPageContent(g, page);
        }
        finally {
            g.setTransform(oldTransform);
        }
    }

    private PageContentMetrics measurePageContent(
            Graphics2D g,
            F2StyledPage page
    ) {
        double y = 0.0d;
        double maxRight = 0.0d;

        for (F2StyledLine line : page.lines()) {
            F2AwtLineMetrics metrics =
                    lineRenderer.measure(g, line);

            double right =
                    line.leftIndentPt() + metrics.widthPt();

            maxRight = Math.max(maxRight, right);

            y += Math.max(line.lineStepPt(), metrics.heightPt());
        }

        return new PageContentMetrics(maxRight, y);
    }

    private double resolveContentScale(
            Graphics2D g,
            F2StyledPage page,
            F2AwtPageRenderConfig config
    ) {
        double configuredScale = config.contentScale();

        if (!config.shrinkToFit())
            return configuredScale;

        PageContentMetrics metrics =
                measurePageContent(g, page);

        double pageContractScale = resolvePageContractScale(config);
        double widthFitScale = fitScale(metrics.widthPt, config.imageableWidthPt());
        double heightFitScale = fitScale(metrics.heightPt, config.imageableHeightPt());
        double metricsFitScale = Math.min(widthFitScale, heightFitScale);
        double fitScale = Math.min(pageContractScale, metricsFitScale);
        double requestedScale = Math.min(configuredScale, fitScale);
        double resultScale = Math.max(requestedScale, DEFAULT_MIN_CONTENT_SCALE);

        double overflowWidthPt = overflowPt(metrics.widthPt, config.imageableWidthPt());
        double overflowHeightPt = overflowPt(metrics.heightPt, config.imageableHeightPt());

        boolean onePageFit = requestedScale >= DEFAULT_MIN_CONTENT_SCALE;
        boolean shrinkRequired = requestedScale < configuredScale;

        logOnePageFitDiagnostics(
                onePageFit,
                shrinkRequired,
                metrics,
                config,
                overflowWidthPt,
                overflowHeightPt,
                pageContractScale,
                widthFitScale,
                heightFitScale,
                configuredScale,
                requestedScale,
                resultScale
        );

        return resultScale;
    }

    private double resolvePageContractScale(F2AwtPageRenderConfig config) {
        double widthScale = fitScale(config.paperWidthPt(), config.imageableWidthPt());
        double heightScale = fitScale(config.paperHeightPt(), config.imageableHeightPt());
        return Math.min(widthScale, heightScale);
    }

    private void logOnePageFitDiagnostics(
            boolean onePageFit,
            boolean shrinkRequired,
            PageContentMetrics metrics,
            F2AwtPageRenderConfig config,
            double overflowWidthPt,
            double overflowHeightPt,
            double pageContractScale,
            double widthFitScale,
            double heightFitScale,
            double configuredScale,
            double requestedScale,
            double resultScale
    ) {
        if (resultScale < DEFAULT_WARN_CONTENT_SCALE || !onePageFit) {
            log.warn(
                    "F2 page one-page fit diagnostics: onePageFit={}, shrinkRequired={}, content={}x{} pt, imageable={}x{} pt, overflow={}x{} pt, overflowMm={}x{}, pageContractScale={}, fitScaleWidth={}, fitScaleHeight={}, configuredScale={}, requestedScale={}, finalScale={}, warnContentScale={}, minContentScale={}",
                    onePageFit,
                    shrinkRequired,
                    metrics.widthPt,
                    metrics.heightPt,
                    config.imageableWidthPt(),
                    config.imageableHeightPt(),
                    overflowWidthPt,
                    overflowHeightPt,
                    ptToMm(overflowWidthPt),
                    ptToMm(overflowHeightPt),
                    pageContractScale,
                    widthFitScale,
                    heightFitScale,
                    configuredScale,
                    requestedScale,
                    resultScale,
                    DEFAULT_WARN_CONTENT_SCALE,
                    DEFAULT_MIN_CONTENT_SCALE
            );
        }
        else {
            log.debug(
                    "F2 page one-page fit diagnostics: onePageFit={}, shrinkRequired={}, content={}x{} pt, imageable={}x{} pt, overflow={}x{} pt, overflowMm={}x{}, pageContractScale={}, fitScaleWidth={}, fitScaleHeight={}, configuredScale={}, requestedScale={}, finalScale={}",
                    onePageFit,
                    shrinkRequired,
                    metrics.widthPt,
                    metrics.heightPt,
                    config.imageableWidthPt(),
                    config.imageableHeightPt(),
                    overflowWidthPt,
                    overflowHeightPt,
                    ptToMm(overflowWidthPt),
                    ptToMm(overflowHeightPt),
                    pageContractScale,
                    widthFitScale,
                    heightFitScale,
                    configuredScale,
                    requestedScale,
                    resultScale
            );
        }
    }

    private static double fitScale(
            double contentSizePt,
            double imageableSizePt
    ) {
        if (contentSizePt <= 0.0d)
            return 1.0d;

        double scale = imageableSizePt / contentSizePt;

        return scale > 1.0d ? 1.0d : scale;
    }

    private static double overflowPt(
            double contentSizePt,
            double imageableSizePt
    ) {
        return Math.max(0.0d, contentSizePt - imageableSizePt);
    }

    private static double ptToMm(double valuePt) {
        return valuePt * PT_TO_MM;
    }

    /** */
    private void paintPageContent( Graphics2D g, F2StyledPage page )
    {
        double y = 0.0d;

        for( F2StyledLine line : page.lines() )
        {
            double x = line.leftIndentPt();

            F2AwtLineMetrics metrics = lineRenderer.measure( g, line );

            double baselineY = y + metrics.ascentPt();

            lineRenderer.paint( g, line, x, baselineY );

            y += Math.max( line.lineStepPt(), metrics.heightPt() );
        }
    }

    private void paintPaper( Graphics2D g, F2AwtPageRenderConfig config )
    {
        g.setColor( Color.WHITE );
        g.fill    ( new Rectangle2D.Double( 0, 0, config.paperWidthPt(), config.paperHeightPt() ) );
        g.setColor( Color.BLACK );
    }

    private void paintDebugOverlay( Graphics2D g, F2AwtPageRenderConfig config ) {

        final Color oldColor = g.getColor();

        try {

            g.setColor( new Color(80, 120, 255, 160) );

            g.draw( new Rectangle2D.Double(
                    0,
                    0,
                    config.paperWidthPt(),
                    config.paperHeightPt()
            ));

            g.setColor(new Color(120, 120, 120, 160));

            g.draw( new Rectangle2D.Double( config.imageableXPt(), config.imageableYPt(), config.imageableWidthPt(), config.imageableHeightPt() ));
        }
        finally {
            g.setColor(oldColor);
        }
    }
}
