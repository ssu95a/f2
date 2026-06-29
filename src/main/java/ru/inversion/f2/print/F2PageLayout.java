package ru.inversion.f2.print;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Физическая раскладка одного логического документа
 * для конкретного printable area.
 */
public final class F2PageLayout {

    private final F2StyledDocument sourceDocument;
    private final List<F2PhysicalPage> pages;

    public F2PageLayout(
            F2StyledDocument sourceDocument,
            List<F2PhysicalPage> pages
    ) {
        if (sourceDocument == null)
            throw new IllegalArgumentException("sourceDocument is null");

        this.sourceDocument = sourceDocument;

        if (pages == null || pages.isEmpty())
            this.pages = Collections.emptyList();
        else
            this.pages = Collections.unmodifiableList(
                    new ArrayList<F2PhysicalPage>(pages)
            );
    }

    public F2StyledDocument sourceDocument() {
        return sourceDocument;
    }

    public List<F2PhysicalPage> pages() {
        return pages;
    }

    public F2PhysicalPage page(int pageIndex) {
        return pages.get(pageIndex);
    }

    public int pageCount() {
        return pages.size();
    }

    public boolean isEmpty() {
        return pages.isEmpty();
    }

    /**
     * Совместимость со старым API paginator.
     * Основной preview/print pipeline этот документ не использует.
     */
    public F2StyledDocument asStyledDocument() {
        if (pages.isEmpty())
            return new F2StyledDocument(Collections.<F2StyledPage>emptyList());

        List<F2StyledPage> styledPages =
                new ArrayList<F2StyledPage>(pages.size());

        for (F2PhysicalPage page : pages)
            styledPages.add(page.asStyledPage());

        return new F2StyledDocument(styledPages);
    }
}
