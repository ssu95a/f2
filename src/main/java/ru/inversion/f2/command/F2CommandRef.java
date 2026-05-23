package ru.inversion.f2.command;

import ru.inversion.utils.Checks;

/**
    Связка конкретного вызова команды и найденного определения.
    Нужен потому что call содержит args, а def содержит тело команды.
 */
public final class F2CommandRef {

    private final F2CommandCall call;
    private final F2CommandDef  def;

    /** */
    public F2CommandRef(F2CommandCall call, F2CommandDef def) {

        Checks.Require.object( call, "call");
        Checks.Require.object( def , "def" );

        this.call = call;
        this.def  = def;
    }

    public F2CommandCall call() {
        return call;
    }

    public F2CommandDef def() {
        return def;
    }

    public String name() {
        return call.name();
    }
}