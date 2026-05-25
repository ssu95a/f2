package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.control.F2ControlSink;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2PageEnd implements F2StyleOp {

    public static final F2PageEnd INSTANCE = new F2PageEnd();

    private F2PageEnd() {
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.PAGE_END;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry,
            F2ControlSink control
    ) {
        if( control != null )
            control.pageEnd();

        return state;
    }
}