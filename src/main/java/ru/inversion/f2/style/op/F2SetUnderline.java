package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

/** */
public final class F2SetUnderline implements F2StyleOp {

    public static final F2SetUnderline ON  = new F2SetUnderline(true);
    public static final F2SetUnderline OFF = new F2SetUnderline(false);

    private final boolean value;

    private F2SetUnderline(boolean value) {
        this.value = value;
    }

    public static F2SetUnderline of( boolean value ) {
        return value ? ON : OFF;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.UNDER;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry
    ) {
        return state.withStyle(state.style().withUnderline(value));
    }
}