package ru.inversion.f2.prepared;

import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.utils.Checks;

import java.util.List;

public final class F2PreparedDocumentParser {

    private final F2PreparedTextParser tokenParser =
            new F2PreparedTextParser();

    private final F2PreparedContentModeDetector contentModeDetector =
            new F2PreparedContentModeDetector();

    public F2PreparedDocument parse(
            String text,
            F2CommandRegistry registry
    ) {
        Checks.Require.text(text, "text");
        Checks.Require.object(registry, "registry");

        List<F2PreparedToken> tokens =
                tokenParser.parse(text);

        F2PreparedContentMode contentMode =
                contentModeDetector.detect(tokens, registry);

        return new F2PreparedDocument(
                tokens,
                contentMode
        );
    }
}