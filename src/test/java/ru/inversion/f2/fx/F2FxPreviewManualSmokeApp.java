package ru.inversion.f2.fx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.ini.F2AltIniModelLoader;
import ru.inversion.f2.prepared.F2PreparedContentMode;
import ru.inversion.f2.prepared.F2PreparedDocument;
import ru.inversion.f2.prepared.F2PreparedDocumentParser;
import ru.inversion.f2.prepared.F2PreparedTextInterpreter;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.prepared.F2StyledTextChunk;
import ru.inversion.f2.print.F2PrintJob;
import ru.inversion.f2.print.F2PrintJobFactory;
import ru.inversion.f2.print.F2PrintListener;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.f2.print.F2PrintPageSetupResolver;
import ru.inversion.f2.print.F2PrintService;
import ru.inversion.f2.print.F2PrintSettings;
import ru.inversion.f2.style.F2Style;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.inversion.f2.fx.F2FxPrintRunner.showPrintError;

/**
 * Ручной smoke для полного F2 pipeline.
 *
 * Поддерживает:
 * - смену принтера без изменения document attributes;
 * - смену кодировки с полным повторным чтением/parse/interpret;
 * - перекрытие font family и font size для PLAIN/PLAIN_WITH_HEADER;
 * - preview и print через один F2PrintPageSetup;
 * - печать через F2PrintJobFactory + F2FxPrintRunner.
 */
public class F2FxPreviewManualSmokeApp extends Application {

    private static final String DEFAULT_INPUT_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\cus02.DAT";

    private static final String DEFAULT_INI_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\ALTPRNT5.INI";

    private static final String DEFAULT_PRINTER_NAME =
            "Microsoft Print to PDF";

    private static final String DEFAULT_TEXT_CHARSET =
            "windows-1251";

    private static final String DEFAULT_INI_CHARSET =
            "windows-1251";

    private final F2PrintPageSetupResolver pageSetupResolver =
            new F2PrintPageSetupResolver();

    private final F2PreparedDocumentParser documentParser =
            new F2PreparedDocumentParser();

    private final F2PreparedTextInterpreter documentInterpreter =
            new F2PreparedTextInterpreter();

    @Override
    public void start(Stage stage) throws Exception {

        PreviewState state = preparePreview();

        F2FxPreviewPane previewPane =
                new F2FxPreviewPane(
                        state.document,
                        state.pageSetup
                );

        previewPane.setDpi(
                doubleProperty(
                        "f2.preview.smoke.dpi",
                        144.0d
                )
        );

        previewPane.setDebugOverlay(true);

        BorderPane root = new BorderPane(previewPane);
        root.setTop(newControls(previewPane, state));

        stage.setTitle("F2 JavaFX Preview Smoke");
        stage.setScene(new Scene(root, 1360, 850));
        stage.show();
    }

