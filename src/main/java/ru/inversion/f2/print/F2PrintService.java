package ru.inversion.f2.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.awt.F2AwtPageable;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.prepared.F2PreparedDocument;
import ru.inversion.f2.prepared.F2PreparedDocumentParser;
import ru.inversion.f2.prepared.F2PreparedTextInterpreter;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.utils.Checks;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.awt.print.PrinterJob;
import java.lang.invoke.MethodHandles;

public final class F2PrintService {

    private static final Logger log =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final F2PreparedDocumentParser preparedDocumentParser =
            new F2PreparedDocumentParser();

    private final F2PreparedTextInterpreter preparedTextInterpreter =
            new F2PreparedTextInterpreter();

    /** */
    public F2StyledDocument prepareDocument(String text)
    {
        Checks.Require.text(text, "text");

        F2CommandRegistry registry =
                F2Runtime.get().commandRegistry();

        F2PreparedDocument prepared =
                preparedDocumentParser.parse(text, registry);

        log.info("F2 prepared content mode: {}", prepared.contentMode());

        return preparedTextInterpreter.interpret(
                prepared.tokens(),
                registry
        );
    }

    /** */
    public F2PrintResult print(F2PrintJob printJob) throws Exception {

        Checks.Require.object(printJob, "printJob");

        if (printJob.matrixPrinter()) {
            throw new UnsupportedOperationException(
                    "CodeText / ESC print path is not implemented yet"
            );
        }

        /*
         * Все динамические параметры фиксируются до начала lifecycle.
         * copiesSupplier вызывается здесь ровно один раз.
         */
        F2PrintJob resolvedPrintJob =
                printJob.resolveCopies();

        int copies =
                resolvedPrintJob.resolvedCopies();

        F2PrintPageSetup setup =
                resolvedPrintJob.pageSetup();

        PrintRequestAttributeSet attributes =
                setup.attributesCopy();

        attributes.add(
                new Copies(copies)
        );

        PrinterJob job =
                PrinterJob.getPrinterJob();

        resolvedPrintJob.cancellation().bind(awtJob);

        try {
            awtJob.print(attributes);
        }
        finally {
            resolvedPrintJob.cancellation().unbind(awtJob);
        }

        job.setPrintService(
                setup.printService()
        );

        job.setJobName("F2 report");

        job.setPageable(
                new F2AwtPageable(resolvedPrintJob)
        );

        F2PrintListener listener =
                resolvedPrintJob.listener();

        Exception finalError = null;

        try {
            listener.onCopiesResolved(
                    resolvedPrintJob,
                    copies
            );

            listener.onBeginPrint(
                    resolvedPrintJob
            );

            job.print(attributes);

            listener.onEndPrint(
                    resolvedPrintJob
            );

            return new F2PrintResult(
                    resolvedPrintJob.printerName(),
                    resolvedPrintJob.driverRef(),
                    resolvedPrintJob.pageCount()
            );
        }
        catch (Exception ex) {
            finalError = ex;
            throw ex;
        }
        finally {
            listener.onFinalPrint(
                    resolvedPrintJob,
                    finalError
            );
        }
    }

    public static int resolveCopies(
            PrintRequestAttributeSet attributes
    ) {
        if (attributes == null)
            return 1;

        Copies copies =
                (Copies) attributes.get(Copies.class);

        return copies == null ? 1 : copies.getValue();
    }
}