package ru.inversion.f2.ini;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.utils.S;
import ru.inversion.utils.ini.IniFileEvent;
import ru.inversion.utils.ini.IniFileEventReader;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class F2AltIniModelLoader {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public F2AltIniModel load( Path iniFile, Charset charset ) throws Exception {

        log.info("Loading F2 ALT INI: file={}, charset={}", iniFile, charset);

        Map<String, String> commands = new LinkedHashMap<>();
        Map<String, String> codeText = new LinkedHashMap<>();
        Map<String, String> codeGraphics = new LinkedHashMap<>();
        Map<String, String> driverRef = new LinkedHashMap<>();

        String section = IniFileEvent.DEFAULT_SECTION;

        try (IniFileEventReader reader = IniFileEventReader.newBuilder().iniFile(iniFile).charset(charset).semicolonPartOfValue(true).hashPartOfValue(false).build() )
        {
            while( reader.hasNext() )
            {
                IniFileEvent e = reader.next();

                if( e.type() == IniFileEvent.Type.Section ) {
                    section = normalizeSectionName(e.value());
                    continue;
                }

                if( e.type() != IniFileEvent.Type.Parameter)
                    continue;

                Map<String, String> target = targetMap (
                    section, commands, codeText, codeGraphics, driverRef
                );

                if( target == null )
                    continue;

                if( e.key() == null || e.key().trim().isEmpty() )
                    continue;

                target.put( e.key().trim(), e.value() );
            }
        }

        return new F2MapAltIniModel( commands, codeText, codeGraphics, driverRef );
    }

    /**
     *
     */
    private static String normalizeSectionName(String section) {
        if (section == null)
            return S.EMPTY_STRING;

        String s = section.trim();
        /*
         * Историческая опечатка в старом INI.
         */
        if ("CodeGraphincs".equalsIgnoreCase(s))
            return "CodeGraphics";

        return s;
    }

    private static Map<String, String> targetMap(
            String section,
            Map<String, String> commands,
            Map<String, String> codeText,
            Map<String, String> codeGraphics,
            Map<String, String> driverRef
    )
    {
        if( "Commands".equalsIgnoreCase(section) )
            return commands;

        if( "CodeText".equalsIgnoreCase(section))
            return codeText;

        if( "CodeGraphics".equalsIgnoreCase(section))
            return codeGraphics;

        if( "DriverRef".equalsIgnoreCase(section))
            return driverRef;

        return null;
    }
}
