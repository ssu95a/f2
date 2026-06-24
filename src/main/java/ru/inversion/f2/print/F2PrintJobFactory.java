package ru.inversion.f2.print;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.utils.Checks;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

public final class F2PrintJobFactory {

    private final F2PrinterMan printerMan;
    private final F2PrintPageSetupResolver pageSetupResolver;

    public F2PrintJobFactory(
            F2PrinterMan printerMan
    ) {
        this(
                printerMan,
                new F2PrintPageSetupResolver()
        );
    }

    F2PrintJobFactory(
            F2PrinterMan printerMan,
            F2PrintPageSetupResolver pageSetupResolver
    ) {
        this.printerMan =
                Checks.Require.object(
                        printerMan,
                        "printerMan"
                );

        this.pageSetupResolver =
                Checks.Require.object(
                        pageSetupResolver,
                        "pageSetupResolver"
                );
    }

    public F2PrintJob create(
            F2StyledDocument document,
            PrintService printService,
            PrintRequestAttributeSet documentAttributes,
            F2PrintListener listener
    ) throws Exception {

        Checks.Require.object(
                document,
                "document"
        );

        Checks.Require.object(
                printService,
                "printService"
        );

        PrintRequestAttributeSet attributes =
                documentAttributes == null
                        ? new HashPrintRequestAttributeSet()
                        : new HashPrintRequestAttributeSet(
                        documentAttributes
                );

        String printerName =
                printService.getName();

        boolean matrixPrinter =
                printerMan.isMatrixPrinter(
                        printerName
                );

        String driverRef =
                printerMan.driverRef(
                        printerName
                );

        F2PrintPageSetup pageSetup =
                pageSetupResolver.resolve(
                        new F2PrintSettings(
                                printService,
                                attributes
                        ),
                        matrixPrinter
                );

        return new F2PrintJob(
                document,
                pageSetup,
                driverRef,
                () -> F2PrintService.resolveCopies(
                        attributes
                ),
                listener
        );
    }
}