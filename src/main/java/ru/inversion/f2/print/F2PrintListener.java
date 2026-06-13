package ru.inversion.f2.print;

public interface F2PrintListener {

    F2PrintListener NONE = new F2PrintListener() {
    };

    default void onBeginPrint(F2PrintJobInfo jobInfo) {
    }

    default void onEndPrint(F2PrintJobInfo jobInfo) {
    }

    default void onPagePrinted(
            F2PrintJobInfo jobInfo,
            int pageIndex
    ) {
    }

    default boolean isCancelled() {
        return false;
    }

    default void onFinalPrint(
            F2PrintJobInfo jobInfo,
            Exception ex
    ) {
    }
}
