package ru.inversion.f2.document;

import ru.inversion.f2.prepared.F2PreparedContentMode;
import ru.inversion.f2.prepared.F2PreparedDocument;
import ru.inversion.f2.prepared.F2StyledDocument;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Результат загрузки и подготовки файла отчёта.
 *
 * Хранит промежуточный документ, чтобы при изменении plain-шрифта
 * не перечитывать файл и не декодировать его повторно.
 */
public final class F2LoadedDocument {

    private final Path source;
    private final Charset charset;
    private final F2PreparedDocument preparedDocument;
    private final F2StyledDocument styledDocument;

    public F2LoadedDocument(
            Path source,
            Charset charset,
            F2PreparedDocument preparedDocument,
            F2StyledDocument styledDocument
    ) {
        this.source =
                Objects.requireNonNull(
                                source,
                                "source"
                        )
                        .toAbsolutePath()
                        .normalize();

        this.charset =
                Objects.requireNonNull(
                        charset,
                        "charset"
                );

        this.preparedDocument =
                Objects.requireNonNull(
                        preparedDocument,
                        "preparedDocument"
                );

        this.styledDocument =
                Objects.requireNonNull(
                        styledDocument,
                        "styledDocument"
                );
    }

    public Path source() {
        return source;
    }

    public Charset charset() {
        return charset;
    }

    public F2PreparedDocument preparedDocument() {
        return preparedDocument;
    }

    public F2PreparedContentMode contentMode() {
        return preparedDocument.contentMode();
    }

    public F2StyledDocument styledDocument() {
        return styledDocument;
    }

    public boolean plainFontEditable() {
        return contentMode() == F2PreparedContentMode.PLAIN
                || contentMode() == F2PreparedContentMode.PLAIN_WITH_HEADER;
    }

    @Override
    public String toString() {
        return "F2LoadedDocument{"
                + "source=" + source
                + ", charset=" + charset
                + ", contentMode=" + contentMode()
                + ", pageCount=" + styledDocument.pageCount()
                + '}';
    }
}
