package ru.inversion.f2.prepared;

import javax.print.attribute.standard.OrientationRequested;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class F2StyledPage {

    private final List<F2StyledLine> lines;
    private final OrientationRequested orientation;

    public F2StyledPage(List<F2StyledLine> lines)
    {
        this(lines, OrientationRequested.PORTRAIT);
    }

    public F2StyledPage( List<F2StyledLine> lines, OrientationRequested orientation )
    {
        if( lines == null || lines.isEmpty() )
            this.lines = Collections.emptyList();
        else
            this.lines = Collections.unmodifiableList( new ArrayList<>(lines) );

        this.orientation = orientation == null ? OrientationRequested.PORTRAIT : orientation;
    }

    /** */
    public List<F2StyledLine> lines() {
        return lines;
    }

    public OrientationRequested orientation() {
        return orientation;
    }

    public boolean isLandscape() {
        return OrientationRequested.LANDSCAPE.equals(orientation) || OrientationRequested.REVERSE_LANDSCAPE.equals(orientation);
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
