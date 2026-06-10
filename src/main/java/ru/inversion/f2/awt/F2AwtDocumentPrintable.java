package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.utils.Checks;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/** */
public final class F2AwtDocumentPrintable implements Printable {

    private final F2StyledDocument document;
    private final F2AwtPagePainter painter;
    private final F2PrintPageSetup pageSetup;

    public F2AwtDocumentPrintable(
        F2StyledDocument document,
        F2PrintPageSetup pageSetup
    )
    {
        this(document, pageSetup, new F2AwtPagePainter());
    }

    /** */
    public F2AwtDocumentPrintable(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup,
            F2AwtPagePainter painter
    )
    {
        this.document  = Checks.Require.object(document, "document");
        this.pageSetup = Checks.Require.object(pageSetup, "pageSetup");
        this.painter   = Checks.Require.object(painter, "painter");
    }

    @Override
    public int print( Graphics graphics, PageFormat pageFormat, int pageIndex )
    {
        F2StyledPage page = document.pages().get(pageIndex);

        F2AwtPageRenderConfig config =
                F2AwtPageRenderConfig.fromPageFormat(
                        pageFormat,
                        72.0d,
                        false
                ).withShrinkToFit(true);

        System.out.println(
                "PRINT pageIndex=" + pageIndex
                        + ", pageLandscape=" + page.isLandscape()
                        + ", pfOrientation=" + pageFormat.getOrientation()
                        + ", pf=[" + pageFormat.getWidth() + "x" + pageFormat.getHeight() + "]"
                        + ", imageable=[" + pageFormat.getImageableX()
                        + "," + pageFormat.getImageableY()
                        + "," + pageFormat.getImageableWidth()
                        + "," + pageFormat.getImageableHeight()
                        + "]"
        );

        painter.paint((Graphics2D) graphics, page, config);

        return PAGE_EXISTS;
    }
}
