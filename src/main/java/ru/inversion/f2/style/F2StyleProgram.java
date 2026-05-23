package ru.inversion.f2.style;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class F2StyleProgram {

    private static final F2StyleProgram EMPTY =
            new F2StyleProgram(Collections.<F2StyleOp>emptyList());

    private final List<F2StyleOp> ops;

    public F2StyleProgram(List<F2StyleOp> ops) {
        if (ops == null || ops.isEmpty()) {
            this.ops = Collections.emptyList();
        }
        else {
            this.ops = Collections.unmodifiableList(
                    new ArrayList<F2StyleOp>(ops)
            );
        }
    }

    public static F2StyleProgram empty() {
        return EMPTY;
    }

    public List<F2StyleOp> ops() {
        return ops;
    }

    public boolean isEmpty() {
        return ops.isEmpty();
    }

    public int size() {
        return ops.size();
    }

    public F2RenderState apply(
            F2CommandCall call,
            F2RenderState state,
            F2CommandRegistry registry
    ) {
        F2RenderState current = state == null
                ? F2RenderState.initial()
                : state;

        for (F2StyleOp op : ops) {
            if (op != null)
                current = op.apply(call, current, registry);
        }

        return current;
    }
}