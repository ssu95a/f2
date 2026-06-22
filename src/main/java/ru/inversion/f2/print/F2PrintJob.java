package ru.inversion.f2.print;

import ru.inversion.f2.prepared.F2StyledDocument;

import java.util.function.IntSupplier;

public final class F2PrintJob {

    private final F2StyledDocument document;
    private final F2PrintPageSetup pageSetup;
    private final String driverRef;
    private final IntSupplier copiesSupplier;
    private final Integer resolvedCopies;
    private final F2PrintListener listener;
    private final F2PrintCancellation cancellation;

    public F2PrintJob(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup,
            String driverRef,
            IntSupplier copiesSupplier,
            F2PrintListener listener,
            F2PrintCancellation cancellation
    ) {
        this(
                document,
                pageSetup,
                driverRef,
                copiesSupplier,
                null,
                listener, cancellation
        );
    }

    private F2PrintJob(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup,
            String driverRef,
            IntSupplier copiesSupplier,
            Integer resolvedCopies,
            F2PrintListener listener, F2PrintCancellation cancellation
    ) {
        this.cancellation = cancellation;
        if (document == null)
            throw new IllegalArgumentException("document is null");

        if (pageSetup == null)
            throw new IllegalArgumentException("pageSetup is null");

        this.document = document;
        this.pageSetup = pageSetup;
        this.driverRef = driverRef;
        this.copiesSupplier =
                copiesSupplier == null
                        ? () -> 1
                        : copiesSupplier;

        this.resolvedCopies = resolvedCopies;
        this.listener =
                listener == null
                        ? F2PrintListener.NONE
                        : listener;
    }

    public boolean isCancelled() {
        return cancellation.isCancelled();
    }

    public void cancel() {
        cancellation.cancel();
    }

    /**
     * Фиксирует динамическое количество копий.
     *
     * Supplier вызывается ровно один раз.
     */
    public F2PrintJob resolveCopies() {
        if (isCopiesResolved())
            return this;

        return withFixedCopies(
                copiesSupplier.getAsInt()
        );
    }

    public F2PrintJob withFixedCopies(int copies) {
        int safeCopies = normalizeCopies(copies);

        return new F2PrintJob(
                document,
                pageSetup,
                driverRef,
                () -> safeCopies,
                Integer.valueOf(safeCopies),
                listener,
        );
    }

    public boolean isCopiesResolved() {
        return resolvedCopies != null;
    }

    /**
     * До resolveCopies() возвращает текущее значение Supplier.
     * После resolveCopies() возвращает зафиксированное значение.
     */
    public int copies() {
        if (resolvedCopies != null)
            return resolvedCopies.intValue();

        return normalizeCopies(
                copiesSupplier.getAsInt()
        );
    }

    /**
     * Разрешено вызывать только после resolveCopies().
     */
    public int resolvedCopies() {
        if (resolvedCopies == null)
            throw new IllegalStateException(
                    "copies are not resolved"
            );

        return resolvedCopies.intValue();
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

    public boolean matrixPrinter() {
        return pageSetup.matrixPrinter();
    }

    public F2PrintListener listener() {
        return listener;
    }

    public String geometryToString() {
        return pageSetup.geometryToString();
    }

    private static int normalizeCopies(int copies) {
        return copies <= 0 ? 1 : copies;
    }

    @Override
    public String toString() {
        Object copiesText =
                resolvedCopies == null
                        ? "<dynamic>"
                        : resolvedCopies;

        return "F2PrintJob{"
                + "printerName='" + printerName() + '\''
                + ", driverRef='" + driverRef + '\''
                + ", pageCount=" + pageCount()
                + ", copies=" + copiesText
                + ", matrixPrinter=" + matrixPrinter()
                + ", pageSetup=" + pageSetup.geometryToString()
                + '}';
    }
}