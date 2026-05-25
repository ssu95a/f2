package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

public final class F2AwtPageRenderer {

    private final F2AwtLineRenderer lineRenderer =
            new F2AwtLineRenderer();

    public BufferedImage render (
            F2StyledPage page,
            F2AwtPageRenderConfig config
    )
    {
        if( page == null )
            throw new IllegalArgumentException("page is null");

        if( config == null )
            throw new IllegalArgumentException("config is null");

        BufferedImage image = new BufferedImage(
                config.imageWidthPx(),
                config.imageHeightPx(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = image.createGraphics();

        try {
            setupGraphics(g);

            /*
             * Дальше работаем в pt, не в px.
             * Масштаб переводит 1 pt -> dpi/72 px.
             */
            g.scale(config.scale(), config.scale());

            paintPaper(g, config);

            if (config.debugOverlay())
                paintDebugOverlay(g, config);

            paintPage(g, page, config);
        }
        finally {
            g.dispose();
        }

        return image;
    }

    private void setupGraphics(Graphics2D g) {
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
    }

    private void paintPaper(
            Graphics2D g,
            F2AwtPageRenderConfig config
    ) {
        g.setColor(Color.WHITE);
        g.fillRect(
                0,
                0,
                (int) Math.ceil(config.paperWidthPt()),
                (int) Math.ceil(config.paperHeightPt())
        );

        g.setColor(Color.BLACK);
    }

    private void paintPage(
            Graphics2D g,
            F2StyledPage page,
            F2AwtPageRenderConfig config
    ) {
        double x0 = config.imageableXPt();
        double y = config.imageableYPt();

        List<F2StyledLine> lines = page.lines();

        for (F2StyledLine line : lines) {
            double x = x0 + line.leftIndentPt();

            F2AwtLineMetrics metrics =
                    lineRenderer.measure(g, line);

            double ascent = metrics.ascentPt();

            /*
             * Если строка пустая, ascent будет 0.
             * Тогда двигаемся только по lineStepPt.
             */
            double baselineY = y + ascent;

            lineRenderer.paint(g, line, x, baselineY);

            /*
             * SPACE_AFTER / lineStep semantics:
             * lineStepPt — полный шаг строки.
             *
             * Но если glyph metrics внезапно выше lineStep,
             * не наезжаем на следующую строку.
             */
            y += Math.max(line.lineStepPt(), metrics.heightPt());
        }
    }

    private void paintDebugOverlay(
            Graphics2D g,
            F2AwtPageRenderConfig config
    ) {
        /*
         * Синий: paper bounds.
         * Серый: imageable area.
         *
         * Цвета только debug, не production-render semantics.
         */
        Color oldColor = g.getColor();

        g.setStroke(new BasicStroke(0.5f));

        g.setColor(new Color(80, 120, 255, 160));
        g.drawRect(
                0,
                0,
                (int) Math.round(config.paperWidthPt()),
                (int) Math.round(config.paperHeightPt())
        );

        g.setColor(new Color(120, 120, 120, 160));
        g.drawRect(
                (int) Math.round(config.imageableXPt()),
                (int) Math.round(config.imageableYPt()),
                (int) Math.round(config.imageableWidthPt()),
                (int) Math.round(config.imageableHeightPt())
        );

        g.setColor(oldColor);
    }
}