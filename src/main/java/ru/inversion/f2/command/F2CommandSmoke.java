package ru.inversion.f2.command;

import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.ini.F2MapAltIniModel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class F2CommandSmoke {

    public static void main(String[] args) {

        Map<String, String> graphics = new LinkedHashMap<String, String>();

        graphics.put("UNDER+", "Under=Yes;");
        graphics.put("UNDER-", "Under=No;");
        graphics.put("BOLD", "Bold=Yes;");
        graphics.put("NORMAL", "Name Font=Courier New;Size Font=10;Bold=No;Italic=No;Under=No;Cmd=`INTERVAL_6`;");
        graphics.put("INTERVAL_6", "Vertical Move=1/6;");
        graphics.put("PAGE_END", "Page End=Yes;");
        graphics.put("FF", "Cmd=`PAGE_END`;");

        Map<String, String> text = new LinkedHashMap<String, String>();

        text.put("UNDER+", "\\d027\\c-\\c1");
        text.put("UNDER-", "\\d027\\c-\\c0");

        F2AltIniModel model = new F2MapAltIniModel(
                Collections.<String, String>emptyMap(),
                text,
                graphics,
                Collections.<String, String>emptyMap()
        );

        F2CommandRegistry registry = F2CommandRegistry.make(model);

        smokeUnderPlus(registry);
        smokeNormal(registry);
        smokeFF(registry);
        smokeUnknownCommand(registry);

        System.out.println("F2 command smoke OK");
    }

    private static void smokeUnderPlus(F2CommandRegistry registry) {

        F2CommandCall call = F2CommandCall.parse("`UNDER+`");
        F2CommandRef ref = registry.resolve(call);

        assertEquals("UNDER+", ref.name());
        assertEquals("UNDER+", ref.def().name());

        List<F2CommandPropertyValue> props = ref.def().properties();

        assertEquals(1, props.size());

        F2CommandPropertyValue p = props.get(0);

        assertEquals(F2CommandProperty.UNDER, p.property());
        assertEquals("Under", p.rawName());
        assertEquals("Yes", p.rawValue());
        assertEquals(Boolean.TRUE, p.value());

        System.out.println("UNDER+ OK");
    }

    private static void smokeNormal(F2CommandRegistry registry) {

        F2CommandDef def = registry.find("NORMAL");

        assertNotNull(def, "NORMAL not found");

        List<F2CommandPropertyValue> props = def.properties();

        assertEquals(6, props.size());

        assertProperty(props, 0, F2CommandProperty.FONT_NAME, "Courier New");
        assertProperty(props, 1, F2CommandProperty.FONT_SIZE, Integer.valueOf(10));
        assertProperty(props, 2, F2CommandProperty.BOLD, Boolean.FALSE);
        assertProperty(props, 3, F2CommandProperty.ITALIC, Boolean.FALSE);
        assertProperty(props, 4, F2CommandProperty.UNDER, Boolean.FALSE);

        F2CommandPropertyValue cmd = props.get(5);

        assertEquals(F2CommandProperty.CMD, cmd.property());

        Object value = cmd.value();

        if (!(value instanceof F2CommandCall))
            throw new IllegalStateException("NORMAL Cmd value is not F2CommandCall: " + value);

        F2CommandCall target = (F2CommandCall) value;

        assertEquals("INTERVAL_6", target.name());
        assertEquals(0, target.args().size());

        System.out.println("NORMAL OK");
    }

    private static void smokeFF(F2CommandRegistry registry) {

        F2CommandDef def = registry.find("FF");

        assertNotNull(def, "FF not found");

        List<F2CommandPropertyValue> props = def.properties();

        assertEquals(1, props.size());

        F2CommandPropertyValue p = props.get(0);

        assertEquals(F2CommandProperty.CMD, p.property());

        F2CommandCall target = (F2CommandCall) p.value();

        assertEquals("PAGE_END", target.name());

        F2CommandRef resolvedTarget = registry.resolve(target);

        assertEquals("PAGE_END", resolvedTarget.def().name());

        F2CommandPropertyValue pageEnd = resolvedTarget.def().properties().get(0);

        assertEquals(F2CommandProperty.PAGE_END, pageEnd.property());
        assertEquals(Boolean.TRUE, pageEnd.value());

        System.out.println("FF -> PAGE_END OK");
    }

    private static void smokeUnknownCommand(F2CommandRegistry registry) {

        try {
            registry.resolve(F2CommandCall.parse("`NO_SUCH_COMMAND`"));
            throw new IllegalStateException("Unknown command was resolved unexpectedly");
        }
        catch (IllegalStateException expected) {
            System.out.println("unknown command OK");
        }
    }

    private static void smokeSections(F2AltIniModel model) {

        assertNotEmpty(model.commandDescription("UNDER+"), "UNDER+ description is empty");

        System.out.println("UNDER+ description = " + model.commandDescription("UNDER+"));
        System.out.println("sections OK");
    }

    private static void assertProperty(
            List<F2CommandPropertyValue> props,
            int index,
            F2CommandProperty expectedProperty,
            Object expectedValue
    ) {
        F2CommandPropertyValue p = props.get(index);

        assertEquals(expectedProperty, p.property());
        assertEquals(expectedValue, p.value());
    }

    private static void assertNotEmpty(String value, String message) {
        if (value == null || value.trim().length() == 0)
            throw new IllegalStateException(message);
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new IllegalStateException(
                    "Expected [" + expected + "], actual [" + actual + "]"
            );
        }
    }

    private static void assertNotNull(Object value, String message) {
        if (value == null)
            throw new IllegalStateException(message);
    }
}