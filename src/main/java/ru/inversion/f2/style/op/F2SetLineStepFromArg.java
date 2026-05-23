package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2SetLineStepFromArg implements F2StyleOp {

    private final int argIndex;
    private final double denominator;

    public F2SetLineStepFromArg(int argIndex, double denominator) {
        this.argIndex = argIndex;
        this.denominator = denominator;
    }

    public int argIndex() {
        return argIndex;
    }

    public double denominator() {
        return denominator;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.VERTICAL_MOVE;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry
    ) {
        if (call == null || denominator <= 0.0d)
            return state;

        int n = call.intArg(argIndex, -1);

        if (n < 0)
            return state;

        double pt = 72.0d * ((double) n / denominator);

        return state.withLineStepPt(pt);
    }
}