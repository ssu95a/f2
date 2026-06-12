package ru.inversion.f2.prepared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Стилизованный документ - набор страниц с форматированным текстом */
public final class F2StyledDocument {

    private final List<F2StyledPage> pages;

    public F2StyledDocument( List<F2StyledPage> pages )
    {
        if( pages == null || pages.isEmpty() )
            this.pages = Collections.emptyList();
        else
            this.pages = Collections.unmodifiableList( new ArrayList<>(pages) );
    }

    public List<F2StyledPage> pages() {
        return pages;
    }

    public int pageCount() {
        return pages.size();
    }

    public boolean isEmpty() {
        return pages.isEmpty();
    }

    /**
     * Временный convenience для старых smoke-тестов.
     */
    public List<F2StyledLine> lines() {

        if( pages.isEmpty() )
            return Collections.emptyList();

        return pages.get(0).lines();
    }

    /**
     * Временный convenience для старых smoke-тестов.
     */
    public int lineCount() {
        return lines().size();
    }
}