package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.utils.Checks;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/** */
public final class F2AwtDocumentPrintable implements Printable {

    private final F2StyledDocument document;
    private final F2AwtPagePainter painter;

    public F2AwtDocumentPrintable(F2StyledDocument document)
    {
        this( document, new F2AwtPagePainter() );
    }

    /** */
    public F2AwtDocumentPrintable( F2StyledDocument document, F2AwtPagePainter painter )
    {
        this.document = Checks.Require.object(document,"document");
        this.painter  = Checks.Require.object(painter ,"painter" );
    }

    @Override
    public int print( Graphics graphics, PageFormat pageFormat, int pageIndex )
    {
        if( pageIndex < 0 || pageIndex >= document.pageCount() )
            return NO_SUCH_PAGE;

        if( !(graphics instanceof Graphics2D) )
            throw new IllegalArgumentException("graphics is not Graphics2D");

        F2AwtPageRenderConfig config = F2AwtPageRenderConfig.fromPageFormat( pageFormat, 72.0d, false );

        painter.paint( (Graphics2D) graphics, document.pages().get(pageIndex), config );

        return PAGE_EXISTS;
    }
}