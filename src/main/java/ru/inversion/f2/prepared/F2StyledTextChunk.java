package ru.inversion.f2.prepared;

import ru.inversion.f2.style.F2Style;
import ru.inversion.utils.S;

public final class F2StyledTextChunk {

    private final String  text;
    private final F2Style style;

    public F2StyledTextChunk(String text, F2Style style) {
        this.text  = text == null ? S.EMPTY_STRING : text;
        this.style = style;
    }

    public String text() {
        return text;
    }

    public F2Style style() {
        return style;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    @Override
    public String toString() {
        return "F2StyledTextChunk{ text='" + text + '\'' + ", style=" + style + '}';
    }
}