package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.utils.Checks;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** */
public final class F2AwtPreviewRenderer {

    private final F2AwtPagePainter painter = new F2AwtPagePainter();

    /** */
    public BufferedImage render( F2StyledPage page, F2PrintPageSetup pageSetup )
    {
        return render( page, F2AwtPageRenderConfig.fromPrintPageSetup( pageSetup, page ) .withShrinkToFit(true) );
    }

    /** */
    public BufferedImage render ( F2StyledPage page, F2PrintPageSetup pageSetup, double dpi, boolean debugOverlay )
    {
        return render( page, F2AwtPageRenderConfig.fromPrintPageSetup( pageSetup, page, dpi, debugOverlay ).withShrinkToFit(true) );
    }


    /** */
    private BufferedImage render( F2StyledPage page, F2AwtPageRenderConfig config )
    {
        Checks.Require.object( page, "page" );
        Checks.Require.object( config, "config" );

        BufferedImage image = new BufferedImage( config.imageWidthPx(), config.imageHeightPx(), BufferedImage.TYPE_INT_ARGB );

        Graphics2D g = image.createGraphics();

        try {

            setupGraphics(g);

            g.scale (
                config.scale(),
                config.scale()
            );

            painter.paint(g, page, config);
        }
        finally {
            g.dispose();
        }

        return image;
    }

    /** */
    private void setupGraphics(Graphics2D g)
    {
        g.setRenderingHint (
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g.setRenderingHint (
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint (
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        );
    }
}
