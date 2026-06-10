package ru.inversion.f2.awt;

import org.junit.Assume;
import org.junit.Test;
import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.ini.F2AltIniModelLoader;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.f2.print.F2PrintService;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;

/**
 * Ручной smoke для визуальной сверки preview geometry.
 *
 * Запускается из IDEA без -D: ниже есть DEFAULT_* значения.
 * Через -D можно переопределить любой параметр.
 *
 * Опционально:
 *   -Df2.preview.smoke.input=/path/to/prepared.txt
 *   -Df2.preview.smoke.ini=/path/to/ALTPRNT5.INI
 *   -Df2.preview.smoke.output=target/f2-preview-debug-page-1.png
 *   -Df2.preview.smoke.printer="Microsoft Print to PDF"
 *   -Df2.preview.smoke.dpi=144
 *   -Df2.preview.smoke.charset=windows-1251
 *   -Df2.preview.smoke.ini.charset=windows-1251
 *   -Df2.preview.smoke.media.printable.mm=5,5,200,287
 *   -Df2.preview.smoke.override.printable=false
 *   -Df2.preview.smoke.margin.mm=5
 *   -Df2.preview.smoke.paper.width.mm=210
 *   -Df2.preview.smoke.paper.height.mm=297
 */
public class F2AwtPreviewManualSmokeTest {

    private static final String DEFAULT_INPUT_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\ae100020_5012.dat";

    private static final String DEFAULT_INI_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\ALTPRNT5.INI";

    private static final String DEFAULT_OUTPUT_FILE =
            "target/f2-preview-debug-page-1.png";

    private static final String DEFAULT_PRINTER_NAME =
            "Microsoft Print to PDF";

    private static final String DEFAULT_TEXT_CHARSET =
            "windows-1251";

    private static final String DEFAULT_INI_CHARSET =
            "windows-1251";

    private static final String DEFAULT_DPI =
            "144";

    private static final String DEFAULT_OVERRIDE_PRINTABLE =
            "true";

    private static final String DEFAULT_MARGIN_MM =
            "5";

    private static final String DEFAULT_PAPER_WIDTH_MM =
            "210";

    private static final String DEFAULT_PAPER_HEIGHT_MM =
            "297";

    @Test
    public void renderFirstRealDocumentPageWithDebugOverlay() throws Exception {

        String inputFileName = stringProperty(
                "f2.preview.smoke.input",
                DEFAULT_INPUT_FILE
        );

        String iniFileName = stringProperty(
                "f2.preview.smoke.ini",
                DEFAULT_INI_FILE
        );

        Assume.assumeTrue(
                "Prepared text file does not exist: " + inputFileName,
                inputFileName != null && inputFileName.trim().length() > 0
        );

        Assume.assumeTrue(
                "INI file does not exist: " + iniFileName,
                iniFileName != null && iniFileName.trim().length() > 0
        );

        Path inputPath = Paths.get(inputFileName);
        Path iniPath = Paths.get(iniFileName);

        Assume.assumeTrue("Prepared text file does not exist: " + inputPath, Files.isRegularFile(inputPath));
        Assume.assumeTrue("INI file does not exist: " + iniPath, Files.isRegularFile(iniPath));

        Charset textCharset = charsetProperty(
                "f2.preview.smoke.charset",
                DEFAULT_TEXT_CHARSET
        );

        Charset iniCharset = charsetProperty(
                "f2.preview.smoke.ini.charset",
                DEFAULT_INI_CHARSET
        );

        F2Runtime.init(
                new F2AltIniModelLoader().load(
                        iniPath,
                        iniCharset
                )
        );

        String printerName = stringProperty(
                "f2.preview.smoke.printer",
                DEFAULT_PRINTER_NAME
        );

        if (printerName != null && printerName.trim().length() > 0) {
            F2Runtime.get()
                    .printerMan()
                    .selectPrinterName(printerName.trim());
        }

        applyPrintableAreaOverrideIfRequested();

        String text = new String(
                Files.readAllBytes(inputPath),
                textCharset
        );

        F2PrintService printService =
                new F2PrintService();

        F2StyledDocument document =
                printService.prepareDocument(text);

        assertFalse(
                "Prepared document has no pages",
                document.isEmpty()
        );

        F2PrintPageSetup setup =
                F2Runtime.get()
                        .printerMan()
                        .currentPrintPageSetup();

        double dpi = Double.parseDouble(
                stringProperty(
                        "f2.preview.smoke.dpi",
                        DEFAULT_DPI
                )
        );

        BufferedImage image =
                new F2AwtPreviewRenderer().render(
                        document.pages().get(0),
                        setup,
                        dpi,
                        true
                );

        Path outputPath = Paths.get(
                stringProperty(
                        "f2.preview.smoke.output",
                        DEFAULT_OUTPUT_FILE
                )
        );

        Path parent = outputPath.getParent();

        if (parent != null)
            Files.createDirectories(parent);

        ImageIO.write(
                image,
                "png",
                outputPath.toFile()
        );

        System.out.println("F2 preview smoke PNG: " + outputPath.toAbsolutePath());
        System.out.println("F2 preview smoke setup: " + setup.geometryToString());
    }

