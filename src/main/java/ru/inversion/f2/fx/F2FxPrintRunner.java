package ru.inversion.f2.fx;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import ru.inversion.f2.print.F2PrintJob;

import java.io.PrintWriter;
import java.io.StringWriter;

/** */
public final class F2FxPrintRunner {

    /** */
    private F2FxPrintRunner()
    { }

    /** */
    public static F2FxPrintTask start( Window owner, F2PrintJob printJob )
    {
        F2FxPrintTask task = new F2FxPrintTask(printJob);

        F2FxPrintProgressDialog dialog = new F2FxPrintProgressDialog( owner, task );

        task.setOnSucceeded(event -> dialog.close());
        task.setOnCancelled(event -> dialog.close());
        task.setOnFailed   (event -> { dialog.close(); showPrintError( owner, task.getException() ); });

        dialog.show();

        Thread thread = new Thread( task, "f2-print" );
        thread.setDaemon(true);
        thread.start();

        return task;
    }

    /** */
    public static void showPrintError( Window owner, Throwable error )
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Ошибка");
        alert.setHeaderText( "Ошибка при печати документа" );
        String message = error == null ? "Неизвестная ошибка печати" : error.getLocalizedMessage();

        if( message == null || message.trim().isEmpty() )
            message = error.getClass().getName();

        alert.setContentText(message);

        if( error != null )
        {
            StringWriter sw = new StringWriter();
            PrintWriter  pw = new PrintWriter(sw);
            error.printStackTrace(pw);
            pw.flush();

            TextArea textArea = new TextArea(sw.toString());

            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setMaxWidth (Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            textArea.setPrefColumnCount(100);
            textArea.setPrefRowCount(20);

            GridPane.setVgrow( textArea, Priority.ALWAYS );
            GridPane.setHgrow( textArea, Priority.ALWAYS );

            GridPane expContent = new GridPane();
            expContent.setMaxWidth( Double.MAX_VALUE );
            expContent.add( textArea, 0, 0 );
            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setExpanded(true);
        }

        if (owner != null)
            alert.initOwner(owner);

        alert.showAndWait();
    }}