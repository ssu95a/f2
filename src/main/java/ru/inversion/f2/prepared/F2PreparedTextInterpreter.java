package ru.inversion.f2.prepared;

import ru.inversion.f2.command.F2CommandRef;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.style.F2RenderState;

import java.util.ArrayList;
import java.util.List;

/**
   Зона ответственности:
   применяет COMMAND токены к F2RenderState, а TEXT превращает в styled runs.
*/
public final class F2PreparedTextInterpreter {

    public F2StyledDocument interpret(
            List<F2PreparedToken> tokens,
            F2CommandRegistry registry
    ) {
        if (registry == null)
            throw new IllegalArgumentException("registry is null");

        List<F2StyledLine> lines =
                new ArrayList<F2StyledLine>();

        List<F2StyledTextRun> currentRuns =
                new ArrayList<F2StyledTextRun>();

        F2RenderState state = F2RenderState.initial();

        if (tokens == null || tokens.isEmpty()) {
            lines.add(new F2StyledLine(
                    currentRuns,
                    state.lineStepPt(),
                    state.leftIndentPt()
            ));

            return new F2StyledDocument(lines);
        }

        for (F2PreparedToken token : tokens) {
            if (token == null)
                continue;

            switch (token.type()) {
                case TEXT:
                    if (token.text() != null && token.text().length() > 0) {
                        currentRuns.add(new F2StyledTextRun(
                                token.text(),
                                state.style()
                        ));
                    }
                    break;

                case COMMAND:
                    F2CommandRef ref = registry.resolve(token.commandCall());

                    state = ref.def().styleProgram().apply(
                            ref.call(),
                            state,
                            registry
                    );
                    break;

                case NEW_LINE:
                    lines.add(new F2StyledLine(
                            currentRuns,
                            state.lineStepPt(),
                            state.leftIndentPt()
                    ));

                    currentRuns = new ArrayList<F2StyledTextRun>();
                    break;

                default:
                    throw new IllegalStateException(
                            "Unsupported token type: " + token.type()
                    );
            }
        }

        /*
         * Последняя строка.
         *
         * Если текст закончился на NEW_LINE, currentRuns будет пустой.
         * Пока оставляем пустую строку как реальный tail-line.
         * Если окажется, что это мешает legacy, добавим policy.
         */
        lines.add(new F2StyledLine(
                currentRuns,
                state.lineStepPt(),
                state.leftIndentPt()
        ));

        return new F2StyledDocument(lines);
    }
}