package ru.inversion.f2.print;

import ru.inversion.f2.awt.F2AwtDocumentPaginator;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.utils.Checks;

import java.util.function.IntSupplier;

/**
 * Создаёт F2PrintJob из уже подготовленного логического документа,
 * физической раскладки и разрешённого F2PrintPageSetup.
 *
 * Не отвечает за:
 * - выбор принтера;
 * - разрешение PageFormat;
 * - расчёт printable area;
 * - подготовку документа;
 * - запуск печати.
 */
public final class F2PrintJobFactory {

    private final F2AwtDocumentPaginator paginator =
            new F2AwtDocumentPaginator();

    private final F2PrinterMan printerMan;

    /** */
    public F2PrintJobFactory( F2PrinterMan printerMan )
    {
        this.printerMan = Checks.Require.object( printerMan, "printerMan" );
    }

    /** */
    public F2PrintJob create ( F2StyledDocument document, F2PrintPageSetup pageSetup, IntSupplier copiesSupplier, F2PrintListener listener )
    {
        Checks.Require.object( document, "document" );
        Checks.Require.object( pageSetup, "pageSetup" );

        String printerName = pageSetup.printService().getName();
        String driverRef = printerMan.driverRef(printerName);

        F2PageLayout pageLayout =
                paginator.layout(document, pageSetup);

        return new F2PrintJob (
            document,
            pageSetup,
            driverRef,
            copiesSupplier,
            listener
        ).withPageLayout(pageLayout);
    }

    /** */
    public F2PrintJob create(
        F2StyledDocument document, F2PrintPageSetup pageSetup, int copies, F2PrintListener listener
    ) {
        return create( document, pageSetup, () -> copies, listener );
    }
}
