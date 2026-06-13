package ru.inversion.f2.print;

public final class F2PrintJobInfo {

    private final F2PrintPageSetup pageSetup;
    private final String printerName;
    private final String driverRef;
    private final int pageCount;
    private final boolean matrixPrinter;

    public F2PrintJobInfo(
            F2PrintPageSetup pageSetup,
            String driverRef,
            int pageCount
    ) {
        if (pageSetup == null)
            throw new IllegalArgumentException("pageSetup is null");

        if (pageCount < 0)
            throw new IllegalArgumentException("pageCount < 0");

        this.pageSetup = pageSetup;
        this.printerName = pageSetup.printService().getName();
        this.driverRef = driverRef;
        this.pageCount = pageCount;
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
                + ", matrixPrinter=" + matrixPrinter
                + ", pageSetup=" + pageSetup.geometryToString()
                + '}';
    }
}
