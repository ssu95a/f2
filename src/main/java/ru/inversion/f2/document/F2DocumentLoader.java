package ru.inversion.f2.document;

import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.prepared.F2PreparedDocument;
import ru.inversion.f2.prepared.F2PreparedDocumentParser;
import ru.inversion.f2.prepared.F2PreparedTextInterpreter;
import ru.inversion.f2.prepared.F2StyledDocument;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Загружает файл отчёта в указанной кодировке
 * и полностью подготавливает его для preview/печати.
 *
 * Не отвечает за:
 * - выбор кодировки пользователем;
 * - JavaFX UI;
 * - выбор принтера;
 * - PageFormat;
 * - запуск печати.
 */
public final class F2DocumentLoader {

    private final F2CommandRegistry commandRegistry;
    private final F2PreparedDocumentParser parser = new F2PreparedDocumentParser();

    private final F2PreparedTextInterpreter interpreter = new F2PreparedTextInterpreter();

    public F2DocumentLoader(
            F2CommandRegistry commandRegistry
    ) {
        this.commandRegistry =
                Objects.requireNonNull(
                        commandRegistry,
                        "commandRegistry"
                );
    }

    public F2LoadedDocument load(
            Path source,
            Charset charset
    ) throws IOException {
        Objects.requireNonNull(
                source,
                "source"
        );

        Objects.requireNonNull(
                charset,
                "charset"
        );

        Path normalizedSource =
                source
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isRegularFile(normalizedSource)) {
            throw new IOException(
                    "Файл отчёта не найден: "
                            + normalizedSource
            );
        }

        byte[] bytes =
                Files.readAllBytes(
                        normalizedSource
                );

        String text =
                decode(
                        bytes,
                        charset
                );

        /*
         * UTF-8 BOM после декодирования превращается в U+FEFF.
         * Он не должен попадать в первый текстовый токен.
         */
        if (!text.isEmpty()
                && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }

        F2PreparedDocument preparedDocument =
                parser.parse(
                        text,
                        commandRegistry
                );

        F2StyledDocument styledDocument =
                interpreter.interpret(
                        preparedDocument.tokens(),
                        commandRegistry
                );

        return new F2LoadedDocument(
                normalizedSource,
                charset,
                preparedDocument,
                styledDocument
        );
    }

    public F2LoadedDocument load(
            Path source,
            String charsetName
    ) throws IOException {
        Objects.requireNonNull(
                charsetName,
                "charsetName"
        );

        return load(
                source,
                Charset.forName(
                        charsetName
                )
        );
    }

    private static String decode(
            byte[] bytes,
            Charset charset
    ) throws CharacterCodingException {
        CharsetDecoder decoder =
                charset
                        .newDecoder()
                        .onMalformedInput(
                                CodingErrorAction.REPORT
                        )
                        .onUnmappableCharacter(
                                CodingErrorAction.REPORT
                        );

        CharBuffer characters =
                decoder.decode(
                        ByteBuffer.wrap(bytes)
                );

        return characters.toString();
    }
}
