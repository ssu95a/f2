package ru.inversion.f2.print;

public final class F2PrintResult {

    private final String printerName;
    private final String driverRef;
    private final int pageCount;

    public F2PrintResult(
            String printerName,
            String driverRef,
            int pageCount
    ) {
        this.printerName = printerName;
        this.driverRef = driverRef;
        this.pageCount = pageCount;
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

    @Override
    public String toString() {
        return "F2PrintResult{"
                + "printerName='" + printerName + '\''
                + ", driverRef='" + driverRef + '\''
                + ", pageCount=" + pageCount
                + '}';
    }
}