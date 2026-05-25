package ru.inversion.f2;

import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.diagnostic.F2StyledDocumentDumper;
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

public final class F2StyledDocumentDumperSmoke {

    public static void main(String[] args) {

        F2CommandRegistry registry = F2CommandRegistry.from(createModel());

        String text =
                "`NORMAL`Получатель платежа: ОАО `BOLD+`ТЕПЛО-ЭНЕРГЕТИК`BOLD-`\n"
                        + "Ф.И.О. `UNDER+`              `UNDER-`|\n"
                        + "Сумма платежа `UNDER+`12-00`UNDER-`\n"
                        + "`FF`"
                        + "Квитанция\n"
                        + "Подпись `UNDER+`          `UNDER-`";

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

    private static F2AltIniModel createModel() {

        Map<String, String> graphics = new LinkedHashMap<String, String>();

        graphics.put("UNDER+", "Under=Yes;");
        graphics.put("UNDER-", "Under=No;");

        graphics.put("BOLD+", "Bold=Yes;");
        graphics.put("BOLD-", "Bold=No;");

        graphics.put("INTERVAL_6", "Vertical Move=1/6;");

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

    private static void assertContains(String text, String part) {
        if (text == null || !text.contains(part)) {
            throw new IllegalStateException(
                    "Expected dump to contain [" + part + "]"
            );
        }
    }
}