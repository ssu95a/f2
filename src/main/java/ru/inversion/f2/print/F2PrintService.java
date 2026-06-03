package ru.inversion.f2.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.awt.F2AwtPageable;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.prepared.*;
import ru.inversion.utils.Checks;

import javax.print.PrintService;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.lang.invoke.MethodHandles;

public final class F2PrintService {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final F2PreparedDocumentParser preparedDocumentParser = new F2PreparedDocumentParser();

    private final F2PreparedTextInterpreter preparedTextInterpreter = new F2PreparedTextInterpreter();

    public F2PrintResult printPreparedText( String text ) throws Exception {

        Checks.Require.text(text, "text");

        F2StyledDocument document = prepareDocument(text);

        return printDocument(document);
    }

    /** */
    public F2StyledDocument prepareDocument( String text)
    {
        Checks.Require.text( text, "text" );

        F2CommandRegistry  registry = F2Runtime.get().commandRegistry();

        F2PreparedDocument prepared = preparedDocumentParser.parse(text, registry);

        log.info ( "F2 prepared content mode: {}", prepared.contentMode() );

        return preparedTextInterpreter.interpret( prepared.tokens(), registry );
    }

    public F2PrintResult printDocument(F2StyledDocument document) throws Exception {

        Checks.Require.object(document, "document");

        F2PrinterMan printerMan = F2Runtime.get().printerMan();

        if( printerMan.isCurrentMatrixPrinter()) {
            throw new UnsupportedOperationException(
                    "CodeText / ESC print path is not implemented yet"
            );
        }

        return printGraphics(document, printerMan);
    }

    /* */
    private F2PrintResult printGraphics( F2StyledDocument document, F2PrinterMan printerMan ) throws Exception {

        PrintService service =
                printerMan.currentPrintService();

        PageFormat pageFormat =
                printerMan.currentPageFormat();

        PrinterJob job =
                PrinterJob.getPrinterJob();

        if (service != null)
            job.setPrintService(service);

        job.setJobName("F2 report");

        job.setPageable(
                new F2AwtPageable(document, pageFormat)
        );

        job.print();

        return new F2PrintResult(
                printerMan.currentPrinterName(),
                printerMan.currentDriverRef(),
                document.pageCount()
        );
    }
}