    private static void applyPrintableAreaOverrideIfRequested() {

        if (!booleanProperty(
                "f2.preview.smoke.override.printable",
                DEFAULT_OVERRIDE_PRINTABLE
        )) {
            System.out.println("F2 preview smoke mediaPrintableArea override: disabled");
            return;
        }

        MediaPrintableArea mediaPrintableArea =
                mediaPrintableAreaFromProperty();

        if (mediaPrintableArea == null)
            mediaPrintableArea = mediaPrintableAreaFromMarginDefaults();

        HashPrintRequestAttributeSet attributes =
                new HashPrintRequestAttributeSet();

        attributes.add(mediaPrintableArea);

        F2Runtime.get()
                .printerMan()
                .selectPrintAttributes(attributes);

        System.out.println("F2 preview smoke mediaPrintableArea override: " + mediaPrintableArea);
    }

    private static MediaPrintableArea mediaPrintableAreaFromProperty() {

        String value = System.getProperty("f2.preview.smoke.media.printable.mm");

        if (value == null || value.trim().length() == 0)
            return null;

        String[] parts = value.split(",");

        if (parts.length != 4)
            throw new IllegalArgumentException(
                    "f2.preview.smoke.media.printable.mm must be x,y,width,height in mm"
            );

        return new MediaPrintableArea(
                (float) parseNonNegativeDouble(parts[0], "mediaPrintableArea.x"),
                (float) parseNonNegativeDouble(parts[1], "mediaPrintableArea.y"),
                (float) parsePositiveDouble(parts[2], "mediaPrintableArea.width"),
                (float) parsePositiveDouble(parts[3], "mediaPrintableArea.height"),
                MediaPrintableArea.MM
        );
    }

    private static MediaPrintableArea mediaPrintableAreaFromMarginDefaults() {

        double marginMm = parseNonNegativeDouble(
                stringProperty(
                        "f2.preview.smoke.margin.mm",
                        DEFAULT_MARGIN_MM
                ),
                "f2.preview.smoke.margin.mm"
        );

        double paperWidthMm = parsePositiveDouble(
                stringProperty(
                        "f2.preview.smoke.paper.width.mm",
                        DEFAULT_PAPER_WIDTH_MM
                ),
                "f2.preview.smoke.paper.width.mm"
        );

        double paperHeightMm = parsePositiveDouble(
                stringProperty(
                        "f2.preview.smoke.paper.height.mm",
                        DEFAULT_PAPER_HEIGHT_MM
                ),
                "f2.preview.smoke.paper.height.mm"
        );

        double printableWidthMm = paperWidthMm - marginMm * 2.0d;
        double printableHeightMm = paperHeightMm - marginMm * 2.0d;

        if (printableWidthMm <= 0.0d || printableHeightMm <= 0.0d) {
            throw new IllegalArgumentException(
                    "Printable area is empty: paper="
                            + paperWidthMm
                            + "x"
                            + paperHeightMm
                            + " mm, margin="
                            + marginMm
                            + " mm"
            );
        }

        return new MediaPrintableArea(
                (float) marginMm,
                (float) marginMm,
                (float) printableWidthMm,
                (float) printableHeightMm,
                MediaPrintableArea.MM
        );
    }

    private static boolean booleanProperty(
            String name,
            String defaultValue
    ) {
        return Boolean.parseBoolean(
                stringProperty(
                        name,
                        defaultValue
                )
        );
    }

    private static String stringProperty(
            String name,
            String defaultValue
    ) {
        String value = System.getProperty(name);

        if (value == null || value.trim().length() == 0)
            return defaultValue;

        return value.trim();
    }

    private static double parsePositiveDouble(
            String value,
            String name
    ) {
        double result = parseNonNegativeDouble(value, name);

        if (result <= 0.0d)
            throw new IllegalArgumentException(name + " <= 0");

        return result;
    }

    private static double parseNonNegativeDouble(
            String value,
            String name
    ) {
        if (value == null || value.trim().length() == 0)
            throw new IllegalArgumentException(name + " is empty");

        double result = Double.parseDouble(value.trim().replace(',', '.'));

        if (result < 0.0d)
            throw new IllegalArgumentException(name + " < 0");

        return result;
    }

    private static Charset charsetProperty(
            String name,
            String defaultValue
    ) {
        return Charset.forName(
                stringProperty(
                        name,
                        defaultValue
                )
        );
    }
}
