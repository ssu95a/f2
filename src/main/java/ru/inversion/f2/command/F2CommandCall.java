package ru.inversion.f2.command;

import ru.inversion.f2.error.F2Errors;
import ru.inversion.utils.Checks;
import ru.inversion.utils.S;
import ru.inversion.utils.U;
import ru.inversion.utils.converter.TypeConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Вызов команды из prepared text stream или из INI Cmd=`...`.
 *
 * Примеры:
 *   `UNDER+`          -> name="UNDER+", args=[]
 *   `VERT_INCH_72,12` -> name="VERT_INCH_72", args=["12"]
 *   `LEFT,5`          -> name="LEFT", args=["5"]
 */
public final class F2CommandCall {

    private final String raw;

    private final String name;
    private final List<String> args;

    /** */
    public F2CommandCall( String name, List<String> args, String raw )
    {
        Checks.Require.text( name,"name" );

        this.name = normalizeName(name);

        if( args == null )
            this.args = Collections.emptyList();
        else
            this.args = Collections.unmodifiableList( new ArrayList<>(args) );

        this.raw = raw;
    }

    public static F2CommandCall of( String name)
    {
        return new F2CommandCall( name, Collections.<String>emptyList(), name );
    }

    public static F2CommandCall of(String name, List<String> args) {
        return new F2CommandCall(name, args, null);
    }

    public String name() {
        return name;
    }

    public List<String> args() {
        return args;
    }

    public int argCount() {
        return args.size();
    }

    public boolean hasArgs() {
        return !args.isEmpty();
    }

    public String raw() {
        return raw;
    }

    public String arg(int index) { return index >= 0 && index < args.size() ? args.get(index) : null; }

    public String arg(int index, String defaultValue) {
        String value = arg(index);
        return value == null ? defaultValue : value;
    }

    public int intArg(int index, int defaultValue) {
        return arg(index) == null ? defaultValue : TypeConverter.convert( arg(index), Integer.class );
    }

    public double doubleArg(int index, double defaultValue) {
        String value = arg(index);

        if (value == null || value.trim().length() == 0)
            return defaultValue;

        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String normalizeName(String name) {
        return name.trim().toUpperCase(Locale.ENGLISH);
    }

    @Override
    public String toString( ) {
        return "F2CommandCall {" + "name='" + name + '\'' + ", args=" + args + ", raw='" + raw + '\'' + '}';
    }

    /** */
    public static F2CommandCall parse( String raw )
    {
        String s;

        if( S.isNullOrEmpty(raw) || (s = raw.trim()).isEmpty() )
            throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID).param( "reason", "Command call is empty").param( "raw", raw );

        if( s.charAt(0) == '`' || S.lastChar(s) == '`' )
        {
            if( !( s.charAt(0) == '`' && S.lastChar(s) == '`' && s.length() >= 2 ) )
            {
                throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                        .param("reason", "Invalid command quote")
                        .param("raw", raw);
            }

            s = s.substring( 1, s.length() - 1 ).trim();
        }

        if( s.isEmpty() )
        {
            throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                    .param("reason", "Command name is empty")
                    .param("raw", raw);
        }

        final String[] parts = s.split("[,=]",-1);

        final String name = parts[0].trim();

        if( name.isEmpty() )
            throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                    .param("reason", "Command name is empty")
                    .param("raw", raw);


        final List<String> args = new ArrayList<String>(parts.length);

        for( int i = 1; i < parts.length; i++) {

             String arg = parts[i].trim();

            if( arg.isEmpty() )
                throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                        .param("reason", "Empty command argument")
                        .param("raw",   raw)
                        .param("index", i - 1 );

            args.add(arg);
        }

        return new F2CommandCall( name, args, raw );
    }

}