package ru.inversion.f2.prepared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
    Зона ответственности:
    одна логическая строка prepared output: список styled run-ов + line layout snapshot.
*/
public final class F2StyledLine {

    private final List<F2StyledTextRun> runs;
    private final double lineStepPt;
    private final double leftIndentPt;

    public F2StyledLine(
            List<F2StyledTextRun> runs,
            double lineStepPt,
            double leftIndentPt
    ) {
        if (runs == null || runs.isEmpty()) {
            this.runs = Collections.emptyList();
        }
        else {
            this.runs = Collections.unmodifiableList(
                    new ArrayList<F2StyledTextRun>(runs)
            );
        }

        this.lineStepPt = lineStepPt;
        this.leftIndentPt = leftIndentPt;
    }

    public List<F2StyledTextRun> runs() {
        return runs;
    }

    public double lineStepPt() {
        return lineStepPt;
    }

    public double leftIndentPt() {
        return leftIndentPt;
    }

    public boolean isEmpty() {
        return runs.isEmpty();
    }

    public String plainText() {
        StringBuilder sb = new StringBuilder();

        for (F2StyledTextRun run : runs)
            sb.append(run.text());

        return sb.toString();
    }

    @Override
    public String toString() {
        return "F2StyledLine{"
                + "runs=" + runs
                + ", lineStepPt=" + lineStepPt
                + ", leftIndentPt=" + leftIndentPt
                + '}';
    }
}