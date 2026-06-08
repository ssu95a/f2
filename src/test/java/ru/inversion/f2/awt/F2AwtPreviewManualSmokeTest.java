package ru.inversion.f2.awt;

import org.junit.Assume;
import org.junit.Test;
import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.f2.print.F2PrintService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertFalse;

/**
 * Ручной smoke для визуальной сверки preview geometry.
 *
 * Обычный mvn test этот тест пропускает. Для запуска передайте:
 *
 * mvn test -Dtest=F2AwtPreviewManualSmokeTest \
 *   -Df2.preview.smoke.input=/path/to/prepared.txt \
 *   -Df2.preview.smoke.ini=/path/to/ALTPRNT5.INI \
 *   -Df2.preview.smoke.output=target/f2-preview-debug-page-1.png
 *
 * Опционально:
 *   -Df2.preview.smoke.printer="Microsoft Print to PDF"
 *   -Df2.preview.smoke.dpi=144
 *   -Df2.preview.smoke.charset=windows-1251
 *   -Df2.preview.smoke.ini.charset=windows-1251
 */
public class F2AwtPreviewManualSmokeTest {

    @Test
    public void renderFirstRealDocumentPageWithDebugOverlay() throws Exception {

        String inputFileName = System.getProperty("f2.preview.smoke.input");
        String iniFileName = System.getProperty("f2.preview.smoke.ini");

        Assume.assumeTrue(
                "Set -Df2.preview.smoke.input=/path/to/prepared.txt",
                inputFileName != null && inputFileName.trim().length() > 0
        );

        Assume.assumeTrue(
                "Set -Df2.preview.smoke.ini=/path/to/ALTPRNT5.INI",
                iniFileName != null && iniFileName.trim().length() > 0
        );

        Path inputPath = Paths.get(inputFileName);
        Path iniPath = Paths.get(iniFileName);

        Assume.assumeTrue("Prepared text file does not exist: " + inputPath, Files.isRegularFile(inputPath));
        Assume.assumeTrue("INI file does not exist: " + iniPath, Files.isRegularFile(iniPath));

        Charset textCharset = charsetProperty(
                "f2.preview.smoke.charset",
                StandardCharsets.UTF_8
        );

        Charset iniCharset = charsetProperty(
                "f2.preview.smoke.ini.charset",
                Charset.forName("windows-1251")
        );

        F2Runtime.init(
                IniFileModel.load(
                        iniPath,
                        iniCharset
                )
        );

        String printerName = System.getProperty("f2.preview.smoke.printer");

        if (printerName != null && printerName.trim().length() > 0) {
            F2Runtime.get()
                    .printerMan()
                    .selectPrinterName(printerName.trim());
        }

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
                System.getProperty(
                        "f2.preview.smoke.dpi",
                        "144"
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
                System.getProperty(
                        "f2.preview.smoke.output",
                        "target/f2-preview-debug-page-1.png"
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

    private static Charset charsetProperty(
            String name,
            Charset defaultValue
    ) {
        String value = System.getProperty(name);

        if (value == null || value.trim().length() == 0)
            return defaultValue;

        return Charset.forName(value.trim());
    }

    private static final class IniFileModel implements F2AltIniModel {

        private final Map<String, String> commands;
        private final Map<String, String> codeText;
        private final Map<String, String> codeGraphics;
        private final Map<String, String> driverRef;

        private IniFileModel(
                Map<String, String> commands,
                Map<String, String> codeText,
                Map<String, String> codeGraphics,
                Map<String, String> driverRef
        ) {
            this.commands = Collections.unmodifiableMap(new LinkedHashMap<String, String>(commands));
            this.codeText = Collections.unmodifiableMap(new LinkedHashMap<String, String>(codeText));
            this.codeGraphics = Collections.unmodifiableMap(new LinkedHashMap<String, String>(codeGraphics));
            this.driverRef = Collections.unmodifiableMap(new LinkedHashMap<String, String>(driverRef));
        }

        private static IniFileModel load(
                Path path,
                Charset charset
        ) throws IOException {

            Map<String, String> commands = new LinkedHashMap<String, String>();
            Map<String, String> codeText = new LinkedHashMap<String, String>();
            Map<String, String> codeGraphics = new LinkedHashMap<String, String>();
            Map<String, String> driverRef = new LinkedHashMap<String, String>();

            Map<String, String> current = null;

            BufferedReader reader = Files.newBufferedReader(path, charset);

            try {
                String line;

                while ((line = reader.readLine()) != null) {
                    line = stripBom(line).trim();

                    if (line.length() == 0)
                        continue;

                    if (line.startsWith(";") || line.startsWith("#"))
                        continue;

                    if (line.startsWith("[") && line.endsWith("]")) {
                        String section = normalizeSectionName(
                                line.substring(1, line.length() - 1)
                        );

                        if ("COMMANDS".equals(section))
                            current = commands;
                        else if ("CODETEXT".equals(section))
                            current = codeText;
                        else if ("CODEGRAPHICS".equals(section))
                            current = codeGraphics;
                        else if ("DRIVERREF".equals(section))
                            current = driverRef;
                        else
                            current = null;

                        continue;
                    }

                    if (current == null)
                        continue;

                    int eq = line.indexOf('=');

                    if (eq <= 0)
                        continue;

                    String key = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();

                    if (key.length() == 0)
                        continue;

                    current.put(key, value);
                }
            }
            finally {
                reader.close();
            }

            return new IniFileModel(
                    commands,
                    codeText,
                    codeGraphics,
                    driverRef
            );
        }

        @Override
        public Map<String, String> commands() {
            return commands;
        }

        @Override
        public Map<String, String> codeText() {
            return codeText;
        }

        @Override
        public Map<String, String> codeGraphics() {
            return codeGraphics;
        }

        @Override
        public Map<String, String> driverRef() {
            return driverRef;
        }

        @Override
        public String cleanCommandName(String name) {

            if (name == null)
                return null;

            String result = name.trim();

            while (result.startsWith("!"))
                result = result.substring(1).trim();

            return result;
        }

        @Override
        public String commandDescription(String name) {
            return valueByCleanName(commands, name);
        }

        @Override
        public String codeText(String name) {
            return valueByCleanName(codeText, name);
        }

        @Override
        public String codeGraphics(String name) {
            return valueByCleanName(codeGraphics, name);
        }

        @Override
        public String driverRef(String name) {
            return valueByCleanName(driverRef, name);
        }

        @Override
        public boolean isMatrixPrinter(String printerName) {
            return DRIVER_REF_CODE_TEXT.equalsIgnoreCase(driverRef(printerName));
        }

        @Override
        public boolean isGraphicsPrinter(String printerName) {
            return DRIVER_REF_CODE_GRAPHICS.equalsIgnoreCase(driverRef(printerName));
        }

        private String valueByCleanName(
                Map<String, String> source,
                String name
        ) {
            if (name == null)
                return null;

            String cleanName = cleanCommandName(name);

            for (Map.Entry<String, String> e : source.entrySet()) {
                if (cleanCommandName(e.getKey()).equalsIgnoreCase(cleanName))
                    return e.getValue();
            }

            return null;
        }

        private static String normalizeSectionName(String value) {
            return value == null
                    ? ""
                    : value.trim().replace("_", "").replace("-", "").toUpperCase(Locale.ENGLISH);
        }

        private static String stripBom(String value) {
            if (value != null && value.length() > 0 && value.charAt(0) == '\uFEFF')
                return value.substring(1);

            return value;
        }
    }
}
