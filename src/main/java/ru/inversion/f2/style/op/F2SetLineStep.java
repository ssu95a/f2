package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2SetLineStep implements F2StyleOp {

    public static final F2SetLineStep STEP_1_6 =
            new F2SetLineStep(72.0d / 6.0d);

    public static final F2SetLineStep STEP_1_8 =
            new F2SetLineStep(72.0d / 8.0d);

    private final double lineStepPt;

    public F2SetLineStep(double lineStepPt) {
        this.lineStepPt = lineStepPt;
    }

    public double lineStepPt() {
        return lineStepPt;
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
        if (lineStepPt <= 0.0d)
            return state;

        return state.withLineStepPt(lineStepPt);
    }
}