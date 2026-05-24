package ru.inversion.f2.prepared;

import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.ini.F2MapAltIniModel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class F2PreparedTextInterpreterSmoke {

    public static void main(String[] args) {

        F2CommandRegistry registry = F2CommandRegistry.from(createModel());

        smokeUnderlineLine(registry);
        smokeNormalAndLineStep(registry);
        smokeNewLine(registry);

        System.out.println("F2 prepared text interpreter smoke OK");
    }

    private static F2AltIniModel createModel() {
        Map<String, String> graphics = new LinkedHashMap<String, String>();

        graphics.put("UNDER+", "Under=Yes;");
        graphics.put("UNDER-", "Under=No;");
        graphics.put("BOLD+", "Bold=Yes;");
        graphics.put("BOLD-", "Bold=No;");
        graphics.put("INTERVAL_6", "Vertical Move=1/6;");
        graphics.put("NORMAL", "Name Font=Courier New;Size Font=10;Bold=No;Italic=No;Under=No;Cmd=`INTERVAL_6`;");

        return new F2MapAltIniModel(
                Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(),
                graphics,
                Collections.<String, String>emptyMap()
        );
    }

    private static void smokeUnderlineLine(F2CommandRegistry registry) {

        F2StyledDocument doc = parseAndInterpret(
                "Ф.И.О. `UNDER+`     `UNDER-`|",
                registry
        );

        assertEquals(Integer.valueOf(1), Integer.valueOf(doc.lineCount()));

        F2StyledLine line = doc.lines().get(0);

        assertEquals("Ф.И.О.      |", line.plainText());
        assertEquals(Integer.valueOf(3), Integer.valueOf(line.runs().size()));

        F2StyledTextRun r0 = line.runs().get(0);
        F2StyledTextRun r1 = line.runs().get(1);
        F2StyledTextRun r2 = line.runs().get(2);

        assertEquals("Ф.И.О. ", r0.text());
        assertEquals(Boolean.FALSE, Boolean.valueOf(r0.style().underline()));

        assertEquals("     ", r1.text());
        assertEquals(Boolean.TRUE, Boolean.valueOf(r1.style().underline()));

        assertEquals("|", r2.text());
        assertEquals(Boolean.FALSE, Boolean.valueOf(r2.style().underline()));

        System.out.println("underline styled line OK");
    }

    private static void smokeNormalAndLineStep(F2CommandRegistry registry) {

        F2StyledDocument doc = parseAndInterpret(
                "`BOLD+`A`NORMAL`B",
                registry
        );

        F2StyledLine line = doc.lines().get(0);

        assertEquals(Integer.valueOf(2), Integer.valueOf(line.runs().size()));

        F2StyledTextRun r0 = line.runs().get(0);
        F2StyledTextRun r1 = line.runs().get(1);

        assertEquals("A", r0.text());
        assertEquals(Boolean.TRUE, Boolean.valueOf(r0.style().bold()));

        assertEquals("B", r1.text());
        assertEquals(Boolean.FALSE, Boolean.valueOf(r1.style().bold()));
        assertEquals("Courier New", r1.style().fontName());
        assertEquals(Integer.valueOf(10), Integer.valueOf(r1.style().fontSize()));

        /*
         * NORMAL вызывает INTERVAL_6.
         * Но lineStep snapshot сохраняется на строке при завершении строки.
         */
        assertDoubleEquals(12.0d, line.lineStepPt());

        System.out.println("NORMAL style + line step OK");
    }

    private static void smokeNewLine(F2CommandRegistry registry) {

        F2StyledDocument doc = parseAndInterpret(
                "A\n`UNDER+`B",
                registry
        );

        assertEquals(Integer.valueOf(2), Integer.valueOf(doc.lineCount()));

        assertEquals("A", doc.lines().get(0).plainText());
        assertEquals("B", doc.lines().get(1).plainText());

        F2StyledTextRun b = doc.lines().get(1).runs().get(0);

        assertEquals(Boolean.TRUE, Boolean.valueOf(b.style().underline()));

        System.out.println("new line style carry OK");
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

    private static void assertDoubleEquals(double expected, double actual) {
        double diff = Math.abs(expected - actual);

        if (diff > 0.0001d) {
            throw new IllegalStateException(
                    "Expected [" + expected + "], actual [" + actual + "]"
            );
        }
    }
}