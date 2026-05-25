package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.control.F2ControlSink;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2SetBold implements F2StyleOp {

    public static final F2SetBold ON  = new F2SetBold(true);
    public static final F2SetBold OFF = new F2SetBold(false);

    private final boolean value;

    private F2SetBold(boolean value) {
        this.value = value;
    }

    public static F2SetBold of(boolean value) {
        return value ? ON : OFF;
    }

    public boolean value() {
        return value;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.BOLD;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry,
            F2ControlSink control
    ) {
        return state.withStyle(
                state.style().withBold(value)
        );
    }
}