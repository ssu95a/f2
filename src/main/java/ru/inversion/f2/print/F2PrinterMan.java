package ru.inversion.f2.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.utils.Checks;
import ru.inversion.utils.S;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.lang.invoke.MethodHandles;

public final class F2PrinterMan {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static volatile F2PrinterMan instance;

    private final F2AltIniModel iniModel;

    private PrintService selectedPrintService;

    private PrintRequestAttributeSet selectedPrintAttributes = new HashPrintRequestAttributeSet();

    private final F2PrintPageSetupResolver pageSetupResolver = new F2PrintPageSetupResolver();

    private F2PrinterMan( F2AltIniModel iniModel ) {
        this.iniModel = Checks.Require.object(iniModel, "iniModel");
    }

    /** */
    public F2PrintSettings currentPrintSettings() {
        return new F2PrintSettings( currentPrintService(), selectedPrintAttributes );
    }

    /** */
    public F2PrintPageSetup currentPrintPageSetup() throws Exception {
        return pageSetupResolver.resolve( currentPrintSettings(), isCurrentMatrixPrinter() );
    }

    /** */
    public void selectPrintAttributes(PrintRequestAttributeSet attributes) {
        selectedPrintAttributes =
                attributes == null
                        ? new HashPrintRequestAttributeSet()
                        : new HashPrintRequestAttributeSet(attributes);
    }

    /** */
    public void clearSelectedPrintAttributes() {
        selectedPrintAttributes =
                new HashPrintRequestAttributeSet();
    }

    /** */
    public static F2PrinterMan init( F2AltIniModel iniModel )
    {
        Checks.Require.object( iniModel, "iniModel" );

        synchronized (F2PrinterMan.class) {
            instance = new F2PrinterMan(iniModel);
            return instance;
        }
    }

    /** */
    public static F2PrinterMan getInstance() {

        F2PrinterMan result = instance;

        if (result == null)
            throw new IllegalStateException("F2PrinterMan is not initialized");

        return result;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public PrintService[] printServices() {
        return PrintServiceLookup.lookupPrintServices(null, null);
    }

    public PrintService defaultPrintService() {
        return PrintServiceLookup.lookupDefaultPrintService();
    }

    public PrintService currentPrintService() {

        if( selectedPrintService != null )
            return selectedPrintService;

        return defaultPrintService();
    }

    public String currentPrinterName() {
        PrintService service = currentPrintService();
        return service == null ? null : service.getName();
    }

    public PrintService selectedPrintService() {
        return selectedPrintService;
    }

    public boolean hasSelectedPrintService() {
        return selectedPrintService != null;
    }

    public void clearSelectedPrinter() {
        selectedPrintService = null;
    }

    /**
     * printerNo — 1-based.
     */
    public void selectPrinterNo(int printerNo) {

        PrintService[] services = printServices();

        if (printerNo <= 0 || printerNo > services.length) {
            throw new IllegalArgumentException(
                    "Invalid printerNo=" + printerNo
                            + ", available=" + services.length
            );
        }

        selectedPrintService = services[printerNo - 1];

        log.info(
                "Selected F2 printer: no={}, name={}",
                Integer.valueOf(printerNo),
                selectedPrintService.getName()
        );
    }

    public void selectPrinterName(String printerName) {

        if (S.isNullOrEmpty(printerName))
            throw new IllegalArgumentException("printerName is empty");

        PrintService service = findPrintService(printerName);

        if (service == null)
            throw new IllegalArgumentException("Printer not found: " + printerName);

        selectedPrintService = service;

        log.info("Selected F2 printer: name={}", selectedPrintService.getName());
    }

    public PrintService findPrintService(String printerName) {

        if (S.isNullOrEmpty(printerName))
            return null;

        PrintService[] services = printServices();

        for (PrintService service : services) {
            if (service == null)
                continue;

            if (printerName.equals(service.getName()))
                return service;
        }

        return null;
    }

    public int currentPrinterNo() {

        PrintService current = currentPrintService();

        if (current == null)
            return 0;

        PrintService[] services = printServices();

        for (int i = 0; i < services.length; i++) {
            PrintService service = services[i];

            if (service == null)
                continue;

            if (service.getName().equals(current.getName()))
                return i + 1;
        }

        return 0;
    }

    /**
     * В терминах F2 "matrix printer" означает DriverRef=CodeText.
     * Это НЕ проверка физического типа устройства.
     */
    public boolean isCurrentMatrixPrinter() {

        String printerName = currentPrinterName();

        if (printerName == null)
            return false;

        return isMatrixPrinter(printerName);
    }

    public boolean isCurrentGraphicsPrinter() {

        String printerName = currentPrinterName();

        if (printerName == null)
            return false;

        return isGraphicsPrinter(printerName);
    }

    public boolean isMatrixPrinter(String printerName) {
        return iniModel.isMatrixPrinter(printerName);
    }

    public boolean isGraphicsPrinter(String printerName) {
        return iniModel.isGraphicsPrinter(printerName);
    }

    public String currentDriverRef() {

        String printerName = currentPrinterName();

        if (printerName == null)
            return null;

        return driverRef(printerName);
    }

    public String driverRef(String printerName) {
        return iniModel.driverRef(printerName);
    }

}