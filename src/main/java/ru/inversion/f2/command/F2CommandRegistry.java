package ru.inversion.f2.command;

import ru.inversion.f2.error.F2Errors;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.style.F2StyleProgram;
import ru.inversion.f2.style.F2StyleProgramCompiler;
import ru.inversion.utils.Checks;

import java.util.*;

/**
    Готовый справочник команд, построенный из INI.
    Хранит F2CommandDef по имени.
    Разрешает F2CommandCall -> F2CommandRef.
    Не даёт внешнему коду добавлять команды после сборки.
*/
public final class F2CommandRegistry {

    private static final String INIT_COMMAND_NAME = "INIT";

    private static final String NORMAL_STYLE_COMMAND_NAME = "NORMAL";

    /** */
    private final Map<String, F2CommandDef> commands;

    /** */
    private F2CommandRegistry( Map<String, F2CommandDef> commands ) {
        this.commands = Collections.unmodifiableMap( new LinkedHashMap<>(commands) );
    }

    /** */
    public F2CommandRef initCommand() {
        return resolve (
            F2CommandCall.of(INIT_COMMAND_NAME),
            false
        );
    }

    /** */
    public F2CommandRef normalStyle() {
        return resolve (
            F2CommandCall.of(NORMAL_STYLE_COMMAND_NAME),
            true
        );
    }

    /** */
    public F2CommandDef find( String name ) {
        return name == null ?  null : commands.get( name.trim() );
    }

    /** */
    public F2CommandRef resolve(F2CommandCall call) {
        return resolve(call, true);
    }

    /** */
    public F2CommandRef resolve( F2CommandCall call, boolean required ) {

        if( call == null )
        {
            if( required )
                throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID).param("call", null);

            return null;
        }

        F2CommandDef def = find(call.name());

        if( def == null )
        {
            if( required )
                throw F2Errors.commandNotFound( call.name(), call.raw() );

            return null;
        }

        return new F2CommandRef(call, def);
    }

    /** */
    public Collection<F2CommandDef> definitions() {
        return commands.values();
    }

    public int size() {
        return commands.size();
    }

    /** */
    public static F2CommandRegistry make( F2AltIniModel model )
    {
        Checks.Require.object( model,"model" );

        F2CommandPropertyValueParser propertyParser = new F2CommandPropertyValueParser();

        F2StyleProgramCompiler styleCompiler = new F2StyleProgramCompiler();

        Map<String, F2CommandDef> result = new LinkedHashMap<>();

        for( Map.Entry<String, String> e : model.codeGraphics().entrySet() )
        {
            String name = model.cleanCommandName(e.getKey());

            List<F2CommandPropertyValue> properties = propertyParser.parse(e.getValue());

            F2CommandDef def0 = new F2CommandDef(name, null,  properties, null);

            F2StyleProgram styleProgram = styleCompiler.compile(def0);

            F2CommandDef def = new F2CommandDef( name, null, properties, styleProgram );

            result.put(name, def);
        }

        return new F2CommandRegistry(result);
    }

}