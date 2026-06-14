package ru.inversion.f2.print;

public interface F2PrintListener {

    F2PrintListener NONE = new F2PrintListener() {
    };

    default void onBeginPrint(F2PrintJob printJob) {
    }

    default void onEndPrint(F2PrintJob printJob) {
    }

    default void onPagePrinted(
            F2PrintJob printJob,
            int pageIndex
    ) {
    }

    default boolean isCancelled() {
        return false;
    }

    default void onFinalPrint(
            F2PrintJob printJob,
            Exception ex
    ) {
    }
}
