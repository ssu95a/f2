package ru.inversion.f2.diagnostic;

import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.ini.F2AltIniModelLoader;
import ru.inversion.f2.ini.F2MapAltIniModel;
import ru.inversion.f2.prepared.F2PreparedTextInterpreter;
import ru.inversion.f2.prepared.F2PreparedTextParser;
import ru.inversion.f2.prepared.F2PreparedToken;
import ru.inversion.f2.prepared.F2StyledDocument;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class F2StyledDocumentDumperSmoke {

    private static final String DEFAULT_INI_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\ALTPRNT5.INI";

    public static void main(String[] args) {


        String text = null;
        F2CommandRegistry registry;
        try {
            registry = F2CommandRegistry.make(createModel());
            text = new String ( Files.readAllBytes( Paths.get("d:\\Java\\Projects\\f2\\src\\test\\ae100020_5012.dat")), Charset.forName("windows-1251") );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<F2PreparedToken> tokens =
                new F2PreparedTextParser().parse(text);

        F2StyledDocument doc =
                new F2PreparedTextInterpreter().interpret(tokens, registry);

        String dump = F2StyledDocumentDumper.dump(doc);

        System.out.println(dump);

        assertContains(dump, "pages=2");
        assertContains(dump, "PAGE 1");
        assertContains(dump, "PAGE 2");
        assertContains(dump, "underline=true");
        assertContains(dump, "bold=true");

        System.out.println("F2 styled document dumper smoke OK");
    }

    private static F2AltIniModel createModel() throws Exception {

                return new F2AltIniModelLoader().load(
                        Paths.get(DEFAULT_INI_FILE),
                        Charset.forName("windows-1251")
                );
    }

    private static void assertContains(String text, String part) {
        if (text == null || !text.contains(part)) {
            throw new IllegalStateException(
                    "Expected dump to contain [" + part + "]"
            );
        }
    }
}