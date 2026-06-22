package ru.inversion.f2.fx;

import javafx.application.Application;
import javafx.collections.FXCollections;
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
import javafx.stage.Stage;
import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.ini.F2AltIniModelLoader;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.print.F2PrintJob;
import ru.inversion.f2.print.F2PrintListener;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.f2.print.F2PrintPageSetupResolver;
import ru.inversion.f2.print.F2PrintService;
import ru.inversion.f2.print.F2PrintSettings;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class F2FxPreviewManualSmokeApp extends Application {

    private static final String DEFAULT_INPUT_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\cus02.DAT";

    private static final String DEFAULT_INI_FILE =
            "d:\\Java\\Projects\\f2\\src\\test\\ALTPRNT5.INI";

    private static final String DEFAULT_PRINTER_NAME =
            "Microsoft Print to PDF";

    private final F2PrintPageSetupResolver pageSetupResolver =
            new F2PrintPageSetupResolver();

//    public static void main(String[] args) {
//        launch(args);
//    }

    @Override
    public void start(Stage stage) throws Exception {

        PreviewState state = preparePreview();
        F2FxPreviewPane previewPane = new F2FxPreviewPane(
            state.document,
            state.pageSetup
        );

        previewPane.setDpi(doubleProperty("f2.preview.smoke.dpi", 144.0d));
        previewPane.setDebugOverlay(true);

        BorderPane root = new BorderPane(previewPane);
        root.setTop(newToolbar(previewPane, state));

        stage.setTitle("F2 JavaFX Preview Smoke");
        stage.setScene(new Scene(root, 1000, 800));
        stage.show();
    }

    private HBox newToolbar(
            F2FxPreviewPane previewPane,
            PreviewState state
    ) {

        ComboBox<PrintService> printerComboBox =
                newPrinterComboBox(state.pageSetup.printService());

        Spinner<Integer> copiesSpinner =
                newCopiesSpinner(state.copyCount);

        Label printerLabel = new Label(
                printerCaption(state.pageSetup.printService())
        );

        printerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
        });

        copiesSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                return;

            state.copyCount = newValue.intValue();
        });

        Button printButton = new Button("Print");

        printButton.setOnAction(event -> {
            F2PrintPageSetup pageSetup =
                    previewPane.pageSetup();

            String driverRef =
                    F2Runtime.get()
                            .printerMan()
                            .driverRef(
                                    pageSetup
                                            .printService()
                                            .getName()
                            );

            F2PrintJob printJob =
                    new F2PrintJob(
                            previewPane.document(),
                            pageSetup,
                            driverRef,
                            () -> state.copyCount,
                            F2PrintListener.NONE
                    );

            F2FxPrintTask printTask =
                    new F2FxPrintTask(printJob);

            Window owner =
                    printButton
                            .getScene()
                            .getWindow();

            F2FxPrintProgressDialog progressDialog =
                    new F2FxPrintProgressDialog(
                            owner,
                            printTask
                    );

            printButton.setDisable(true);
            printerComboBox.setDisable(true);
            copiesSpinner.setDisable(true);

            Runnable enableControls = () -> {
                printButton.setDisable(false);
                printerComboBox.setDisable(false);
                copiesSpinner.setDisable(false);
            };

            printTask.setOnSucceeded(taskEvent -> {
                enableControls.run();
                System.out.println(
                        printTask.getValue()
                );
            });

            printTask.setOnFailed(taskEvent -> {
                enableControls.run();

                Throwable ex =
                        printTask.getException();

                if (ex != null)
                    ex.printStackTrace();
            });

            printTask.setOnCancelled(taskEvent -> {
                enableControls.run();
                System.out.println("F2 print cancelled");
            });

            progressDialog.show();

            Thread thread =
                    new Thread(
                            printTask,
                            "f2-print"
                    );

            thread.setDaemon(true);
            thread.start();
        });


        Button previousButton = new Button("<");
        Button nextButton = new Button(">");
        CheckBox debugOverlayCheckBox = new CheckBox("Debug overlay");
        Label pageLabel = new Label();

        debugOverlayCheckBox.setSelected(true);

        previousButton.setOnAction(event -> {
            previewPane.previousPage();
            updatePageControls(previewPane, previousButton, nextButton, pageLabel);
        });

        nextButton.setOnAction(event -> {
            previewPane.nextPage();
            updatePageControls(previewPane, previousButton, nextButton, pageLabel);
        });

        debugOverlayCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                previewPane.setDebugOverlay(Boolean.TRUE.equals(newValue))
        );

        HBox toolbar = new HBox(
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

        toolbar.setStyle("-fx-padding: 8; -fx-alignment: center-left;");

        updatePageControls(previewPane, previousButton, nextButton, pageLabel);

        return toolbar;
    }

    private ComboBox<PrintService> newPrinterComboBox(PrintService selectedService) {
        ComboBox<PrintService> comboBox = new ComboBox<>(
                FXCollections.observableArrayList(
                        F2Runtime.get()
                                .printerMan()
                                .printServices()
                )
        );

        comboBox.setCellFactory(listView -> new PrintServiceListCell());
        comboBox.setButtonCell(new PrintServiceListCell());
        comboBox.setPrefWidth(260.0d);

        selectPrinter(comboBox, selectedService);

        return comboBox;
    }

    private Spinner<Integer> newCopiesSpinner(int copyCount) {
        Spinner<Integer> spinner = new Spinner<>();

        spinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        99,
                        copyCount
                )
        );

        spinner.setPrefWidth(80.0d);

        return spinner;
    }

    private String driverRef(F2PrintPageSetup pageSetup) {
        if (pageSetup == null)
            return null;

        return F2Runtime.get()
                .printerMan()
                .driverRef(pageSetup.printService().getName());
    }

    private void selectPrinter(
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

    private static String printerCaption(PrintService service) {
        return service == null ? "<no printer>" : service.getName();
    }

    private void updatePageControls(
            F2FxPreviewPane previewPane,
            Button previousButton,
            Button nextButton,
            Label pageLabel
    ) {
        pageLabel.setText(
                "Page " + previewPane.pageNumber()
                        + " / " + previewPane.pageCount()
        );

        previousButton.setDisable(previewPane.pageIndex() <= 0);
        nextButton.setDisable(previewPane.pageIndex() + 1 >= previewPane.pageCount());
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

    F2StyledDocument document =
            new F2PrintService().prepareDocument(text);

    PrintRequestAttributeSet attributes = F2Runtime.get()
            .printerMan()
            .currentPrintSettings()
            .attributesCopy();

    PrintService printService = F2Runtime.get()
            .printerMan()
            .currentPrintService();


    return new PreviewState(
            document,
            setup,
            attributes,
            F2PrintService.resolveCopies(attributes)
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
    private F2PrintPageSetup pageSetup;
    private final PrintRequestAttributeSet attributes;
    private int copyCount;

    private PreviewState(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup,
            PrintRequestAttributeSet attributes,
            int copyCount
    ) {
        this.document = document;
        this.pageSetup = pageSetup;
        this.attributes = attributes;
        this.copyCount = copyCount;
    }
}

private static final class PrintServiceListCell extends ListCell<PrintService> {
    @Override
    protected void updateItem(
            PrintService item,
            boolean empty
    ) {
        super.updateItem(item, empty);

        setText(empty || item == null ? null : item.getName());
    }
}
}