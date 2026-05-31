package ru.inversion.f2.awt;

import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.ini.F2AltIniModelLoader;
import ru.inversion.f2.prepared.F2PreparedTextInterpreter;
import ru.inversion.f2.prepared.F2PreparedTextParser;
import ru.inversion.f2.prepared.F2PreparedToken;
import ru.inversion.f2.prepared.F2StyledDocument;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class F2AwtPrintObjectsSmoke {

    public static void main(String[] args) throws Exception {

        F2CommandRegistry registry =
                F2CommandRegistry.make(createModel());

        F2StyledDocument document =
                parseAndInterpret(createText(), registry);

        F2AwtPageRenderConfig config =
                F2AwtPageRenderConfig.a4Portrait()
                        .withDpi(144.0d)
                        .withDebugOverlay(true)       .withShrinkToFit(true);

        smokePreviewRenderer(document, config);
        smokePageable(document, config);
        smokeDocumentPrintable(document, config);

        System.out.println("F2 AWT print objects smoke OK");
    }

    private static String createText() throws IOException {
        return Files.readString( Paths.get("d:\\Java\\Projects\\f2\\src\\test\\ae100020_5012.dat"), Charset.forName("windows-1251"));
    }

    private static F2AltIniModel createModel() throws Exception {

        Map<String, String> graphics =
                new LinkedHashMap<String, String>();

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

//        return new F2MapAltIniModel(
//                Collections.<String, String>emptyMap(),
//                Collections.<String, String>emptyMap(),
//                graphics,
//                Collections.<String, String>emptyMap()
//        );

        return new F2AltIniModelLoader().load( Paths.get("d:\\Java\\Projects\\f2\\src\\test\\ALTPRNT5.INI"), Charset.forName("windows-1251"));

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

    private static void smokePreviewRenderer(
            F2StyledDocument document,
            F2AwtPageRenderConfig config
    ) throws Exception {

        assertEquals(Integer.valueOf(1), Integer.valueOf(document.pageCount()));

        F2AwtPreviewRenderer renderer =
                new F2AwtPreviewRenderer();

        Path outDir = Paths.get("target");
        Files.createDirectories(outDir);

        for (int i = 0; i < document.pageCount(); i++) {
            BufferedImage image = renderer.render(
                    document.pages().get(i),
                    config
            );

            assertEquals(Integer.valueOf(config.imageWidthPx()), Integer.valueOf(image.getWidth()));
            assertEquals(Integer.valueOf(config.imageHeightPx()), Integer.valueOf(image.getHeight()));

            Path out = outDir.resolve("f2-awt-preview-page-" + (i + 1) + ".png");

            ImageIO.write(image, "png", out.toFile());

            System.out.println("preview page written: " + out);
        }

        System.out.println("F2AwtPreviewRenderer OK");
    }

    private static void smokePageable(
            F2StyledDocument document,
            F2AwtPageRenderConfig config
    ) {
        PageFormat sourcePageFormat =
                config.toPageFormat();

        F2AwtPageable pageable =
                new F2AwtPageable(document, sourcePageFormat);

        assertEquals(Integer.valueOf(2), Integer.valueOf(pageable.getNumberOfPages()));

        PageFormat pageFormat0 =
                pageable.getPageFormat(0);

        PageFormat pageFormat1 =
                pageable.getPageFormat(1);

        if (pageFormat0 == sourcePageFormat) {
            throw new IllegalStateException(
                    "Expected cloned PageFormat, actual same instance as source"
            );
        }

        if (pageFormat0 == pageFormat1) {
            throw new IllegalStateException(
                    "Expected cloned PageFormat per call, actual same instance"
            );
        }

        assertDoubleEquals(sourcePageFormat.getWidth(), pageFormat0.getWidth());
        assertDoubleEquals(sourcePageFormat.getHeight(), pageFormat0.getHeight());
        assertDoubleEquals(sourcePageFormat.getImageableX(), pageFormat0.getImageableX());
        assertDoubleEquals(sourcePageFormat.getImageableY(), pageFormat0.getImageableY());

        Printable printable0 =
                pageable.getPrintable(0);

        Printable printable1 =
                pageable.getPrintable(1);

        if (printable0 == null || printable1 == null)
            throw new IllegalStateException("Printable is null");

        try {
            pageable.getPageFormat(2);
            throw new IllegalStateException("Invalid page index was accepted by getPageFormat");
        }
        catch (IndexOutOfBoundsException expected) {
            // ok
        }

        try {
            pageable.getPrintable(2);
            throw new IllegalStateException("Invalid page index was accepted by getPrintable");
        }
        catch (IndexOutOfBoundsException expected) {
            // ok
        }

        System.out.println("F2AwtPageable OK");
    }

    private static void smokeDocumentPrintable(
            F2StyledDocument document,
            F2AwtPageRenderConfig config
    ) {

        F2AwtDocumentPrintable printable =
                new F2AwtDocumentPrintable(document);

        PageFormat pageFormat =
                config.toPageFormat();

        BufferedImage image = new BufferedImage(
                (int) Math.ceil(pageFormat.getWidth()),
                (int) Math.ceil(pageFormat.getHeight()),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = image.createGraphics();

        try {
            int page0 = printable.print(g, pageFormat, 0);
            int page1 = printable.print(g, pageFormat, 1);
            int page2 = printable.print(g, pageFormat, 2);

            assertEquals(Integer.valueOf(Printable.PAGE_EXISTS), Integer.valueOf(page0));
            assertEquals(Integer.valueOf(Printable.PAGE_EXISTS), Integer.valueOf(page1));
            assertEquals(Integer.valueOf(Printable.NO_SUCH_PAGE), Integer.valueOf(page2));
        }
        finally {
            g.dispose();
        }

        System.out.println("F2AwtDocumentPrintable OK");
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