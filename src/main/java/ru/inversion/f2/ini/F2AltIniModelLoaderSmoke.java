package ru.inversion.f2.ini;

import ru.inversion.f2.command.*;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class F2AltIniModelLoaderSmoke {

    public static void main(String[] args) throws Exception {

        Charset charset = Charset.forName("Windows-1251");

        Path ini = Files.createTempFile("altprnt5-smoke-", ".ini");

       // writeSmokeIni(ini, charset);

        F2AltIniModel model = new F2AltIniModelLoader()
                .load(Paths.get("d:\\Инверсия\\java\\ALTPRNT5.INI"), charset);

        smokeSections(model);
        smokeCodeGraphics(model);
        smokeCodeText(model);
        smokeRegistryFromLoadedModel(model);

        Files.deleteIfExists(ini);

        System.out.println("F2AltIniModelLoader smoke OK");
    }

    private static void writeSmokeIni(Path ini, Charset charset) throws Exception {

        try (BufferedWriter w = Files.newBufferedWriter(ini, charset)) {

            w.write("[Commands]\n");
            w.write("UNDER+ = underline on\n");
            w.write("NORMAL = normal style\n");
            w.write("\n");

            /*
             * Историческая опечатка должна поддерживаться.
             */
            w.write("[CodeGraphincs]\n");
            w.write("UNDER+ = Under=Yes;\n");
            w.write("UNDER- = Under=No;\n");
            w.write("NORMAL = Name Font=Courier New;Size Font=10;Bold=No;Italic=No;Under=No;Cmd=`INTERVAL_6`;\n");
            w.write("INTERVAL_6 = Vertical Move=1/6;\n");
            w.write("PAGE_END = Page End=Yes;\n");
            w.write("FF = Cmd=`PAGE_END`;\n");
            w.write("\n");

            w.write("[CodeText]\n");
            w.write("UNDER+ = \\d027\\c-\\c1\n");
            w.write("UNDER- = \\d027\\c-\\c0\n");
            w.write("PAGE_END = \\d012\n");
            w.write("\n");

            w.write("[DriverRef]\n");
            w.write("EPSON = FX-890\n");
        }
    }

    private static void smokeSections(F2AltIniModel model) {

        assertEquals("underline on", model.commandDescription("UNDER+"));
        assertEquals("normal style", model.commandDescription("NORMAL"));
        assertEquals("FX-890", model.driverRef("EPSON"));

        System.out.println("sections OK");
    }

    private static void smokeCodeGraphics(F2AltIniModel model) {

        assertEquals("Under=Yes;", model.codeGraphics("UNDER+"));
        assertEquals("Under=No;", model.codeGraphics("UNDER-"));

        String normal = model.codeGraphics("NORMAL");

        assertEquals(
                "Name Font=Courier New;Size Font=10;Bold=No;Italic=No;Under=No;Cmd=`INTERVAL_6`;",
                normal
        );

        /*
         * Критично: semicolonPartOfValue=true.
         * NORMAL не должен быть обрезан до "Name Font=Courier New".
         */
        if (!normal.contains("Size Font=10")
                || !normal.contains("Cmd=`INTERVAL_6`")) {
            throw new IllegalStateException("NORMAL was cut by semicolon: " + normal);
        }

        System.out.println("CodeGraphincs/CodeGraphics OK");
    }

    private static void smokeCodeText(F2AltIniModel model) {

        assertEquals("\\d027\\c-\\c1", model.codeText("UNDER+"));
        assertEquals("\\d027\\c-\\c0", model.codeText("UNDER-"));
        assertEquals("\\d012", model.codeText("PAGE_END"));

        System.out.println("CodeText raw escapes OK");
    }

    private static void smokeRegistryFromLoadedModel(F2AltIniModel model) {

        F2CommandRegistry registry = F2CommandRegistry.make(model);

        F2CommandRef under = registry.resolve(
                F2CommandCall.parse("`UNDER+`")
        );

        assertEquals("UNDER+", under.def().name());
        assertEquals(F2CommandProperty.UNDER, under.def().properties().get(0).property());
        assertEquals(Boolean.TRUE, under.def().properties().get(0).value());

        F2CommandRef ff = registry.resolve(
                F2CommandCall.parse("`FF`")
        );

        F2CommandPropertyValue pv = ff.def().properties().get(0);

        assertEquals(F2CommandProperty.CMD, pv.property());

        F2CommandCall target = (F2CommandCall) pv.value();

        assertEquals("PAGE_END", target.name());

        System.out.println("Registry from loaded model OK");
    }

    private static void assertEquals(Object expected, Object actual) {

        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new IllegalStateException(
                    "Expected [" + expected + "], actual [" + actual + "]"
            );
        }
    }
}