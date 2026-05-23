package ru.inversion.f2.command;

public enum F2CommandEffect {
    /**
     * Меняет только отрисовку, но не влияет на layout.
     * Пример: underline.
     */
    PAINT_ONLY,

    /**
     * Может изменить ширину текста.
     * Пример: font family, font size, bold, italic.
     */
    TEXT_METRICS,

    /**
     * Меняет параметры строки.
     * Пример: Vertical Move, Left.
     */
    LINE_LAYOUT,

    /**
     * Меняет поток страниц/строк.
     * Пример: Page End, Lf.
     */
    FLOW,

    /**
     * Меняет настройки документа/job.
     * Пример: Orientation, Set Copies.
     */
    DOCUMENT_SETUP
}