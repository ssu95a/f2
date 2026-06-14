package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.F2PrintJobInfo;
import ru.inversion.f2.print.F2PrintListener;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/** */
public final class F2AwtDocumentPrintable implements Printable {

    private final F2StyledDocument document;

    private final F2AwtPagePainter painter;

    private final F2PrintJobInfo   jobInfo;
    private final F2PrintListener  listener;

    public F2AwtDocumentPrintable(
            F2StyledDocument document
    ) {
        this(
                document,
                null,
                F2PrintListener.NONE,
                new F2AwtPagePainter()
        );
    }

    /** */
    public F2AwtDocumentPrintable(
            F2StyledDocument document,
            F2AwtPagePainter painter
    ) {
        this(
                document,
                null,
                F2PrintListener.NONE,
                painter
        );
    }

    /** */
    public F2AwtDocumentPrintable(
            F2StyledDocument document,
            F2PrintJobInfo jobInfo,
            F2PrintListener listener
    ) {
        this(
                document,
                jobInfo,
                listener,
                new F2AwtPagePainter()
        );
    }

    /** */
    public F2AwtDocumentPrintable(
            F2StyledDocument document,
            F2PrintJobInfo jobInfo,
            F2PrintListener listener,
            F2AwtPagePainter painter
    ) {
        if (document == null)
            throw new IllegalArgumentException("document is null");

        if (painter == null)
            throw new IllegalArgumentException("painter is null");

        this.document = document;
        this.jobInfo = jobInfo;
        this.listener = listener == null ? F2PrintListener.NONE : listener;
        this.painter = painter;
    }

    @Override
    public int print( Graphics graphics, PageFormat pageFormat, int pageIndex )
    {
        if (listener.isCancelled())
            return NO_SUCH_PAGE;

        if (pageIndex < 0 || pageIndex >= document.pageCount())
            return NO_SUCH_PAGE;

        F2StyledPage page = document.pages().get(pageIndex);

        F2AwtPageRenderConfig config =
                F2AwtPageRenderConfig.fromPageFormat(
                        pageFormat,
                        72.0d,
                        false
                ).withShrinkToFit(true);

        painter.paint((Graphics2D) graphics, page, config);

        listener.onPagePrinted(
                jobInfo,
                pageIndex
        );

        return PAGE_EXISTS;
    }
}
