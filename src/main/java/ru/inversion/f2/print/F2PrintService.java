package ru.inversion.f2.print;

import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.awt.F2AwtPageable;
import ru.inversion.f2.prepared.F2PreparedTextInterpreter;
import ru.inversion.f2.prepared.F2PreparedTextParser;
import ru.inversion.f2.prepared.F2PreparedToken;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.utils.Checks;

import javax.print.PrintService;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.List;

public final class F2PrintService {

    public F2PrintResult printPreparedText( String text ) throws Exception {

        Checks.Require.text(text, "text");

        F2StyledDocument document = prepareDocument(text);

        return printDocument(document);
    }

    public F2StyledDocument prepareDocument(String text) {

        Checks.Require.text( text, "text" );

        List<F2PreparedToken> tokens = new F2PreparedTextParser().parse(text);

        return new F2PreparedTextInterpreter().interpret(
                tokens,
                F2Runtime.get().commandRegistry()
        );
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