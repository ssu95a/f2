package ru.inversion.f2.style;

/** */
public final class F2RenderState {

    private final F2Style style;

    private final double lineStepPt;
    private final double leftIndentPt;

    public F2RenderState(
        F2Style style,
        double lineStepPt,
        double leftIndentPt
    )
    {
        this.style        = style == null ? F2Style.defaultStyle() : style;
        this.lineStepPt   = lineStepPt;
        this.leftIndentPt = leftIndentPt;
    }

    public static F2RenderState initial ( ) {
        return new F2RenderState(F2Style.defaultStyle(), 12.0d, 0.0d);
    }

    public F2Style style() {
        return style;
    }

    public F2RenderState withStyle(F2Style value) {
        return new F2RenderState(value, lineStepPt, leftIndentPt);
    }

    public F2RenderState withLineStepPt(double value) {
        return new F2RenderState(style, value, leftIndentPt);
    }

    public F2RenderState withLeftIndentPt(double value) {
        return new F2RenderState(style, lineStepPt, value);
    }

    public double lineStepPt() {
        return lineStepPt;
    }

    public double leftIndentPt() {
        return leftIndentPt;
    }
}
