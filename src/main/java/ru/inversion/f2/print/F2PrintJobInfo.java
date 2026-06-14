package ru.inversion.f2.print;

public final class F2PrintJobInfo {

    private final F2PrintPageSetup pageSetup;
    private final String printerName;
    private final String driverRef;
    private final int pageCount;
    private final int copies;
    private final boolean matrixPrinter;

    public F2PrintJobInfo(
            F2PrintPageSetup pageSetup,
            String driverRef,
            int pageCount,
            int copies
    ) {
        if (pageSetup == null)
            throw new IllegalArgumentException("pageSetup is null");

        if (pageCount < 0)
            throw new IllegalArgumentException("pageCount < 0");

        if (copies <= 0)
            throw new IllegalArgumentException("copies <= 0");

        this.pageSetup = pageSetup;
        this.printerName = pageSetup.printService().getName();
        this.driverRef = driverRef;
        this.pageCount = pageCount;
        this.copies = copies;
        this.matrixPrinter = pageSetup.matrixPrinter();
    }

    public F2PrintPageSetup pageSetup() {
        return pageSetup;
    }

    public String printerName() {
        return printerName;
    }

    public String driverRef() {
        return driverRef;
    }

    public int pageCount() {
        return pageCount;
    }

    public int copies() {
        return copies;
    }

    public boolean matrixPrinter() {
        return matrixPrinter;
    }

    public String geometryToString() {
        return pageSetup.geometryToString();
    }

    @Override
    public String toString() {
        return "F2PrintJobInfo{"
                + "printerName='" + printerName + '\''
                + ", driverRef='" + driverRef + '\''
                + ", pageCount=" + pageCount
                + ", copies=" + copies
                + ", matrixPrinter=" + matrixPrinter
                + ", pageSetup=" + pageSetup.geometryToString()
                + '}';
    }
}
