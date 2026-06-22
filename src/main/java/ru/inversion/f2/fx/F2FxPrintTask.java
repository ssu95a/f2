package ru.inversion.f2.fx;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import ru.inversion.f2.print.F2PrintJob;
import ru.inversion.f2.print.F2PrintListener;
import ru.inversion.f2.print.F2PrintResult;
import ru.inversion.f2.print.F2PrintService;

public final class F2FxPrintTask
        extends Task<F2PrintResult>
        implements F2PrintListener {

    private final F2PrintService printService;
    private final F2PrintJob printJob;
    private final F2PrintListener delegate;

    public F2FxPrintTask(F2PrintJob printJob) {
        this(
                new F2PrintService(),
                printJob
        );
    }

    F2FxPrintTask(
            F2PrintService printService,
            F2PrintJob sourcePrintJob
    ) {
        if (printService == null)
            throw new IllegalArgumentException("printService is null");

        if (sourcePrintJob == null)
            throw new IllegalArgumentException("sourcePrintJob is null");

        this.printService = printService;
        this.delegate = sourcePrintJob.listener();
        this.printJob = sourcePrintJob.withListener(this);

        stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.CANCELLED)
                this.printJob.cancel();
        });
    }

    @Override
    protected F2PrintResult call() throws Exception {
        updateTitle("F2 print");
        updateMessage("Подготовка задания печати");
        updateProgress(0, pageCount());

        return printService.print(printJob);
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
        updateMessage("Начало печати");
        updateProgress(0, pageCount());

        delegate.onBeginPrint(printJob);
    }

    @Override
    public void onPagePrinted(
            F2PrintJob printJob,
            int pageIndex
    ) {
        int completedPages =
                Math.min(
                        pageIndex + 1,
                        pageCount()
                );

        updateMessage(
                "Передача страницы "
                        + completedPages
                        + " / "
                        + pageCount()
        );

        updateProgress(
                completedPages,
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
    public void onFinalPrint(
            F2PrintJob printJob,
            Exception ex
    ) {
        if (!isCancelled()) {
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