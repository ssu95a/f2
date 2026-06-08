package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.print.F2PrintPageSetup;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

public final class F2AwtPageable implements Pageable {

    private final F2StyledDocument document;
    private final Printable        printable;
    private final F2PrintPageSetup pageSetup;

    public F2AwtPageable(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup
    ) {
        if (document == null)
            throw new IllegalArgumentException("document is null");

        if (pageSetup == null)
            throw new IllegalArgumentException("pageSetup is null");

        this.document = document;
        this.pageSetup = pageSetup;
        this.printable = new F2AwtDocumentPrintable(document);
    }

    @Override
    public int getNumberOfPages() {
        return document.pageCount();
    }

    @Override
    public PageFormat getPageFormat(int pageIndex) {
        checkPageIndex(pageIndex);
        return pageSetup.pageFormat();
    }

    @Override
    public Printable getPrintable(int pageIndex) {
        checkPageIndex(pageIndex);
        return printable;
    }

    private void checkPageIndex(int pageIndex) {
        if( pageIndex < 0 || pageIndex >= document.pageCount() )
            throw new IndexOutOfBoundsException("pageIndex=" + pageIndex);
    }
}