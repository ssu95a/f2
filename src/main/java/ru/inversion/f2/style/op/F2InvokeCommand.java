package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRef;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.control.F2ControlSink;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2InvokeCommand implements F2StyleOp {

    private final F2CommandCall targetCall;

    public F2InvokeCommand(F2CommandCall targetCall) {
        this.targetCall = targetCall;
    }

    public F2CommandCall targetCall() {
        return targetCall;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.CMD;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry,
            F2ControlSink control
    )
    {
        if( targetCall == null || registry == null )
            return state;

        F2CommandRef ref = registry.resolve(targetCall);

        return ref.def().styleProgram().apply(ref.call(), state, registry,control);
    }
}