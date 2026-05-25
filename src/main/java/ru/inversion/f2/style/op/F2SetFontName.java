package ru.inversion.f2.style.op;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.control.F2ControlSink;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleOp;
import ru.inversion.utils.S;

public final class F2SetFontName implements F2StyleOp {

    private final String value;

    public F2SetFontName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public F2CommandProperty property() {
        return F2CommandProperty.FONT_NAME;
    }

    @Override
    public F2RenderState apply(
        F2CommandCall call,
        F2RenderState state,
        F2CommandRegistry registry,
        F2ControlSink control
    )
    {
        if(S.isNullOrEmpty(value))
            return state;

        return state.withStyle( state.style().withFontName(value.trim() ) );
    }
}