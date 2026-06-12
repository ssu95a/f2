package ru.inversion.f2.prepared;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.utils.Checks;
import ru.inversion.utils.S;

public final class F2PreparedToken {

    private static final F2PreparedToken NEW_LINE = new F2PreparedToken( Type.NEW_LINE, "\n", null );

    public enum Type {
        TEXT,
        COMMAND,
        NEW_LINE
    }

    private final Type   type;
    private final String text;
    private final F2CommandCall commandCall;

    private F2PreparedToken( Type type, String text, F2CommandCall commandCall )
    {
        this.type = type;
        this.text = text;
        this.commandCall = commandCall;
    }

    public Type type() {
        return type;
    }

    public String text() {
        return text;
    }

    public F2CommandCall commandCall() {
        return commandCall;
    }

    @Override
    public String toString() {
        return "F2PreparedToken{" + "type=" + type + ", text='" + text + '\'' + ", commandCall=" + commandCall + '}';
    }

    /** */
    public static F2PreparedToken text( String value )
    {
        return new F2PreparedToken( Type.TEXT, value == null ? S.EMPTY_STRING : value, null );
    }

    /** */
    public static F2PreparedToken command(F2CommandCall call) {
        return new F2PreparedToken( Type.COMMAND, null, Checks.Require.object( call,"call" ) );
    }

    /** */
    public static F2PreparedToken newLine() {
        return NEW_LINE;
    }

}