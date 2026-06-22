package ru.inversion.f2.print;

public final class F2PrintJobs {

    private F2PrintJobs() {
    }

    public static F2PrintJob withListener(
            F2PrintJob source,
            F2PrintListener listener
    ) {
        if (source == null)
            throw new IllegalArgumentException("source is null");

        return new F2PrintJob(
                source.document(),
                source.pageSetup(),
                source.driverRef(),
                source::copies,
                listener,
                source.cancellation()
        );
    }
}
