package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.control.F2ControlSink;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2SetLeftIndent implements F2StyleOp {

    private final double leftIndentPt;

    public F2SetLeftIndent(Double leftIndentPt) {
        this(leftIndentPt == null ? 0.0d : leftIndentPt.doubleValue());
    }

    public F2SetLeftIndent(double leftIndentPt) {
        this.leftIndentPt = leftIndentPt;
    }

    public double leftIndentPt() {
        return leftIndentPt;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.LEFT;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry,
            F2ControlSink control
    ) {
        return state.withLeftIndentPt(leftIndentPt);
    }
}