package ru.inversion.f2.ini;

import ru.inversion.utils.S;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class F2MapAltIniModel implements F2AltIniModel {

    private final Map<String, String> commands;
    private final Map<String, String> codeText;
    private final Map<String, String> codeGraphics;
    private final Map<String, String> driverRef;

    public F2MapAltIniModel(
            Map<String, String> commands,
            Map<String, String> codeText,
            Map<String, String> codeGraphics,
            Map<String, String> driverRef
    ) {
        this.commands = cleanCommandMap(commands);
        this.codeText = cleanCommandMap(codeText);
        this.codeGraphics = cleanCommandMap(codeGraphics);
        this.driverRef = cleanCommandMap(driverRef);
    }

    @Override
    public Map<String, String> commands() {
        return commands;
    }

    @Override
    public Map<String, String> codeText() {
        return codeText;
    }

    @Override
    public Map<String, String> codeGraphics() {
        return codeGraphics;
    }

    @Override
    public Map<String, String> driverRef() {
        return driverRef;
    }

    @Override
    public String cleanCommandName(String name) {
        return cleanName(name);
    }

    @Override
    public String commandDescription(String name) {
        return commands.get(cleanCommandName(name));
    }

    @Override
    public String codeText(String name) {
        return codeText.get(cleanCommandName(name));
    }

    @Override
    public String codeGraphics(String name) {
        return codeGraphics.get(cleanCommandName(name));
    }

    @Override
    public String driverRef(String name) {
        return driverRef.get(cleanCommandName(name));
    }

    @Override
    public boolean isMatrixPrinter(String printerName) {
        return isDriverRefMode( printerName, DRIVER_REF_CODE_TEXT );
    }

    @Override
    public boolean isGraphicsPrinter(String printerName) {
        return isDriverRefMode( printerName, DRIVER_REF_CODE_GRAPHICS );
    }

    private boolean isDriverRefMode(
            String printerName,
            String expectedMode
    ) {
        String value = driverRef(printerName);
        if (S.isNullOrEmpty(value))
            return false;
        return normalizeDriverRef(value).equals(normalizeDriverRef(expectedMode));
    }

    /** */
    private static String normalizeDriverRef(String value) {
        return value == null
            ? S.EMPTY_STRING
            : value.trim()
            .replace('_', ' ')
            .replace('-', ' ')
            .replaceAll("\\s+", " ")
            .toUpperCase(Locale.ENGLISH);
    }

    /** */
    private static Map<String, String> cleanCommandMap( Map<String, String> source )
    {
        if( source == null || source.isEmpty() )
            return Collections.emptyMap();

        Map<String, String> result = new LinkedHashMap<String, String>();

        for( Map.Entry<String, String> e : source.entrySet() ) {
            result.put(cleanName(e.getKey()), e.getValue());
        }

        return Collections.unmodifiableMap(result);
    }

    private static String cleanName(String name) {
        return name == null ? S.EMPTY_STRING : name.trim();
    }
}