package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.print.F2PrintJob;
import ru.inversion.f2.print.F2PrintListener;
import ru.inversion.f2.print.F2PrintPageSetup;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

/** */
public final class F2AwtPageable implements Pageable {

    private final F2PrintJob printJob;
    private final Printable  printable;

    public F2AwtPageable (
        F2StyledDocument document,
        F2PrintPageSetup pageSetup
    )
    {
        this( new F2PrintJob( document, pageSetup, null, null, F2PrintListener.NONE ) );
    }

    /** */
    public F2AwtPageable( F2PrintJob printJob )
    {
        if( printJob == null )
            throw new IllegalArgumentException("printJob is null");

        this.printJob  = printJob;
        this.printable = new F2AwtDocumentPrintable(printJob);
    }

    @Override
    public int getNumberOfPages() {
        return printJob.pageCount();
    }

    @Override
    public PageFormat getPageFormat(int pageIndex) {
        checkPageIndex(pageIndex);

        PageFormat pageFormat = printJob.pageSetup().pageFormat();

        if (printJob.document().pages().get(pageIndex).isLandscape())
            pageFormat.setOrientation(PageFormat.LANDSCAPE);
        else
            pageFormat.setOrientation(PageFormat.PORTRAIT);

        return pageFormat;
    }

    @Override
    public Printable getPrintable(int pageIndex) {
        checkPageIndex(pageIndex);
        return printable;
    }

    private void checkPageIndex(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= printJob.pageCount())
            throw new IndexOutOfBoundsException("pageIndex=" + pageIndex);
    }
}