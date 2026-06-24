package ru.inversion.f2.fx;

import javafx.concurrent.Task;
import ru.inversion.f2.print.*;
import ru.inversion.utils.Checks;

public final class F2FxPrintTask extends Task<F2PrintResult> implements F2PrintListener
{
    private final F2PrintJob      printJob;
    private final F2PrintListener delegate;
    private final F2PrintExecutor printExecutor;

    public F2FxPrintTask(F2PrintJob printJob) {
        this( new F2PrintService()::print, printJob );
    }

    F2FxPrintTask( F2PrintExecutor printExecutor, F2PrintJob sourcePrintJob )
    {
        this.printExecutor = Checks.Require.object(printExecutor, "printExecutor");
        this.delegate      = sourcePrintJob.listener();
        this.printJob      = F2PrintJobs.withListener( Checks.Require.object(sourcePrintJob, "sourcePrintJob"), this );
    }

    @Override
    protected F2PrintResult call() throws Exception {

        updateTitle   ("F2 print");
        updateMessage ("Подготовка задания печати");
        updateProgress(0, pageCount());

        try {

            F2PrintResult result = printExecutor.print(printJob);

            if( super.isCancelled() || printJob.isCancelled() )
                return null;

            return result;
        }
        catch (Exception ex) {
            if (super.isCancelled() || printJob.isCancelled())
                return null;
            throw ex;
        }
    }

    public void cancelPrint() {
        updateMessage("Отмена печати");

        printJob.cancel();

        /*
         * Отдельно переводим JavaFX Task в CANCELLED.
         * false: не прерываем поток принудительно,
         * PrinterJob останавливается через F2PrintCancellation.
         */
        super.cancel(false);
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

        delegate.onFinalPrint( printJob, ex );
    }

    private int pageCount() {
        return Math.max( 1, printJob.pageCount() );
    }
}