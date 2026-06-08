package ru.inversion.f2.print;

import org.junit.Test;
import ru.inversion.f2.awt.F2AwtPageRenderConfig;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Sides;
import java.awt.print.PageFormat;
import java.awt.print.Paper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class F2PrintPageSetupTest {

    @Test
    public void smokePrintPageSetupKeepsDefensiveAttributeCopy() {

        PrintService printService = mock(PrintService.class);
        when(printService.getName()).thenReturn("Smoke Printer");

        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(Sides.ONE_SIDED);
        attrs.add(new Copies(2));

        F2PrintPageSetup setup = F2PrintPageSetup.builder()
                .printService(printService)
                .attributes(attrs)
                .pageFormat(newPageFormat())
                .matrixPrinter(false)
                .safeFallbackApplied(true)
                .verticalMarginsNormalized(true)
                .build();

        attrs.add(Sides.TWO_SIDED_LONG_EDGE);
        attrs.add(new Copies(5));

        PrintRequestAttributeSet copy1 = setup.attributesCopy();
        assertEquals(Sides.ONE_SIDED, copy1.get(Sides.class));
        assertEquals(new Copies(2), copy1.get(Copies.class));

        copy1.add(Sides.TWO_SIDED_LONG_EDGE);
        copy1.add(new Copies(7));

        PrintRequestAttributeSet copy2 = setup.attributesCopy();
        assertEquals(Sides.ONE_SIDED, copy2.get(Sides.class));
        assertEquals(new Copies(2), copy2.get(Copies.class));

        assertSame(printService, setup.printService());
        assertFalse(setup.matrixPrinter());
        assertTrue(setup.safeFallbackApplied());
        assertTrue(setup.verticalMarginsNormalized());
    }

    @Test
    public void smokePrintPageSetupKeepsDefensivePageFormatCopy() {

        PrintService printService = mock(PrintService.class);
        when(printService.getName()).thenReturn("Smoke Printer");

        PageFormat original = newPageFormat();

        F2PrintPageSetup setup = F2PrintPageSetup.builder()
                .printService(printService)
                .attributes(new HashPrintRequestAttributeSet())
                .pageFormat(original)
                .matrixPrinter(false)
                .build();

        mutatePageFormat(original, 1.0d, 2.0d, 3.0d, 4.0d);

        assertEquals(200.0d, setup.pageWidthPt(), 0.001d);
        assertEquals(300.0d, setup.pageHeightPt(), 0.001d);
        assertEquals(10.0d, setup.imageableXPt(), 0.001d);
        assertEquals(20.0d, setup.imageableYPt(), 0.001d);
        assertEquals(170.0d, setup.imageableWidthPt(), 0.001d);
        assertEquals(260.0d, setup.imageableHeightPt(), 0.001d);
        assertEquals(180.0d, setup.imageableRightPt(), 0.001d);
        assertEquals(280.0d, setup.imageableBottomPt(), 0.001d);

        PageFormat copy = setup.pageFormat();
        mutatePageFormat(copy, 5.0d, 6.0d, 7.0d, 8.0d);

        assertEquals(10.0d, setup.imageableXPt(), 0.001d);
        assertEquals(20.0d, setup.imageableYPt(), 0.001d);
        assertEquals(170.0d, setup.imageableWidthPt(), 0.001d);
        assertEquals(260.0d, setup.imageableHeightPt(), 0.001d);
    }

    @Test
    public void smokePageRenderConfigUsesPrintPageSetupGeometry() {

        PrintService printService = mock(PrintService.class);
        when(printService.getName()).thenReturn("Smoke Printer");

        F2PrintPageSetup setup = F2PrintPageSetup.builder()
                .printService(printService)
                .attributes(new HashPrintRequestAttributeSet())
                .pageFormat(newPageFormat())
                .matrixPrinter(false)
                .build();

        F2AwtPageRenderConfig config =
                F2AwtPageRenderConfig.fromPrintPageSetup(setup);

        assertEquals(200.0d, config.paperWidthPt(), 0.001d);
        assertEquals(300.0d, config.paperHeightPt(), 0.001d);
        assertEquals(10.0d, config.imageableXPt(), 0.001d);
        assertEquals(20.0d, config.imageableYPt(), 0.001d);
        assertEquals(170.0d, config.imageableWidthPt(), 0.001d);
        assertEquals(260.0d, config.imageableHeightPt(), 0.001d);

        assertEquals(144.0d, config.dpi(), 0.001d);
        assertEquals(2.0d, config.scale(), 0.001d);
        assertEquals(400, config.imageWidthPx());
        assertEquals(600, config.imageHeightPx());
        assertFalse(config.debugOverlay());
    }

    @Test
    public void smokePageRenderConfigUsesExplicitDpiAndDebugOverlay() {

        PrintService printService = mock(PrintService.class);
        when(printService.getName()).thenReturn("Smoke Printer");

        F2PrintPageSetup setup = F2PrintPageSetup.builder()
                .printService(printService)
                .attributes(new HashPrintRequestAttributeSet())
                .pageFormat(newPageFormat())
                .matrixPrinter(false)
                .build();

        F2AwtPageRenderConfig config =
                F2AwtPageRenderConfig.fromPrintPageSetup(
                        setup,
                        72.0d,
                        true
                );

        assertEquals(72.0d, config.dpi(), 0.001d);
        assertEquals(1.0d, config.scale(), 0.001d);
        assertEquals(200, config.imageWidthPx());
        assertEquals(300, config.imageHeightPx());
        assertTrue(config.debugOverlay());
    }

    @Test
    public void smokePrintSettingsKeepsDefensiveAttributeCopy() {

        PrintService printService = mock(PrintService.class);
        when(printService.getName()).thenReturn("Smoke Printer");

        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(Sides.ONE_SIDED);
        attrs.add(new Copies(1));

        F2PrintSettings settings = new F2PrintSettings(printService, attrs);

        attrs.add(Sides.TWO_SIDED_LONG_EDGE);
        attrs.add(new Copies(9));

        PrintRequestAttributeSet copy = settings.attributesCopy();
        assertEquals(Sides.ONE_SIDED, copy.get(Sides.class));
        assertEquals(new Copies(1), copy.get(Copies.class));

        copy.add(new Copies(4));

        PrintRequestAttributeSet secondCopy = settings.attributesCopy();
        assertEquals(new Copies(1), secondCopy.get(Copies.class));
        assertSame(printService, settings.printService());
        assertTrue(settings.hasPrintService());
    }

    private static PageFormat newPageFormat() {

        Paper paper = new Paper();
        paper.setSize(200.0d, 300.0d);
        paper.setImageableArea(10.0d, 20.0d, 170.0d, 260.0d);

        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(paper);
        return pageFormat;
    }

    private static void mutatePageFormat(
            PageFormat pageFormat,
            double imageableX,
            double imageableY,
            double imageableWidth,
            double imageableHeight
    ) {
        Paper paper = pageFormat.getPaper();
        paper.setImageableArea(
                imageableX,
                imageableY,
                imageableWidth,
                imageableHeight
        );
        pageFormat.setPaper(paper);
    }
}
