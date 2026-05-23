package ru.inversion.f2.style;

import ru.inversion.f2.command.F2CommandCallParser;
import ru.inversion.f2.command.F2CommandRef;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.ini.F2MapAltIniModel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class F2StyleProgramSmoke {

    public static void main(String[] args) {

        F2AltIniModel model = createModel();
        F2CommandRegistry registry = F2CommandRegistry.from(model);

        smokeUnderline(registry);
        smokeBoldItalic(registry);
        smokeNormalWithCmd(registry);
        smokeVerticalMoveConst(registry);
        smokeVerticalMoveFromArg(registry);
        smokeLeft(registry);
        smokePageEndNoOp(registry);
        smokeUnknownCommand(registry);

        System.out.println("F2 style program smoke OK");
    }

    private static F2AltIniModel createModel() {

        Map<String, String> graphics = new LinkedHashMap<String, String>();

        graphics.put("UNDER+", "Under=Yes;");
        graphics.put("UNDER-", "Under=No;");

        graphics.put("BOLD+", "Bold=Yes;");
        graphics.put("BOLD-", "Bold=No;");

        graphics.put("ITALIC+", "Italic=Yes;");
        graphics.put("ITALIC-", "Italic=No;");

        graphics.put("INTERVAL_6", "Vertical Move=1/6;");
        graphics.put("INTERVAL_8", "Vertical Move=1/8;");
        graphics.put("VERT_72", "Vertical Move=n/72;");

        graphics.put("LEFT_15", "Left=15;");

        graphics.put(
                "NORMAL",
                "Name Font=Courier New;Size Font=10;Bold=No;Italic=No;Under=No;Cmd=`INTERVAL_6`;"
        );

        graphics.put("PAGE_END", "Page End=Yes;");
        graphics.put("FF", "Cmd=`PAGE_END`;");

        return new F2MapAltIniModel(
                Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(),
                graphics,
                Collections.<String, String>emptyMap()
        );
    }

    private static void smokeUnderline(F2CommandRegistry registry) {

        F2RenderState state = F2RenderState.initial();

        state = apply(registry, "`UNDER+`", state);
        assertEquals(Boolean.TRUE, Boolean.valueOf(state.style().underline()));

        state = apply(registry, "`UNDER-`", state);
        assertEquals(Boolean.FALSE, Boolean.valueOf(state.style().underline()));

        System.out.println("UNDER +/- OK");
    }

    private static void smokeBoldItalic(F2CommandRegistry registry) {

        F2RenderState state = F2RenderState.initial();

        state = apply(registry, "`BOLD+`", state);
        assertEquals(Boolean.TRUE, Boolean.valueOf(state.style().bold()));

        state = apply(registry, "`ITALIC+`", state);
        assertEquals(Boolean.TRUE, Boolean.valueOf(state.style().italic()));

        state = apply(registry, "`BOLD-`", state);
        assertEquals(Boolean.FALSE, Boolean.valueOf(state.style().bold()));

        state = apply(registry, "`ITALIC-`", state);
        assertEquals(Boolean.FALSE, Boolean.valueOf(state.style().italic()));

        System.out.println("BOLD / ITALIC OK");
    }

    private static void smokeNormalWithCmd(F2CommandRegistry registry) {

        F2RenderState state = F2RenderState.initial();

        state = apply(registry, "`UNDER+`", state);
        state = apply(registry, "`BOLD+`", state);
        state = apply(registry, "`ITALIC+`", state);

        state = apply(registry, "`NORMAL`", state);

        assertEquals("Courier New", state.style().fontName());
        assertEquals(Integer.valueOf(10), Integer.valueOf(state.style().fontSize()));
        assertEquals(Boolean.FALSE, Boolean.valueOf(state.style().bold()));
        assertEquals(Boolean.FALSE, Boolean.valueOf(state.style().italic()));
        assertEquals(Boolean.FALSE, Boolean.valueOf(state.style().underline()));

        /*
         * NORMAL содержит Cmd=`INTERVAL_6`,
         * INTERVAL_6 = Vertical Move=1/6,
         * 72 / 6 = 12 pt.
         */
        assertDoubleEquals(12.0d, state.lineStepPt());

        System.out.println("NORMAL + Cmd=`INTERVAL_6` OK");
    }

    private static void smokeVerticalMoveConst(F2CommandRegistry registry) {

        F2RenderState state = F2RenderState.initial();

        state = apply(registry, "`INTERVAL_8`", state);

        assertDoubleEquals(9.0d, state.lineStepPt());

        System.out.println("Vertical Move=1/8 OK");
    }

    private static void smokeVerticalMoveFromArg(F2CommandRegistry registry) {

        F2RenderState state = F2RenderState.initial();

        state = apply(registry, "`VERT_72,18`", state);

        /*
         * n/72 inch -> 72 * n / 72 = n pt.
         */
        assertDoubleEquals(18.0d, state.lineStepPt());

        System.out.println("Vertical Move=n/72 OK");
    }

    private static void smokeLeft(F2CommandRegistry registry) {

        F2RenderState state = F2RenderState.initial();

        state = apply(registry, "`LEFT_15`", state);

        assertDoubleEquals(15.0d, state.leftIndentPt());

        System.out.println("LEFT OK");
    }

    private static void smokePageEndNoOp(F2CommandRegistry registry) {

        F2RenderState state = F2RenderState.initial();

        double beforeLineStep = state.lineStepPt();
        double beforeLeft = state.leftIndentPt();
        String beforeFont = state.style().fontName();

        state = apply(registry, "`FF`", state);

        /*
         * Пока PAGE_END это flow-event и no-op для F2RenderState.
         * Главное здесь: Cmd=`PAGE_END` должен разрешиться без ошибки.
         */
        assertDoubleEquals(beforeLineStep, state.lineStepPt());
        assertDoubleEquals(beforeLeft, state.leftIndentPt());
        assertEquals(beforeFont, state.style().fontName());

        System.out.println("FF -> PAGE_END no-op OK");
    }

    private static void smokeUnknownCommand(F2CommandRegistry registry) {

        try {
            apply(registry, "`NO_SUCH_COMMAND`", F2RenderState.initial());
            throw new IllegalStateException("Unknown command was resolved unexpectedly");
        }
        catch (RuntimeException expected) {
            System.out.println("unknown command OK");
        }
    }

    private static F2RenderState apply(
            F2CommandRegistry registry,
            String commandText,
            F2RenderState state
    ) {
        F2CommandRef ref = registry.resolve(
                F2CommandCallParser.parse(commandText)
        );

        return ref.def().styleProgram().apply(
                ref.call(),
                state,
                registry
        );
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