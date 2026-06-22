package ru.inversion.f2.fx;

import javafx.concurrent.Task;
import ru.inversion.f2.print.F2PrintExecutor;
import ru.inversion.f2.print.F2PrintJob;
import ru.inversion.f2.print.F2PrintJobs;
import ru.inversion.f2.print.F2PrintListener;
import ru.inversion.f2.print.F2PrintResult;
import ru.inversion.f2.print.F2PrintService;

import java.util.concurrent.atomic.AtomicInteger;

public final class F2FxPrintTask
        extends Task<F2PrintResult>
        implements F2PrintListener {

    private final F2PrintExecutor printExecutor;
    private final F2PrintJob printJob;
    private final F2PrintListener delegate;
    private final AtomicInteger completedPages =
            new AtomicInteger();

    public F2FxPrintTask(F2PrintJob printJob) {
        this(
                new F2PrintService()::print,
                printJob
        );
    }

    F2FxPrintTask(
            F2PrintExecutor printExecutor,
            F2PrintJob sourcePrintJob
    ) {
        if (printExecutor == null)
            throw new IllegalArgumentException("printExecutor is null");

        if (sourcePrintJob == null)
            throw new IllegalArgumentException("sourcePrintJob is null");

        this.printExecutor = printExecutor;
        this.delegate = sourcePrintJob.listener();
        this.printJob = F2PrintJobs.withListener(
                sourcePrintJob,
                this
        );
    }

    @Override
    protected F2PrintResult call() throws Exception {
        updateTitle("F2 print");
        updateMessage("Подготовка задания печати");
        updateProgress(0, pageCount());

        try {
            F2PrintResult result =
                    printExecutor.print(printJob);

            if (printJob.isCancelled()) {
                cancel(false);
                return null;
            }

            return result;
        }
        catch (Exception ex) {
            if (printJob.isCancelled()) {
                cancel(false);
                return null;
            }

            throw ex;
        }
    }

    public void cancelPrint() {
        updateMessage("Отмена печати");
        printJob.cancel();
    }

    public F2PrintJob printJob() {
        return printJob;
    }

    @Override
    public void onCopiesResolved(
            F2PrintJob printJob,
            int copies
    ) {
        updateMessage(
                "Принтер: " + printJob.printerName()
                        + ", копий: " + copies
        );

        delegate.onCopiesResolved(
                printJob,
                copies
        );
    }

    @Override
    public void onBeginPrint(F2PrintJob printJob) {
        completedPages.set(0);
        updateMessage("Начало печати");
        updateProgress(0, pageCount());

        delegate.onBeginPrint(printJob);
    }

    @Override
    public void onPagePrinted(
            F2PrintJob printJob,
            int pageIndex
    ) {
        int completed = completedPages.accumulateAndGet(
                pageIndex + 1,
                Math::max
        );

        completed = Math.min(
                completed,
                pageCount()
        );

        updateMessage(
                "Передача страницы "
                        + completed
                        + " / "
                        + pageCount()
        );

        updateProgress(
                completed,
                pageCount()
        );

        delegate.onPagePrinted(
                printJob,
                pageIndex
        );
    }

    @Override
    public void onEndPrint(F2PrintJob printJob) {
        updateProgress(
                pageCount(),
                pageCount()
        );

        updateMessage(
                "Документ передан драйверу печати"
        );

        delegate.onEndPrint(printJob);
    }

    @Override
    public boolean isCancelled() {
        return printJob.isCancelled()
                || super.isCancelled();
    }

    @Override
    public void onFinalPrint(
            F2PrintJob printJob,
            Exception ex
    ) {
        if (printJob.isCancelled()) {
            updateMessage("Печать отменена");
        }
        else {
            updateMessage(
                    ex == null
                            ? "Печать завершена"
                            : "Ошибка печати: " + ex.getMessage()
            );
        }

        delegate.onFinalPrint(
                printJob,
                ex
        );
    }

    private int pageCount() {
        return Math.max(
                1,
                printJob.pageCount()
        );
    }
}
