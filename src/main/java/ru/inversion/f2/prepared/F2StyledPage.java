package ru.inversion.f2.prepared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class F2StyledPage {

    private final List<F2StyledLine> lines;

    public F2StyledPage(List<F2StyledLine> lines)
    {
        if( lines == null || lines.isEmpty() )
            this.lines = Collections.emptyList();
        else
            this.lines = Collections.unmodifiableList( new ArrayList<>(lines) );
    }

    /** */
    public List<F2StyledLine> lines() {
        return lines;
    }

    /** */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /** */
    public int lineCount() {
        return lines.size();
    }
}