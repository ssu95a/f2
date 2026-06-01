package ru.inversion.f2;

import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.print.F2PrinterMan;
import ru.inversion.utils.Checks;

public final class F2Runtime {

    private static volatile F2Runtime instance;

    private final F2AltIniModel     iniModel;
    private final F2CommandRegistry commandRegistry;
    private final F2PrinterMan      printerMan;

    /* */
    private F2Runtime(F2AltIniModel iniModel) {

        this.iniModel        = Checks.Require.object(iniModel, "iniModel");

        this.commandRegistry = F2CommandRegistry.make(iniModel);
        this.printerMan      = F2PrinterMan.init(iniModel);
    }

    public static F2Runtime init( F2AltIniModel iniModel ) {

        synchronized (F2Runtime.class) {
            instance = new F2Runtime(iniModel);
            return instance;
        }
    }

    /** */
    public static F2Runtime get() {

        F2Runtime result = instance;

        if( result == null )
            throw new IllegalStateException("F2Runtime is not initialized");

        return result;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public F2AltIniModel iniModel() {
        return iniModel;
    }

    public F2CommandRegistry commandRegistry() {
        return commandRegistry;
    }

    public F2PrinterMan printerMan() {
        return printerMan;
    }
}