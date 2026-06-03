package ru.inversion.f2.print;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

/** */
public final class F2PrintSettings {

    private final PrintService printService;
    private final PrintRequestAttributeSet attributes;

    /** */
    public F2PrintSettings( PrintService printService, PrintRequestAttributeSet attributes )
    {
        this.printService = printService;
        this.attributes   = attributes == null
                          ? new HashPrintRequestAttributeSet()
                          : new HashPrintRequestAttributeSet(attributes);
    }

    /** */
    public PrintService printService( ) {
        return printService;
    }

    /** */
    public PrintRequestAttributeSet attributesCopy( ) {
        return new HashPrintRequestAttributeSet(attributes);
    }

    /** */
    public boolean hasPrintService() {
        return printService != null;
    }
}