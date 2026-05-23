package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;

public final class F2SetItalic implements F2StyleOp {

    public static final F2SetItalic ON  = new F2SetItalic(true);
    public static final F2SetItalic OFF = new F2SetItalic(false);

    private final boolean value;

    private F2SetItalic(boolean value) {
        this.value = value;
    }

    public static F2SetItalic of(boolean value) {
        return value ? ON : OFF;
    }

    public boolean value() {
        return value;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.ITALIC;
    }

    @Override
    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry
    ) {
        return state.withStyle(
                state.style().withItalic(value)
        );
    }
}