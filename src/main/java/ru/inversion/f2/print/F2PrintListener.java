package ru.inversion.f2.print;

public interface F2PrintListener {

    F2PrintListener NONE = new F2PrintListener() {
    };

    /**
     * Динамические параметры задания зафиксированы.
     * Вызывается перед onBeginPrint().
     */
    default void onCopiesResolved(
            F2PrintJob printJob,
            int copies
    )
    { }

    /**
     * Начинается передача задания в PrinterJob.
     */
    default void onBeginPrint(F2PrintJob printJob) {
    }

    /**
     * Страница отрисована для печатного pipeline.
     */
    default void onPagePrinted(
            F2PrintJob printJob,
            int pageIndex
    ) {
    }

    /**
     * PrinterJob.print() успешно завершился.
     * Физический принтер ещё может продолжать печать.
     */
    default void onEndPrint(F2PrintJob printJob) {
    }

    default boolean isCancelled() {
        return false;
    }

    /**
     * Финальное событие, вызывается и при успехе, и при ошибке.
     */
    default void onFinalPrint(
            F2PrintJob printJob,
            Exception ex
    ) {
    }
}