    private VBox newControls(
            F2FxPreviewPane previewPane,
            PreviewState state
    ) {
        F2PrintJobFactory printJobFactory =
                new F2PrintJobFactory(
                        F2Runtime.get().printerMan()
                );

        ComboBox<PrintService> printerComboBox =
                newPrinterComboBox(state.pageSetup.printService());

        Spinner<Integer> copiesSpinner =
                newCopiesSpinner(state.copyCount);

        ComboBox<Charset> encodingComboBox =
                newEncodingComboBox(state.charset);

        ComboBox<String> fontFamilyComboBox =
                newFontFamilyComboBox(state.plainFontFamily);

        Spinner<Integer> fontSizeSpinner =
                newFontSizeSpinner(state.plainFontSize);

        Button previousButton = new Button("<");
        Button nextButton = new Button(">");
        Label pageLabel = new Label();

        Label printerLabel = new Label(
                printerCaption(state.pageSetup.printService())
        );

        Label modeLabel = new Label();
        Label sourceLabel = new Label(state.inputPath.toString());

        CheckBox debugOverlayCheckBox =
                new CheckBox("Debug overlay");

        Button printButton = new Button("Print");

        HBox documentControls = new HBox(
                8.0d,
                new Label("Encoding:"),
                encodingComboBox,
                new Label("Mode:"),
                modeLabel,
                new Label("Font:"),
                fontFamilyComboBox,
                new Label("Size:"),
                fontSizeSpinner,
                new Label("File:"),
                sourceLabel
        );

        HBox printControls = new HBox(
                8.0d,
                new Label("Printer:"),
                printerComboBox,
                printerLabel,
                new Label("Copies:"),
                copiesSpinner,
                previousButton,
                pageLabel,
                nextButton,
                debugOverlayCheckBox,
                printButton
        );

        VBox controls = new VBox(
                6.0d,
                documentControls,
                printControls
        );

        controls.setStyle("-fx-padding: 8;");
        documentControls.setStyle("-fx-alignment: center-left;");
        printControls.setStyle("-fx-alignment: center-left;");

        AtomicBoolean printerChanging =
                new AtomicBoolean(false);

        AtomicBoolean documentControlsChanging =
                new AtomicBoolean(false);

        printerComboBox
                .valueProperty()
                .addListener(
                        (observable, oldPrinter, newPrinter) -> {
                            if (printerChanging.get())
                                return;

                            if (newPrinter == null)
                                return;

                            if (samePrinter(oldPrinter, newPrinter))
                                return;

                            try {
                                F2PrintPageSetup newSetup =
                                        resolvePageSetup(
                                                newPrinter,
                                                state.attributes
                                        );

                                state.pageSetup = newSetup;
                                previewPane.setPageSetup(newSetup);
                                printerLabel.setText(
                                        printerCaption(newPrinter)
                                );
                            }
                            catch (Throwable error) {
                                printerChanging.set(true);

                                try {
                                    printerComboBox.setValue(oldPrinter);
                                }
                                finally {
                                    printerChanging.set(false);
                                }

                                showPrintError(
                                        ownerOf(printerComboBox),
                                        error
                                );
                            }
                        }
                );

        copiesSpinner
                .valueProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (newValue != null)
                                state.copyCount = newValue.intValue();
                        }
                );

        encodingComboBox
                .valueProperty()
                .addListener(
                        (observable, oldCharset, newCharset) -> {
                            if (documentControlsChanging.get())
                                return;

                            if (newCharset == null
                                    || newCharset.equals(oldCharset)) {
                                return;
                            }

                            try {
                                LoadedSource loaded =
                                        loadSource(
                                                state.inputPath,
                                                newCharset
                                        );

                                /*
                                 * Сначала проверяем текущий принтер.
                                 * State меняем только после успешного resolve,
                                 * чтобы откат ComboBox был настоящим откатом.
                                 */
                                F2PrintPageSetup newSetup =
                                        resolvePageSetup(
                                                printerComboBox.getValue(),
                                                state.attributes
                                        );

                                applyLoadedSource(state, loaded);
                                state.charset = newCharset;
                                state.pageSetup = newSetup;

                                refreshDocumentControls(
                                        state,
                                        documentControlsChanging,
                                        fontFamilyComboBox,
                                        fontSizeSpinner,
                                        modeLabel
                                );

                                refreshPreview(
                                        previewPane,
                                        state.document,
                                        state.pageSetup,
                                        previousButton,
                                        nextButton,
                                        pageLabel,
                                        0
                                );
                            }
                            catch (Throwable error) {
                                documentControlsChanging.set(true);

                                try {
                                    encodingComboBox.setValue(oldCharset);
                                }
                                finally {
                                    documentControlsChanging.set(false);
                                }

                                showPrintError(
                                        ownerOf(encodingComboBox),
                                        error
                                );
                            }
                        }
                );

        fontFamilyComboBox
                .valueProperty()
                .addListener(
                        (observable, oldFont, newFont) -> {
                            if (documentControlsChanging.get())
                                return;

                            if (!state.plainFontEditable())
                                return;

                            if (newFont == null
                                    || newFont.trim().isEmpty()
                                    || newFont.equals(oldFont)) {
                                return;
                            }

                            try {
                                state.plainFontFamily = newFont;
                                state.plainFontOverrideActive = true;

                                rebuildPlainDocument(state);

                                refreshPreview(
                                        previewPane,
                                        state.document,
                                        state.pageSetup,
                                        previousButton,
                                        nextButton,
                                        pageLabel,
                                        previewPane.pageIndex()
                                );
                            }
                            catch (Throwable error) {
                                showPrintError(
                                        ownerOf(fontFamilyComboBox),
                                        error
                                );
                            }
                        }
                );

        fontSizeSpinner
                .valueProperty()
                .addListener(
                        (observable, oldSize, newSize) -> {
                            if (documentControlsChanging.get())
                                return;

                            if (!state.plainFontEditable())
                                return;

                            if (newSize == null
                                    || newSize.equals(oldSize)) {
                                return;
                            }

                            try {
                                state.plainFontSize = newSize.intValue();
                                state.plainFontOverrideActive = true;

                                rebuildPlainDocument(state);

                                refreshPreview(
                                        previewPane,
                                        state.document,
                                        state.pageSetup,
                                        previousButton,
                                        nextButton,
                                        pageLabel,
                                        previewPane.pageIndex()
                                );
                            }
                            catch (Throwable error) {
                                showPrintError(
                                        ownerOf(fontSizeSpinner),
                                        error
                                );
                            }
                        }
                );

        previousButton.setOnAction(
                event -> {
                    previewPane.previousPage();
                    updatePageControls(
                            previewPane,
                            previousButton,
                            nextButton,
                            pageLabel
                    );
                }
        );

        nextButton.setOnAction(
                event -> {
                    previewPane.nextPage();
                    updatePageControls(
                            previewPane,
                            previousButton,
                            nextButton,
                            pageLabel
                    );
                }
        );

        debugOverlayCheckBox.setSelected(true);

        debugOverlayCheckBox
                .selectedProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                previewPane.setDebugOverlay(
                                        Boolean.TRUE.equals(newValue)
                                )
                );

        printButton.setOnAction(
                event -> {
                    try {
                        F2PrintJob printJob =
                                printJobFactory.create(
                                        state.document,
                                        state.pageSetup,
                                        () -> state.copyCount,
                                        F2PrintListener.NONE
                                );

                        controls.setDisable(true);

                        F2FxPrintTask printTask =
                                F2FxPrintRunner.start(
                                        ownerOf(printButton),
                                        printJob
                                );

                        printTask.addEventHandler(
                                WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                                taskEvent -> controls.setDisable(false)
                        );

                        printTask.addEventHandler(
                                WorkerStateEvent.WORKER_STATE_CANCELLED,
                                taskEvent -> controls.setDisable(false)
                        );

                        printTask.addEventHandler(
                                WorkerStateEvent.WORKER_STATE_FAILED,
                                taskEvent -> controls.setDisable(false)
                        );

                        /*
                         * Runner запускает thread до возврата task.
                         * На случай мгновенной ошибки не оставляем toolbar disabled.
                         */
                        if (printTask.isDone())
                            controls.setDisable(false);
                    }
                    catch (Throwable error) {
                        controls.setDisable(false);

                        showPrintError(
                                ownerOf(printButton),
                                error
                        );
                    }
                }
        );

        refreshDocumentControls(
                state,
                documentControlsChanging,
                fontFamilyComboBox,
                fontSizeSpinner,
                modeLabel
        );

        updatePageControls(
                previewPane,
                previousButton,
                nextButton,
                pageLabel
        );

        return controls;
    }

    private PreviewState preparePreview() throws Exception {
        Path inputPath =
                Paths.get(
                                stringProperty(
                                        "f2.preview.smoke.input",
                                        DEFAULT_INPUT_FILE
                                )
                        )
                        .toAbsolutePath()
                        .normalize();

        Path iniPath =
                Paths.get(
                                stringProperty(
                                        "f2.preview.smoke.ini",
                                        DEFAULT_INI_FILE
                                )
                        )
                        .toAbsolutePath()
                        .normalize();

        Charset textCharset =
                Charset.forName(
                        stringProperty(
                                "f2.preview.smoke.charset",
                                DEFAULT_TEXT_CHARSET
                        )
                );

        Charset iniCharset =
                Charset.forName(
                        stringProperty(
                                "f2.preview.smoke.ini.charset",
                                DEFAULT_INI_CHARSET
                        )
                );

        F2Runtime.init(
                new F2AltIniModelLoader()
                        .load(
                                iniPath,
                                iniCharset
                        )
        );

        String printerName =
                stringProperty(
                        "f2.preview.smoke.printer",
                        DEFAULT_PRINTER_NAME
                );

        if (printerName != null
                && !printerName.trim().isEmpty()) {
            F2Runtime.get()
                    .printerMan()
                    .selectPrinterName(printerName.trim());
        }

        applyPrintableAreaOverride();

        LoadedSource loaded =
                loadSource(
                        inputPath,
                        textCharset
                );

        PrintRequestAttributeSet attributes =
                F2Runtime.get()
                        .printerMan()
                        .currentPrintSettings()
                        .attributesCopy();

        PrintService printService =
                F2Runtime.get()
                        .printerMan()
                        .currentPrintService();

        if (printService == null)
            throw new IllegalStateException("Принтер не выбран");

        F2PrintPageSetup setup =
                resolvePageSetup(
                        printService,
                        attributes
                );

        FontDefaults fontDefaults =
                detectFontDefaults(
                        loaded.styledDocument
                );

        return new PreviewState(
                inputPath,
                textCharset,
                loaded.preparedDocument,
                loaded.contentMode,
                loaded.styledDocument,
                loaded.styledDocument,
                setup,
                attributes,
                F2PrintService.resolveCopies(attributes),
                fontDefaults.family,
                fontDefaults.size
        );
    }

    /**
     * Smoke-local вариант F2DocumentLoader.
     * При смене Charset файл перечитывается полностью.
     */
    private LoadedSource loadSource(
            Path source,
            Charset charset
    ) throws IOException {
        if (source == null)
            throw new IllegalArgumentException("source is null");

        if (charset == null)
            throw new IllegalArgumentException("charset is null");

        if (!Files.isRegularFile(source)) {
            throw new IOException(
                    "Файл отчёта не найден: " + source
            );
        }

        String text =
                decode(
                        Files.readAllBytes(source),
                        charset
                );

        if (!text.isEmpty()
                && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }

        F2CommandRegistry registry =
                F2Runtime.get().commandRegistry();

        F2PreparedDocument preparedDocument =
                documentParser.parse(
                        text,
                        registry
                );

        F2StyledDocument styledDocument =
                documentInterpreter.interpret(
                        preparedDocument.tokens(),
                        registry
                );

        return new LoadedSource(
                preparedDocument,
                preparedDocument.contentMode(),
                styledDocument
        );
    }

    private void applyLoadedSource(
            PreviewState state,
            LoadedSource loaded
    ) {
        if (loaded == null)
            throw new IllegalArgumentException("loaded is null");

        state.preparedDocument = loaded.preparedDocument;
        state.contentMode = loaded.contentMode;
        state.baseDocument = loaded.styledDocument;

        if (state.plainFontEditable()
                && state.plainFontOverrideActive) {
            state.document =
                    applyPlainFontOverride(
                            state.baseDocument,
                            state.plainFontFamily,
                            state.plainFontSize
                    );
        }
        else {
            state.document = state.baseDocument;

            if (state.plainFontEditable()) {
                FontDefaults defaults =
                        detectFontDefaults(
                                state.baseDocument
                        );

                state.plainFontFamily = defaults.family;
                state.plainFontSize = defaults.size;
            }
        }
    }

    private void rebuildPlainDocument(
            PreviewState state
    ) {
        if (!state.plainFontEditable()) {
            state.document = state.baseDocument;
            return;
        }

        state.document =
                applyPlainFontOverride(
                        state.baseDocument,
                        state.plainFontFamily,
                        state.plainFontSize
                );
    }

    /**
     * Для PLAIN и PLAIN_WITH_HEADER GUI font family/size имеют приоритет.
     * Orientation, page boundaries, indents и decorations сохраняются.
     */
    private static F2StyledDocument applyPlainFontOverride(
            F2StyledDocument source,
            String fontFamily,
            int fontSize
    ) {
        if (source == null)
            throw new IllegalArgumentException("source is null");

        if (fontFamily == null
                || fontFamily.trim().isEmpty()) {
            throw new IllegalArgumentException("fontFamily is empty");
        }

        if (fontSize <= 0)
            throw new IllegalArgumentException("fontSize must be positive");

        List<F2StyledPage> pages =
                new ArrayList<F2StyledPage>();

        double lineStepPt =
                Math.max(
                        fontSize + 2.0d,
                        fontSize * 1.2d
                );

        for (F2StyledPage page : source.pages()) {
            List<F2StyledLine> lines =
                    new ArrayList<F2StyledLine>();

            for (F2StyledLine line : page.lines()) {
                List<F2StyledTextChunk> chunks =
                        new ArrayList<F2StyledTextChunk>();

                for (F2StyledTextChunk chunk : line.chunks()) {
                    F2Style oldStyle =
                            chunk.style() == null
                                    ? F2Style.defaultStyle()
                                    : chunk.style();

                    F2Style newStyle =
                            new F2Style(
                                    fontFamily,
                                    fontSize,
                                    oldStyle.bold(),
                                    oldStyle.italic(),
                                    oldStyle.underline()
                            );

                    chunks.add(
                            new F2StyledTextChunk(
                                    chunk.text(),
                                    newStyle
                            )
                    );
                }

                lines.add(
                        new F2StyledLine(
                                chunks,
                                lineStepPt,
                                line.leftIndentPt()
                        )
                );
            }

            pages.add(
                    new F2StyledPage(
                            lines,
                            page.orientation()
                    )
            );
        }

        return new F2StyledDocument(pages);
    }

    private static FontDefaults detectFontDefaults(
            F2StyledDocument document
    ) {
        if (document != null) {
            for (F2StyledPage page : document.pages()) {
                for (F2StyledLine line : page.lines()) {
                    for (F2StyledTextChunk chunk : line.chunks()) {
                        F2Style style = chunk.style();

                        if (style != null) {
                            String family = style.fontName();
                            int size = style.fontSize();

                            if (family != null
                                    && !family.trim().isEmpty()
                                    && size > 0) {
                                return new FontDefaults(
                                        family,
                                        size
                                );
                            }
                        }
                    }
                }
            }
        }

        F2Style defaultStyle = F2Style.defaultStyle();

        return new FontDefaults(
                defaultStyle.fontName(),
                defaultStyle.fontSize()
        );
    }

    private void refreshDocumentControls(
            PreviewState state,
            AtomicBoolean changing,
            ComboBox<String> fontFamilyComboBox,
            Spinner<Integer> fontSizeSpinner,
            Label modeLabel
    ) {
        changing.set(true);

        try {
            boolean enabled = state.plainFontEditable();

            fontFamilyComboBox.setDisable(!enabled);
            fontSizeSpinner.setDisable(!enabled);
            modeLabel.setText(state.contentMode.name());

            if (enabled) {
                ensureFontPresent(
                        fontFamilyComboBox,
                        state.plainFontFamily
                );

                fontFamilyComboBox.setValue(
                        state.plainFontFamily
                );

                fontSizeSpinner
                        .getValueFactory()
                        .setValue(
                                Integer.valueOf(
                                        state.plainFontSize
                                )
                        );
            }
        }
        finally {
            changing.set(false);
        }
    }

    private static void refreshPreview(
            F2FxPreviewPane previewPane,
            F2StyledDocument document,
            F2PrintPageSetup pageSetup,
            Button previousButton,
            Button nextButton,
            Label pageLabel,
            int preferredPageIndex
    ) {
        previewPane.setPreview(document, pageSetup);

        int pageCount = previewPane.pageCount();

        if (pageCount > 0) {
            int pageIndex =
                    Math.max(
                            0,
                            Math.min(
                                    preferredPageIndex,
                                    pageCount - 1
                            )
                    );

            if (pageIndex > 0)
                previewPane.setPageIndex(pageIndex);
        }

        updatePageControls(
                previewPane,
                previousButton,
                nextButton,
                pageLabel
        );
    }

    private F2PrintPageSetup resolvePageSetup(
            PrintService printService,
            PrintRequestAttributeSet documentAttributes
    ) throws Exception {
        if (printService == null)
            throw new IllegalArgumentException("printService is null");

        boolean matrixPrinter =
                F2Runtime.get()
                        .printerMan()
                        .isMatrixPrinter(
                                printService.getName()
                        );

        return pageSetupResolver.resolve(
                new F2PrintSettings(
                        printService,
                        documentAttributes
                ),
                matrixPrinter
        );
    }

    private ComboBox<PrintService> newPrinterComboBox(
            PrintService selectedService
    ) {
        ComboBox<PrintService> comboBox =
                new ComboBox<PrintService>(
                        FXCollections.observableArrayList(
                                F2Runtime.get()
                                        .printerMan()
                                        .printServices()
                        )
                );

        comboBox.setCellFactory(
                listView -> new PrintServiceListCell()
        );

        comboBox.setButtonCell(
                new PrintServiceListCell()
        );

        comboBox.setPrefWidth(260.0d);
        selectPrinter(comboBox, selectedService);

        return comboBox;
    }

    private static ComboBox<Charset> newEncodingComboBox(
            Charset selected
    ) {
        List<Charset> charsets =
                new ArrayList<Charset>();

        addCharset(charsets, "UTF-8");
        addCharset(charsets, "windows-1251");
        addCharset(charsets, "IBM866");
        addCharset(charsets, "KOI8-R");
        addCharset(charsets, "ISO-8859-5");
        addCharset(charsets, "UTF-16LE");
        addCharset(charsets, "UTF-16BE");

        if (selected != null
                && !charsets.contains(selected)) {
            charsets.add(selected);
        }

        ComboBox<Charset> comboBox =
                new ComboBox<Charset>(
                        FXCollections.observableArrayList(charsets)
                );

        comboBox.setPrefWidth(150.0d);
        comboBox.setValue(selected);

        return comboBox;
    }

    private static ComboBox<String> newFontFamilyComboBox(
            String selected
    ) {
        List<String> families =
                new ArrayList<String>(
                        Font.getFamilies()
                );

        Collections.sort(families);

        ComboBox<String> comboBox =
                new ComboBox<String>(
                        FXCollections.observableArrayList(families)
                );

        comboBox.setPrefWidth(190.0d);
        ensureFontPresent(comboBox, selected);
        comboBox.setValue(selected);

        return comboBox;
    }

    private static Spinner<Integer> newCopiesSpinner(
            int copyCount
    ) {
        Spinner<Integer> spinner = new Spinner<Integer>();

        spinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        99,
                        Math.max(1, copyCount)
                )
        );

        spinner.setPrefWidth(80.0d);
        return spinner;
    }

    private static Spinner<Integer> newFontSizeSpinner(
            int fontSize
    ) {
        Spinner<Integer> spinner = new Spinner<Integer>();

        spinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        6,
                        72,
                        Math.max(
                                6,
                                Math.min(72, fontSize)
                        )
                )
        );

        spinner.setPrefWidth(76.0d);
        return spinner;
    }

    private static void ensureFontPresent(
            ComboBox<String> comboBox,
            String family
    ) {
        if (family == null
                || family.trim().isEmpty()) {
            return;
        }

        if (!comboBox.getItems().contains(family))
            comboBox.getItems().add(family);
    }

    private static void addCharset(
            List<Charset> charsets,
            String charsetName
    ) {
        if (!Charset.isSupported(charsetName))
            return;

        Charset charset = Charset.forName(charsetName);

        if (!charsets.contains(charset))
            charsets.add(charset);
    }

    private static String decode(
            byte[] bytes,
            Charset charset
    ) throws CharacterCodingException {
        CharsetDecoder decoder =
                charset
                        .newDecoder()
                        .onMalformedInput(
                                CodingErrorAction.REPORT
                        )
                        .onUnmappableCharacter(
                                CodingErrorAction.REPORT
                        );

        CharBuffer chars =
                decoder.decode(
                        ByteBuffer.wrap(bytes)
                );

        return chars.toString();
    }

    private static Window ownerOf(
            javafx.scene.Node node
    ) {
        if (node == null
                || node.getScene() == null) {
            return null;
        }

        return node.getScene().getWindow();
    }

    private static void selectPrinter(
            ComboBox<PrintService> comboBox,
            PrintService selectedService
    ) {
        if (selectedService == null)
            return;

        for (PrintService service : comboBox.getItems()) {
            if (samePrinter(service, selectedService)) {
                comboBox.setValue(service);
                return;
            }
        }

        comboBox.setValue(selectedService);
    }

    private static boolean samePrinter(
            PrintService left,
            PrintService right
    ) {
        if (left == null || right == null)
            return false;

        return left.getName().equals(right.getName());
    }

    private static String printerCaption(
            PrintService service
    ) {
        return service == null
                ? "<no printer>"
                : service.getName();
    }

    private static void updatePageControls(
            F2FxPreviewPane previewPane,
            Button previousButton,
            Button nextButton,
            Label pageLabel
    ) {
        int pageCount = previewPane.pageCount();

        pageLabel.setText(
                pageCount <= 0
                        ? "Page 0 / 0"
                        : "Page "
                        + previewPane.pageNumber()
                        + " / "
                        + pageCount
        );

        previousButton.setDisable(
                pageCount <= 0
                        || previewPane.pageIndex() <= 0
        );

        nextButton.setDisable(
                pageCount <= 0
                        || previewPane.pageIndex() + 1 >= pageCount
        );
    }

    private static void applyPrintableAreaOverride() {
        HashPrintRequestAttributeSet attributes =
                new HashPrintRequestAttributeSet();

        attributes.add(
                new MediaPrintableArea(
                        5.0f,
                        5.0f,
                        200.0f,
                        287.0f,
                        MediaPrintableArea.MM
                )
        );

        F2Runtime.get()
                .printerMan()
                .selectPrintAttributes(attributes);
    }

    private static String stringProperty(
            String name,
            String defaultValue
    ) {
        String value = System.getProperty(name);

        if (value == null || value.trim().isEmpty())
            return defaultValue;

        return value;
    }

    private static double doubleProperty(
            String name,
            double defaultValue
    ) {
        String value = System.getProperty(name);

        if (value == null || value.trim().isEmpty())
            return defaultValue;

        return Double.parseDouble(value.trim());
    }

    private static final class LoadedSource {

        private final F2PreparedDocument preparedDocument;
        private final F2PreparedContentMode contentMode;
        private final F2StyledDocument styledDocument;

        private LoadedSource(
                F2PreparedDocument preparedDocument,
                F2PreparedContentMode contentMode,
                F2StyledDocument styledDocument
        ) {
            this.preparedDocument = preparedDocument;
            this.contentMode = contentMode;
            this.styledDocument = styledDocument;
        }
    }

    private static final class FontDefaults {

        private final String family;
        private final int size;

        private FontDefaults(
                String family,
                int size
        ) {
            this.family = family;
            this.size = size;
        }
    }

    private static final class PreviewState {

        private final Path inputPath;
        private Charset charset;

        private F2PreparedDocument preparedDocument;
        private F2PreparedContentMode contentMode;

        private F2StyledDocument baseDocument;
        private F2StyledDocument document;

        private F2PrintPageSetup pageSetup;
        private final PrintRequestAttributeSet attributes;

        private int copyCount;

        private String plainFontFamily;
        private int plainFontSize;
        private boolean plainFontOverrideActive;

        private PreviewState(
                Path inputPath,
                Charset charset,
                F2PreparedDocument preparedDocument,
                F2PreparedContentMode contentMode,
                F2StyledDocument baseDocument,
                F2StyledDocument document,
                F2PrintPageSetup pageSetup,
                PrintRequestAttributeSet attributes,
                int copyCount,
                String plainFontFamily,
                int plainFontSize
        ) {
            this.inputPath = inputPath;
            this.charset = charset;
            this.preparedDocument = preparedDocument;
            this.contentMode = contentMode;
            this.baseDocument = baseDocument;
            this.document = document;
            this.pageSetup = pageSetup;
            this.attributes =
                    new HashPrintRequestAttributeSet(attributes);
            this.copyCount = copyCount;
            this.plainFontFamily = plainFontFamily;
            this.plainFontSize = plainFontSize;
        }

        private boolean plainFontEditable() {
            return contentMode == F2PreparedContentMode.PLAIN
                    || contentMode == F2PreparedContentMode.PLAIN_WITH_HEADER;
        }
    }

    private static final class PrintServiceListCell
            extends ListCell<PrintService> {

        @Override
        protected void updateItem(
                PrintService item,
                boolean empty
        ) {
            super.updateItem(item, empty);
            setText(
                    empty || item == null
                            ? null
                            : item.getName()
            );
        }
    }
}
