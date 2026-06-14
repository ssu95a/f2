package ru.inversion.f2.print;

import ru.inversion.f2.prepared.F2StyledDocument;

import java.util.function.IntSupplier;

public final class F2PrintJob {

    private final F2StyledDocument document;
    private final F2PrintPageSetup pageSetup;
    private final String driverRef;
    private final IntSupplier copiesSupplier;
    private final F2PrintListener listener;

    public F2PrintJob(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup,
            String driverRef,
            IntSupplier copiesSupplier,
            F2PrintListener listener
    ) {
        if (document == null)
            throw new IllegalArgumentException("document is null");

        if (pageSetup == null)
            throw new IllegalArgumentException("pageSetup is null");

        this.document = document;
        this.pageSetup = pageSetup;
        this.driverRef = driverRef;
        this.copiesSupplier = copiesSupplier == null ? () -> 1 : copiesSupplier;
        this.listener = listener == null ? F2PrintListener.NONE : listener;
    }

    public F2StyledDocument document() {
        return document;
    }

    public F2PrintPageSetup pageSetup() {
        return pageSetup;
    }

    public String printerName() {
        return pageSetup.printService().getName();
    }

    public String driverRef() {
        return driverRef;
    }

    public int pageCount() {
        return document.pageCount();
    }

    public int copies() {
        int copies = copiesSupplier.getAsInt();
        return copies <= 0 ? 1 : copies;
    }

    public boolean matrixPrinter() {
        return pageSetup.matrixPrinter();
    }

    public F2PrintListener listener() {
        return listener;
    }

    public String geometryToString() {
        return pageSetup.geometryToString();
    }

    @Override
    public String toString() {
        return "F2PrintJob{"
                + "printerName='" + printerName() + '\''
                + ", driverRef='" + driverRef + '\''
                + ", pageCount=" + pageCount()
                + ", copies=" + copies()
                + ", matrixPrinter=" + matrixPrinter()
                + ", pageSetup=" + pageSetup.geometryToString()
                + '}';
    }
}
