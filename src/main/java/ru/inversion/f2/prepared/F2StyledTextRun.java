package ru.inversion.f2.prepared;

import ru.inversion.f2.style.F2Style;

public final class F2StyledTextRun {

    private final String text;
    private final F2Style style;

    public F2StyledTextRun(String text, F2Style style) {
        this.text = text == null ? "" : text;
        this.style = style;
    }

    public String text() {
        return text;
    }

    public F2Style style() {
        return style;
    }

    public boolean isEmpty() {
        return text.length() == 0;
    }

    @Override
    public String toString() {
        return "F2StyledTextRun{"
                + "text='" + text + '\''
                + ", style=" + style
                + '}';
    }
}