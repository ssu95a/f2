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

    private final Map<String, F2CommandDef> commands;

    private F2CommandRegistry(Map<String, F2CommandDef> commands) {
        this.commands = Collections.unmodifiableMap(
                new LinkedHashMap<String, F2CommandDef>(commands)
        );
    }

    public static F2CommandRegistry from(F2AltIniModel model) {

        Checks.Require.object(model,"model");

        F2CommandPropertyValueParser propertyParser = new F2CommandPropertyValueParser();

        F2StyleProgramCompiler styleCompiler = new F2StyleProgramCompiler();

        Map<String, F2CommandDef> result = new LinkedHashMap<>();

        for( Map.Entry<String, String> e : model.codeGraphics().entrySet() )
        {
            String name = model.cleanCommandName(e.getKey());

            List<F2CommandPropertyValue> properties = propertyParser.parse(e.getValue());

            F2CommandDef def0 = new F2CommandDef(name, null,  properties, null);

            F2StyleProgram styleProgram = styleCompiler.compile(def0);

            F2CommandDef def = new F2CommandDef( name, null, properties, styleProgram);

            result.put(name, def);        }

        return new F2CommandRegistry(result);
    }

    public F2CommandDef find(String name) {
        if (name == null)
            return null;

        return commands.get(name.trim());
    }

    /** */
    public F2CommandRef resolve(F2CommandCall call) {

        if( call == null )
            throw F2Errors.of( F2Errors.ErrorCode.COMMAND_CALL_INVALID ).param("call", null);

        F2CommandDef def = find( call.name() );

        if( def == null )
            throw F2Errors.commandNotFound(call.name(), call.raw());

        return new F2CommandRef(call, def);
    }

    public Collection<F2CommandDef> definitions() {
        return commands.values();
    }

    public int size() {
        return commands.size();
    }
}