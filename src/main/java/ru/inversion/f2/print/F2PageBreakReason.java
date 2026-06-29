package ru.inversion.f2.print;

/** Причина завершения физической страницы. */
public enum F2PageBreakReason {

    /** Автоматический разрыв из-за переполнения printable area. */
    HEIGHT_OVERFLOW,

    /** Конец логической страницы, включая явный PAGE_END. */
    LOGICAL_PAGE_END
}
