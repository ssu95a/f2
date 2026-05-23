package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2LineFeed implements F2StyleOp {

    public static final F2LineFeed INSTANCE = new F2LineFeed();

    private F2LineFeed() {
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.LF;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry
    ) {
        /*
         * LF это flow event, не persistent render state.
         * Позже сюда нужен F2FlowSink/F2LayoutContext.
         */
        return state;
    }
}