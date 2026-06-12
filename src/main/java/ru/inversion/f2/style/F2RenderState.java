package ru.inversion.f2.style;

import javax.print.attribute.standard.OrientationRequested;

/** */
public final class F2RenderState {

    private final F2Style style;

    private final double lineStepPt;
    private final double leftIndentPt;
    private final OrientationRequested orientation;

    public F2RenderState(
        F2Style style,
        double lineStepPt,
        double leftIndentPt
    ) {
        this(
                style,
                lineStepPt,
                leftIndentPt,
                OrientationRequested.PORTRAIT
        );
    }

    public F2RenderState(
        F2Style style,
        double lineStepPt,
        double leftIndentPt,
        OrientationRequested orientation
    )
    {
        this.style        = style == null ? F2Style.defaultStyle() : style;
        this.lineStepPt   = lineStepPt;
        this.leftIndentPt = leftIndentPt;
        this.orientation  = orientation == null ? OrientationRequested.PORTRAIT : orientation;
    }

    public static F2RenderState initial ( ) {
        return new F2RenderState( F2Style.defaultStyle(), 12.0d, 0.0d, OrientationRequested.PORTRAIT );
    }

    public F2Style style() {
        return style;
    }

    public F2RenderState withStyle(F2Style value) {
        return new F2RenderState(value, lineStepPt, leftIndentPt, orientation);
    }

    public F2RenderState withLineStepPt(double value) {
        return new F2RenderState(style, value, leftIndentPt, orientation);
    }

    public F2RenderState withLeftIndentPt(double value) {
        return new F2RenderState(style, lineStepPt, value, orientation);
    }

    public F2RenderState withOrientation(OrientationRequested value) {
        return new F2RenderState(style, lineStepPt, leftIndentPt, value);
    }

    public double lineStepPt() {
        return lineStepPt;
    }

    public double leftIndentPt() {
        return leftIndentPt;
    }

    public OrientationRequested orientation() {
        return orientation;
    }
}
