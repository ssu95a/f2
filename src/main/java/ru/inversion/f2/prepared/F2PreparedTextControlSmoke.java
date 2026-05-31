package ru.inversion.f2;

import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.ini.F2MapAltIniModel;
import ru.inversion.f2.prepared.F2PreparedTextInterpreter;
import ru.inversion.f2.prepared.F2PreparedTextParser;
import ru.inversion.f2.prepared.F2PreparedToken;
import ru.inversion.f2.prepared.F2StyledDocument;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class F2PreparedTextControlSmoke {

    public static void main(String[] args) {

        F2CommandRegistry registry = F2CommandRegistry.make(createModel());

        smokePageEnd(registry);
        smokeDirectPageEnd(registry);
        smokeLineFeed(registry);
        smokePageEndKeepsStyle(registry);

        System.out.println("F2 prepared text control smoke OK");
    }

    private static F2AltIniModel createModel() {

        Map<String, String> graphics = new LinkedHashMap<String, String>();

        graphics.put("UNDER+", "Under=Yes;");
        graphics.put("UNDER-", "Under=No;");

        graphics.put("PAGE_END", "Page End=Yes;");
        graphics.put("FF", "Cmd=`PAGE_END`;");

        graphics.put("LF", "Lf=Yes;");

        return new F2MapAltIniModel(
                Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(),
                graphics,
                Collections.<String, String>emptyMap()
        );
    }

    private static void smokePageEnd(F2CommandRegistry registry) {

        F2StyledDocument doc = parseAndInterpret("A`FF`B", registry);

        assertEquals(Integer.valueOf(2), Integer.valueOf(doc.pageCount()));

        assertEquals("A", doc.pages().get(0).lines().get(0).plainText());
        assertEquals("B", doc.pages().get(1).lines().get(0).plainText());

        System.out.println("FF -> PAGE_END OK");
    }

    private static void smokeDirectPageEnd(F2CommandRegistry registry) {

        F2StyledDocument doc = parseAndInterpret("A`PAGE_END`B", registry);

        assertEquals(Integer.valueOf(2), Integer.valueOf(doc.pageCount()));

        assertEquals("A", doc.pages().get(0).lines().get(0).plainText());
        assertEquals("B", doc.pages().get(1).lines().get(0).plainText());

        System.out.println("direct PAGE_END OK");
    }

    private static void smokeLineFeed(F2CommandRegistry registry) {

        F2StyledDocument doc = parseAndInterpret("A`LF`B", registry);

        assertEquals(Integer.valueOf(1), Integer.valueOf(doc.pageCount()));
        assertEquals(Integer.valueOf(2), Integer.valueOf(doc.pages().get(0).lineCount()));

        assertEquals("A", doc.pages().get(0).lines().get(0).plainText());
        assertEquals("B", doc.pages().get(0).lines().get(1).plainText());

        System.out.println("LF OK");
    }

    private static void smokePageEndKeepsStyle(F2CommandRegistry registry) {

        F2StyledDocument doc = parseAndInterpret("`UNDER+`A`FF`B", registry);

        assertEquals(Integer.valueOf(2), Integer.valueOf(doc.pageCount()));

        assertEquals("A", doc.pages().get(0).lines().get(0).plainText());
        assertEquals("B", doc.pages().get(1).lines().get(0).plainText());

        assertEquals(
                Boolean.TRUE,
                Boolean.valueOf(
                        doc.pages().get(0)
                                .lines().get(0)
                                .chunks().get(0)
                                .style()
                                .underline()
                )
        );

        assertEquals(
                Boolean.TRUE,
                Boolean.valueOf(
                        doc.pages().get(1)
                                .lines().get(0)
                                .chunks().get(0)
                                .style()
                                .underline()
                )
        );

        System.out.println("PAGE_END keeps style OK");
    }

    private static F2StyledDocument parseAndInterpret(
            String text,
            F2CommandRegistry registry
    ) {
        List<F2PreparedToken> tokens =
                new F2PreparedTextParser().parse(text);

        return new F2PreparedTextInterpreter()
                .interpret(tokens, registry);
    }

    private static void assertEquals(Object expected, Object actual) {

        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new IllegalStateException(
                    "Expected [" + expected + "], actual [" + actual + "]"
            );
        }
    }
}