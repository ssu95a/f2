package ru.inversion.f2.prepared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
    Зона ответственности:
    одна логическая строка prepared output: список styled run-ов + line layout snapshot.
*/
public final class F2StyledLine {

    private final List<F2StyledTextChunk> chunks;
    private final double lineStepPt;
    private final double leftIndentPt;

    public F2StyledLine (
            List<F2StyledTextChunk> chunks,
            double lineStepPt,
            double leftIndentPt
    )
    {
        if( chunks == null || chunks.isEmpty() )
            this.chunks = Collections.emptyList();
        else
            this.chunks = Collections.unmodifiableList( new ArrayList<>(chunks) );

        this.lineStepPt   = lineStepPt;
        this.leftIndentPt = leftIndentPt;
    }

    /** */
    public List<F2StyledTextChunk> chunks() {
        return chunks;
    }

    /** */
    public double lineStepPt() {
        return lineStepPt;
    }

    /** */
    public double leftIndentPt() {
        return leftIndentPt;
    }

    /** */
    public boolean isEmpty() {
        return chunks.isEmpty();
    }

    /** */
    public String plainText()
    {
        StringBuilder sb = new StringBuilder();

        for( F2StyledTextChunk run : chunks )
             sb.append( run.text() );

        return sb.toString();
    }

    @Override
    public String toString() {
        return "F2StyledLine{"
                + "chunks=" + chunks
                + ", lineStepPt=" + lineStepPt
                + ", leftIndentPt=" + leftIndentPt
                + '}';
    }
}