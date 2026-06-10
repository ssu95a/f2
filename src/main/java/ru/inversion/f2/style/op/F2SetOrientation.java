package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.control.F2ControlSink;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

import javax.print.attribute.standard.OrientationRequested;

public final class F2SetOrientation implements F2StyleOp {

    private final OrientationRequested orientation;

    public F2SetOrientation(OrientationRequested orientation) {
        this.orientation = orientation == null ? OrientationRequested.PORTRAIT : orientation;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.ORIENTATION;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry,
            F2ControlSink control
    ) {
        if (state == null)
            return F2RenderState.initial().withOrientation(orientation);

        return state.withOrientation(orientation);
    }
}
