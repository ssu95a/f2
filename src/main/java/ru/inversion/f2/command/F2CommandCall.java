package ru.inversion.f2.command;

import ru.inversion.utils.Checks;

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

    private final String name;
    private final List<String> args;
    private final String raw;

    /** */
    public F2CommandCall(String name, List<String> args, String raw)
    {
        Checks.Require.text( name,"name" );

        this.name = normalizeName(name);

        if (args == null)
            this.args = Collections.emptyList();
        else
            this.args = Collections.unmodifiableList(new ArrayList<String>(args));

        this.raw = raw;
    }

    public static F2CommandCall of( String name)
    {
        return new F2CommandCall(name, Collections.<String>emptyList(), name);
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

    public String arg(int index) {
        return index >= 0 && index < args.size()
                ? args.get(index)
                : null;
    }

    public String arg(int index, String defaultValue) {
        String value = arg(index);
        return value == null ? defaultValue : value;
    }

    public int intArg(int index, int defaultValue) {
        String value = arg(index);

        if (value == null || value.trim().length() == 0)
            return defaultValue;

        try {
            return Integer.parseInt(value.trim());
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
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
    public String toString() {
        return "F2CommandCall{"
                + "name='" + name + '\''
                + ", args=" + args
                + ", raw='" + raw + '\''
                + '}';
    }
}