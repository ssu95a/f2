package ru.inversion.f2.fx;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

public final class F2FxPrintProgressDialog
        extends Dialog<Void> {

    public F2FxPrintProgressDialog(
            Window owner,
            F2FxPrintTask printTask
    ) {
        if (printTask == null)
            throw new IllegalArgumentException("printTask is null");

        if (owner != null) {
            initOwner(owner);
            initModality(Modality.WINDOW_MODAL);
        }

        setTitle("Печать документа");
        setHeaderText(
                printTask.printJob().printerName()
        );

        ProgressBar progressBar =
                new ProgressBar();

        progressBar.setPrefWidth(480);
        progressBar.progressProperty().bind(
                printTask.progressProperty()
        );

        Label messageLabel =
                new Label();

        messageLabel.setWrapText(true);
        messageLabel.textProperty().bind(
                printTask.messageProperty()
        );

        VBox content =
                new VBox(
                        10,
                        messageLabel,
                        progressBar
                );

        content.setStyle("-fx-padding: 10;");

        getDialogPane().setContent(content);

        ButtonType cancelButtonType =
                new ButtonType(
                        "Отмена",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        getDialogPane()
                .getButtonTypes()
                .add(cancelButtonType);

        Button cancelButton =
                (Button) getDialogPane()
                        .lookupButton(cancelButtonType);

        cancelButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    if (!isTerminal(printTask.getState())) {
                        printTask.cancelPrint();
                        cancelButton.setDisable(true);
                        event.consume();
                    }
                }
        );

        setOnCloseRequest(event -> {
            if (!isTerminal(printTask.getState())) {
                printTask.cancelPrint();
                cancelButton.setDisable(true);
                event.consume();
            }
        });

        printTask.stateProperty().addListener(
                (observable, oldState, newState) -> {
                    if (isTerminal(newState))
                        close();
                }
        );
    }

    private static boolean isTerminal(
            Worker.State state
    ) {
        return state == Worker.State.SUCCEEDED
                || state == Worker.State.FAILED
                || state == Worker.State.CANCELLED;
    }
}