package ru.inversion.f2.awt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.invoke.MethodHandles;

public final class F2AwtPagePainter {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final double DEFAULT_MIN_CONTENT_SCALE = 0.80d;


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

    /** */
    private final F2AwtLineRenderer lineRenderer = new F2AwtLineRenderer();

    public void paint( Graphics2D g, F2StyledPage page, F2AwtPageRenderConfig config )
    {
        if( g == null )
            throw new IllegalArgumentException("g is null");

        if( page == null )
            throw new IllegalArgumentException("page is null");

        if( config == null )
            throw new IllegalArgumentException("config is null");

        paintPaper( g, config );

        if( config.debugOverlay() )
            paintDebugOverlay(g, config);

        paintScaledPageContent( g, page, config );
    }

    private void paintScaledPageContent(
            Graphics2D g,
            F2StyledPage page,
            F2AwtPageRenderConfig config
    ) {
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
        double scale = config.contentScale();

        if (!config.shrinkToFit())
            return scale;

        PageContentMetrics metrics =
                measurePageContent(g, page);

        double fitScale = 1.0d;

        if (metrics.widthPt > 0.0d) {
            fitScale = Math.min(
                    fitScale,
                    config.imageableWidthPt() / metrics.widthPt
            );
        }

        if (metrics.heightPt > 0.0d) {
            fitScale = Math.min(
                    fitScale,
                    config.imageableHeightPt() / metrics.heightPt
            );
        }

        if (fitScale > 1.0d)
            fitScale = 1.0d;

        double requestedScale =
                Math.min(scale, fitScale);

        double resultScale =
                Math.max(requestedScale, DEFAULT_MIN_CONTENT_SCALE);

        if (requestedScale < DEFAULT_MIN_CONTENT_SCALE) {
            log.warn(
                    "F2 page content does not fit min scale: content={}x{} pt, imageable={}x{} pt, requestedScale={}, minContentScale={}",
                    metrics.widthPt,
                    metrics.heightPt,
                    config.imageableWidthPt(),
                    config.imageableHeightPt(),
                    requestedScale,
                    DEFAULT_MIN_CONTENT_SCALE
            );
        }
        else {
            log.debug(
                    "F2 page content scale: content={}x{} pt, imageable={}x{} pt, scale={}",
                    metrics.widthPt,
                    metrics.heightPt,
                    config.imageableWidthPt(),
                    config.imageableHeightPt(),
                    resultScale
            );
        }

        return resultScale;
    }

    private void paintPageContent(
            Graphics2D g,
            F2StyledPage page
    ) {
        double y = 0.0d;

        for (F2StyledLine line : page.lines()) {
            double x = line.leftIndentPt();

            F2AwtLineMetrics metrics =
                    lineRenderer.measure(g, line);

            double baselineY = y + metrics.ascentPt();

            lineRenderer.paint(g, line, x, baselineY);

            y += Math.max(line.lineStepPt(), metrics.heightPt());
        }
    }

    /** */
    private void paintPage (
            Graphics2D g,
            F2StyledPage page,
            F2AwtPageRenderConfig config
    )
    {
        double x0 = config.imageableXPt();
        double y  = config.imageableYPt();

        for( F2StyledLine line : page.lines() )
        {
            double x = x0 + line.leftIndentPt();

            F2AwtLineMetrics metrics = lineRenderer.measure(g, line);

            double baselineY = y + metrics.ascentPt();

            lineRenderer.paint(g, line, x, baselineY);

            y += Math.max(line.lineStepPt(), metrics.heightPt());
        }
    }

    /** */
    private void paintPaper( Graphics2D g, F2AwtPageRenderConfig config )
    {
        g.setColor(Color.WHITE);
        g.fill    ( new Rectangle2D.Double( 0, 0, config.paperWidthPt(), config.paperHeightPt() ) );
        g.setColor(Color.BLACK);
    }

    /** */
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