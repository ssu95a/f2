package ru.inversion.f2.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.ini.F2AltIniModelLoader;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.f2.print.F2PrintService;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class F2FxPreviewManualSmokeApp extends Application {

    private static final String DEFAULT_INPUT_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\ae100020_5012.dat";

    private static final String DEFAULT_INI_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\ALTPRNT5.INI";

    private static final String DEFAULT_PRINTER_NAME =
            "Microsoft Print to PDF";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        PreviewState state = preparePreview();

        F2FxPreviewPane previewPane = new F2FxPreviewPane(
                state.document,
                state.pageSetup
        );

        previewPane.setDpi(doubleProperty("f2.preview.smoke.dpi", 144.0d));
        previewPane.setDebugOverlay(true);

        stage.setTitle("F2 JavaFX Preview Smoke");
        stage.setScene(new Scene(previewPane, 1000, 800));
        stage.show();
    }

    private PreviewState preparePreview() throws Exception {
        Path inputPath = Paths.get(stringProperty(
                "f2.preview.smoke.input",
                DEFAULT_INPUT_FILE
        ));

        Path iniPath = Paths.get(stringProperty(
                "f2.preview.smoke.ini",
                DEFAULT_INI_FILE
        ));

        Charset textCharset = Charset.forName(stringProperty(
                "f2.preview.smoke.charset",
                "windows-1251"
        ));

        Charset iniCharset = Charset.forName(stringProperty(
                "f2.preview.smoke.ini.charset",
                "windows-1251"
        ));

        F2Runtime.init(new F2AltIniModelLoader().load(iniPath, iniCharset));

        String printerName = stringProperty(
                "f2.preview.smoke.printer",
                DEFAULT_PRINTER_NAME
        );

        if (printerName != null && printerName.trim().length() > 0) {
            F2Runtime.get()
                    .printerMan()
                    .selectPrinterName(printerName.trim());
        }

        applyPrintableAreaOverride();

        String text = new String(
                Files.readAllBytes(inputPath),
                textCharset
        );

        F2StyledDocument document = new F2PrintService().prepareDocument(text);

        F2PrintPageSetup setup = F2Runtime.get()
                .printerMan()
                .currentPrintPageSetup();

        return new PreviewState(document, setup);
    }

    private static void applyPrintableAreaOverride() {
        HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(new MediaPrintableArea(5.0f, 5.0f, 200.0f, 287.0f, MediaPrintableArea.MM));

        F2Runtime.get()
                .printerMan()
                .selectPrintAttributes(attributes);
    }

    private static String stringProperty(String name, String defaultValue) {
        String value = System.getProperty(name);

        if (value == null || value.trim().length() == 0)
            return defaultValue;

        return value;
    }

    private static double doubleProperty(String name, double defaultValue) {
        String value = System.getProperty(name);

        if (value == null || value.trim().length() == 0)
            return defaultValue;

        return Double.parseDouble(value.trim());
    }

    private static final class PreviewState {
        private final F2StyledDocument document;
        private final F2PrintPageSetup pageSetup;

        private PreviewState(F2StyledDocument document, F2PrintPageSetup pageSetup) {
            this.document = document;
            this.pageSetup = pageSetup;
        }
    }
}
