package ru.inversion.f2.print;

import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;

import javax.print.attribute.standard.OrientationRequested;
import java.util.List;

/**
 * Лёгкое описание физической страницы.
 *
 * Текст и стили не копируются: страница хранит диапазон строк
 * исходной логической страницы.
 */
public final class F2PhysicalPage {

    private final F2StyledPage sourcePage;
    private final int logicalPageIndex;
    private final int firstLineIndex;
    private final int lineCount;
    private final double contentScale;
    private final F2PageBreakReason breakReason;

    public F2PhysicalPage(
            F2StyledPage sourcePage,
            int logicalPageIndex,
            int firstLineIndex,
            int lineCount,
            double contentScale,
            F2PageBreakReason breakReason
    ) {
        if (sourcePage == null)
            throw new IllegalArgumentException("sourcePage is null");

        if (logicalPageIndex < 0)
            throw new IllegalArgumentException("logicalPageIndex < 0");

        if (firstLineIndex < 0)
            throw new IllegalArgumentException("firstLineIndex < 0");

        if (lineCount < 0)
            throw new IllegalArgumentException("lineCount < 0");

        if (firstLineIndex + lineCount > sourcePage.lineCount())
            throw new IllegalArgumentException("line range is outside sourcePage");

        if (contentScale <= 0.0d)
            throw new IllegalArgumentException("contentScale <= 0");

        if (breakReason == null)
            throw new IllegalArgumentException("breakReason is null");

        this.sourcePage = sourcePage;
        this.logicalPageIndex = logicalPageIndex;
        this.firstLineIndex = firstLineIndex;
        this.lineCount = lineCount;
        this.contentScale = contentScale;
        this.breakReason = breakReason;
    }

    public F2StyledPage sourcePage() {
        return sourcePage;
    }

    public int logicalPageIndex() {
        return logicalPageIndex;
    }

    public int firstLineIndex() {
        return firstLineIndex;
    }

    public int lineCount() {
        return lineCount;
    }

    public int lastLineIndexExclusive() {
        return firstLineIndex + lineCount;
    }

    public double contentScale() {
        return contentScale;
    }

    public F2PageBreakReason breakReason() {
        return breakReason;
    }

    public OrientationRequested orientation() {
        return sourcePage.orientation();
    }

    public boolean isLandscape() {
        return sourcePage.isLandscape();
    }

    public boolean isEmpty() {
        return lineCount == 0;
    }

    public List<F2StyledLine> lines() {
        return sourcePage.lines().subList(
                firstLineIndex,
                firstLineIndex + lineCount
        );
    }

    /** Адаптер для существующего painter API. */
    public F2StyledPage asStyledPage() {
        return new F2StyledPage(lines(), orientation());
    }
}
