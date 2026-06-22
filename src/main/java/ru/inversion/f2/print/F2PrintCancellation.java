package ru.inversion.f2.print;

import java.awt.print.PrinterJob;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class F2PrintCancellation {

    private final AtomicBoolean cancelled =
            new AtomicBoolean();

    private final AtomicReference<PrinterJob> activeJob =
            new AtomicReference<>();

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        cancelled.set(true);

        PrinterJob awtJob = activeJob.get();

        if (awtJob != null)
            awtJob.cancel();
    }

    void bind(PrinterJob awtJob) {
        activeJob.set(awtJob);

        if (cancelled.get())
            awtJob.cancel();
    }

    void unbind(PrinterJob awtJob) {
        activeJob.compareAndSet(awtJob, null);
    }
}