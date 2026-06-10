package ru.inversion.f2.print;

import org.junit.Assume;
import org.junit.Test;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class F2PrintPageSetupResolverTest {

    private static final double PT_PER_MM = 72.0d / 25.4d;

    @Test
    public void smokeResolverAppliesMediaPrintableAreaFromAttributes() throws Exception {

        PrintService printService =
                defaultOrFirstPrintService();

        Assume.assumeNotNull(
                "No print services available for F2PrintPageSetupResolver smoke",
                printService
        );

        MediaPrintableArea mediaPrintableArea =
                new MediaPrintableArea(
                        5.0f,
                        5.0f,
                        200.0f,
                        287.0f,
                        MediaPrintableArea.MM
                );

        PrintRequestAttributeSet attrs =
                new HashPrintRequestAttributeSet();

        attrs.add(mediaPrintableArea);

        F2PrintSettings settings =
                new F2PrintSettings(
                        printService,
                        attrs
                );

        F2PrintPageSetup setup =
                new F2PrintPageSetupResolver().resolve(
                        settings,
                        false
                );

        assertSame(printService, setup.printService());
        assertEquals(mediaPrintableArea, setup.mediaPrintableArea());

        assertEquals(mmToPt(5.0d), setup.imageableXPt(), 0.25d);
        assertEquals(mmToPt(5.0d), setup.imageableYPt(), 0.25d);
        assertEquals(mmToPt(200.0d), setup.imageableWidthPt(), 0.25d);
        assertEquals(mmToPt(287.0d), setup.imageableHeightPt(), 0.25d);
    }

    private static PrintService defaultOrFirstPrintService() {

        PrintService defaultPrintService =
                PrintServiceLookup.lookupDefaultPrintService();

        if (defaultPrintService != null)
            return defaultPrintService;

        PrintService[] printServices =
                PrintServiceLookup.lookupPrintServices(null, null);

        if (printServices == null || printServices.length == 0)
            return null;

        return printServices[0];
    }

    private static double mmToPt(double valueMm) {
        return valueMm * PT_PER_MM;
    }
}
