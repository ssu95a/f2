package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.control.F2ControlSink;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2SetFontSize implements F2StyleOp {

    private final int value;

    public F2SetFontSize(Integer value) {
        this(value == null ? 10 : value );
    }

    public F2SetFontSize(int value) {
        this.value = value <= 0 ? 10 : value;
    }

    public int value() {
        return value;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.FONT_SIZE;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry,
            F2ControlSink control
    ) {
        return state.withStyle(
                state.style().withFontSize(value)
        );
    }
}