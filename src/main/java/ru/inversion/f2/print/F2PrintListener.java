package ru.inversion.f2.print;

public interface F2PrintListener {

    final public static F2PrintListener NONE = new F2PrintListener() { };

    /** Перед началом печати */
    default void onBeginPrint(F2PrintJobInfo jobInfo)
    { }

    /** После последней страницы */
    default void onEndPrint(F2PrintJobInfo jobInfo)
    { }

    /** После печати одной страницы */
    default void onPagePrinted( F2PrintJobInfo jobInfo, int pageIndex )
    { }

    /** Признак, что была отмена печати */
    default boolean isCancelled() {
        return false;
    }

    /** На завершение всего процесса печати */
    default void onFinalPrint( F2PrintJobInfo jobInfo, Exception ex )
    { }
}
