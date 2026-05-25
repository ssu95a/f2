package ru.inversion.f2.awt;

public final class F2AwtLineMetrics {

    private final double widthPt;
    private final double ascentPt;
    private final double descentPt;
    private final double leadingPt;

    public F2AwtLineMetrics(
            double widthPt,
            double ascentPt,
            double descentPt,
            double leadingPt
    ) {
        this.widthPt = widthPt;
        this.ascentPt = ascentPt;
        this.descentPt = descentPt;
        this.leadingPt = leadingPt;
    }

    public double widthPt() {
        return widthPt;
    }

    public double ascentPt() {
        return ascentPt;
    }

    public double descentPt() {
        return descentPt;
    }

    public double leadingPt() {
        return leadingPt;
    }

    public double heightPt() {
        return ascentPt + descentPt + leadingPt;
    }
}