package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRef;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;
import ru.inversion.f2.style.F2StyleProgram;
import ru.inversion.f2.style.F2StyleProgramCompiler;

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
            F2CommandRegistry registry
    ) {
        if (targetCall == null || registry == null)
            return state;

        F2CommandRef ref = registry.resolve(targetCall);
        return ref.def().styleProgram().apply(ref.call(), state, registry);
    }
}