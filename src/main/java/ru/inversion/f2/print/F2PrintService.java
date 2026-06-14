package ru.inversion.f2.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.f2.F2Runtime;
import ru.inversion.f2.awt.F2AwtPageable;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.prepared.*;
import ru.inversion.utils.Checks;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.awt.print.PrinterJob;
import java.lang.invoke.MethodHandles;

public final class F2PrintService {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final F2PreparedDocumentParser preparedDocumentParser = new F2PreparedDocumentParser();

    private final F2PreparedTextInterpreter preparedTextInterpreter = new F2PreparedTextInterpreter();

    /** */
    public F2StyledDocument prepareDocument( String text)
    {
        Checks.Require.text( text, "text" );

        F2CommandRegistry  registry = F2Runtime.get().commandRegistry();

        F2PreparedDocument prepared = preparedDocumentParser.parse(text, registry);

        log.info ( "F2 prepared content mode: {}", prepared.contentMode() );

        return preparedTextInterpreter.interpret( prepared.tokens(), registry );
    }

    /** */
    public F2PrintResult printDocument(
            F2StyledDocument document,
            F2PrintPageSetup setup,
            F2PrintListener listener
    ) throws Exception {

        Checks.Require.object(document, "document");
        Checks.Require.object(setup, "setup");

        F2PrintListener safeListener = listener == null ? F2PrintListener.NONE : listener;

        if (setup.matrixPrinter()) {
            throw new UnsupportedOperationException(
                    "CodeText / ESC print path is not implemented yet"
            );
        }

        PrintRequestAttributeSet attributes = setup.attributesCopy();
        int copies = resolveCopies(attributes);

        String driverRef = F2Runtime.get()
                .printerMan()
                .driverRef(setup.printService().getName());

        F2PrintJobInfo jobInfo = new F2PrintJobInfo(
                setup,
                driverRef,
                document.pageCount(),
                copies
        );

        PrinterJob job = PrinterJob.getPrinterJob();

        job.setPrintService(
            setup.printService()
        );

        job.setJobName("F2 report");

        job.setPageable(
                new F2AwtPageable(
                        document,
                        setup,
                        jobInfo,
                        safeListener
                )
        );

        Exception finalError = null;

        try {
            safeListener.onBeginPrint(jobInfo);

            job.print(attributes);

            safeListener.onEndPrint(jobInfo);
        }
        catch (Exception ex) {
            finalError = ex;
            throw ex;
        }
        finally {
            safeListener.onFinalPrint(
                    jobInfo,
                    finalError
            );
        }

        return new F2PrintResult(
                setup.printService().getName(),
                jobInfo.driverRef(),
                document.pageCount()
        );
    }

    private int resolveCopies(PrintRequestAttributeSet attributes) {
        if (attributes == null)
            return 1;

        Copies copies =
                (Copies) attributes.get(Copies.class);

        return copies == null ? 1 : copies.getValue();
    }
}
