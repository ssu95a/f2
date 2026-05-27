package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.utils.Checks;

import java.awt.*;
import java.awt.image.BufferedImage;

/** */
public final class F2AwtPreviewRenderer {

    private final F2AwtPagePainter painter = new F2AwtPagePainter();

    public BufferedImage render (
        F2StyledPage page,
        F2AwtPageRenderConfig config
    )
    {
        Checks.Require.object(page, "page");
        Checks.Require.object(config, "config");

        final BufferedImage image = new BufferedImage( config.imageWidthPx(), config.imageHeightPx(), BufferedImage.TYPE_INT_ARGB );

        Graphics2D g = image.createGraphics();

        try {

            setupGraphics(g);

            g.scale( config.scale(), config.scale() );

            painter.paint(g, page, config);
        }
        finally {
            g.dispose();
        }

        return image;
    }

    /** */
    private void setupGraphics(Graphics2D g) {
        g.setRenderingHint (
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
}