package ru.inversion.f2.prepared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
    Зона ответственности:
    результат интерпретации prepared tokens: список styled lines.
*/
public final class F2StyledDocument {

    private final List<F2StyledLine> lines;

    public F2StyledDocument(List<F2StyledLine> lines)
    {
        if (lines == null || lines.isEmpty()) {
            this.lines = Collections.emptyList();
        }
        else {
            this.lines = Collections.unmodifiableList(
                    new ArrayList<F2StyledLine>(lines)
            );
        }
    }

    public List<F2StyledLine> lines() {
        return lines;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public int lineCount() {
        return lines.size();
    }
}