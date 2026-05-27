package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public final class F2AwtPagePainter {

    private final F2AwtLineRenderer lineRenderer = new F2AwtLineRenderer();

    public void paint (
            Graphics2D g,
            F2StyledPage page,
            F2AwtPageRenderConfig config
    )
    {
        if (g == null)
            throw new IllegalArgumentException("g is null");

        if (page == null)
            throw new IllegalArgumentException("page is null");

        if (config == null)
            throw new IllegalArgumentException("config is null");

        paintPaper(g, config);

        if( config.debugOverlay() )
            paintDebugOverlay(g, config);

        paintPage(g, page, config);
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
        g.fill(new Rectangle2D.Double( 0, 0, config.paperWidthPt(), config.paperHeightPt() ));
        g.setColor(Color.BLACK);
    }

    /** */
    private void paintDebugOverlay(Graphics2D g, F2AwtPageRenderConfig config) {

        Color oldColor = g.getColor();

        g.setColor(new Color(80, 120, 255, 160));
        g.draw(new Rectangle2D.Double(
                0,
                0,
                config.paperWidthPt(),
                config.paperHeightPt()
        ));

        g.setColor(new Color(120, 120, 120, 160));
        g.draw(new Rectangle2D.Double(
                config.imageableXPt(),
                config.imageableYPt(),
                config.imageableWidthPt(),
                config.imageableHeightPt()
        ));

        g.setColor(oldColor);
    }
}