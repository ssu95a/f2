package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.F2PageLayout;
import ru.inversion.f2.print.F2PhysicalPage;
import ru.inversion.f2.print.F2PrintJob;
import ru.inversion.f2.print.F2PrintListener;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** */
public final class F2AwtDocumentPrintable implements Printable {

    private final F2StyledDocument document;
    private final F2PageLayout pageLayout;
    private final F2AwtPagePainter painter;
    private final F2PrintJob printJob;
    private final F2PrintListener listener;

    public F2AwtDocumentPrintable(
            F2StyledDocument document
    ) {
        this(
                document,
                null,
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
                null,
                F2PrintListener.NONE,
                painter
        );
    }

    /** */
    public F2AwtDocumentPrintable(F2PrintJob printJob) {
        this(
                printJob,
                resolveLayout(printJob),
                new F2AwtPagePainter()
        );
    }

    F2AwtDocumentPrintable(
            F2PrintJob printJob,
            F2PageLayout pageLayout
    ) {
        this(
                printJob,
                pageLayout,
                new F2AwtPagePainter()
        );
    }

    /** */
    public F2AwtDocumentPrintable(
            F2PrintJob printJob,
            F2AwtPagePainter painter
    ) {
        this(
                printJob,
                resolveLayout(printJob),
                painter
        );
    }

    private F2AwtDocumentPrintable(
            F2PrintJob printJob,
            F2PageLayout pageLayout,
            F2AwtPagePainter painter
    ) {
        this(
                printJob.document(),
                pageLayout,
                printJob,
                printJob.listener(),
                painter
        );
    }

    private final Set<Integer> notifiedPages =
            Collections.synchronizedSet(new HashSet<>());

    private F2AwtDocumentPrintable(
            F2StyledDocument document,
            F2PageLayout pageLayout,
            F2PrintJob printJob,
            F2PrintListener listener,
            F2AwtPagePainter painter
    ) {
        if (document == null)
            throw new IllegalArgumentException("document is null");

        if (painter == null)
            throw new IllegalArgumentException("painter is null");

        this.document = document;
        this.pageLayout = pageLayout;
        this.printJob = printJob;
        this.listener = listener == null ? F2PrintListener.NONE : listener;
        this.painter = painter;
    }

    @Override
    public int print( Graphics graphics, PageFormat pageFormat, int pageIndex )
    {
        if (printJob != null && printJob.isCancelled())
            return NO_SUCH_PAGE;

        int pageCount = pageLayout == null
                ? document.pageCount()
                : pageLayout.pageCount();

        if (pageIndex < 0 || pageIndex >= pageCount)
            return NO_SUCH_PAGE;

        F2StyledPage page;
        F2AwtPageRenderConfig config =
                F2AwtPageRenderConfig.fromPageFormat(
                        pageFormat,
                        72.0d,
                        false
                );

        if (pageLayout == null) {
            page = document.pages().get(pageIndex);
            config = config.withShrinkToFit(true);
        }
        else {
            F2PhysicalPage physicalPage = pageLayout.page(pageIndex);
            page = physicalPage.asStyledPage();
            config = config
                    .withContentScale(physicalPage.contentScale())
                    .withShrinkToFit(false);
        }

        painter.paint((Graphics2D) graphics, page, config);

        if (notifiedPages.add(pageIndex)) {
            listener.onPagePrinted(
                    printJob,
                    pageIndex
            );
        }

        return PAGE_EXISTS;
    }

    private static F2PageLayout resolveLayout(F2PrintJob printJob) {
        if (printJob == null)
            throw new IllegalArgumentException("printJob is null");

        if (printJob.hasPageLayout())
            return printJob.pageLayout();

        return new F2AwtDocumentPaginator().layout(
                printJob.document(),
                printJob.pageSetup()
        );
    }
}
