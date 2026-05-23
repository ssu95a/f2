package ru.inversion.f2.style;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandEffect;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandRegistry;

public interface F2StyleOp {

    F2CommandProperty property();

    default F2CommandEffect effect() {
        F2CommandProperty p = property();
        return p == null ? null : p.effect();
    }

    F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry
    );
}