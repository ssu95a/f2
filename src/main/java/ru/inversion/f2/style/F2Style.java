package ru.inversion.f2.style;

/** Один стиль части текста + фабричные методы для создания производных стилей  */
public final class F2Style {

    private final String  fontName;
    private final int     fontSize;
    private final boolean bold;
    private final boolean italic;
    private final boolean underline;

    public F2Style (
        String  fontName,
        int     fontSize,
        boolean bold,
        boolean italic,
        boolean underline
    )
    {
        this.fontName  = fontName;
        this.fontSize  = fontSize;
        this.bold      = bold;
        this.italic    = italic;
        this.underline = underline;
    }

    public String fontName() {
        return fontName;
    }

    public int fontSize() {
        return fontSize;
    }

    public boolean bold() {
        return bold;
    }

    public boolean italic() {
        return italic;
    }

    public boolean underline() {
        return underline;
    }

    public F2Style withFontName(String value) {
        return new F2Style(value, fontSize, bold, italic, underline);
    }

    public F2Style withFontSize(int value) {
        return new F2Style(fontName, value, bold, italic, underline);
    }

    public F2Style withBold(boolean value) {
        return new F2Style(fontName, fontSize, value, italic, underline);
    }

    public F2Style withItalic(boolean value) {
        return new F2Style(fontName, fontSize, bold, value, underline);
    }

    public F2Style withUnderline(boolean value) {
        return new F2Style(fontName, fontSize, bold, italic, value);
    }

    /** Стиль по умолчанию, если не задан в .ini Init команда */
    public static F2Style defaultStyle( ) {
        return new F2Style( "Courier New", 10, false, false, false );
    }
}