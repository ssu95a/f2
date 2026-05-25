package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledTextRun;
import ru.inversion.f2.style.F2Style;
import ru.inversion.utils.Checks;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.util.List;

public final class F2AwtLineRenderer {

    /** */
    public F2AwtLineMetrics measure (
        Graphics2D g,
        F2StyledLine line
    )
    {
        Checks.Require.object( g,"g");

        if( line == null || line.isEmpty() )
            return new F2AwtLineMetrics( 0.0d, 0.0d, 0.0d, 0.0d );

        double width      = 0.0d;
        double maxAscent  = 0.0d;
        double maxDescent = 0.0d;
        double maxLeading = 0.0d;

        final List<F2StyledTextRun> runs = line.runs();

        for( F2StyledTextRun run : runs )
        {
            if( run == null || run.isEmpty() )
                continue;

            TextLayout layout = createTextLayout( g, run );

            width += layout.getAdvance();

            maxAscent = Math.max(maxAscent, layout.getAscent());
            maxDescent = Math.max(maxDescent, layout.getDescent());
            maxLeading = Math.max(maxLeading, layout.getLeading());
        }

        return new F2AwtLineMetrics( width, maxAscent, maxDescent, maxLeading );
    }

    /**
     * Рисует строку по заданному baseline.
     *
     * Все runs строки рисуются на одной baselineY.
     */
    public F2AwtLineMetrics paint(
            Graphics2D g,
            F2StyledLine line,
            double xPt,
            double baselineYPt
    )
    {
        Checks.Require.object( g,"g");

        final F2AwtLineMetrics metrics = measure( g, line);

        if( line == null || line.isEmpty() )
            return metrics;

        double x = xPt;

        for (F2StyledTextRun run : line.runs()) {
            if (isEmpty(run))
                continue;

            TextLayout layout = createTextLayout(g, run);

            layout.draw(g, (float) x, (float) baselineYPt);

            if (run.style() != null && run.style().underline()) {
                paintUnderline(
                        g,
                        x,
                        baselineYPt,
                        layout.getAdvance(),
                        layout.getDescent()
                );
            }

            x += layout.getAdvance();
        }

        return metrics;
    }

    private TextLayout createTextLayout(
            Graphics2D g,
            F2StyledTextRun run
    ) {
        Font font = toAwtFont(run.style());

        /*
         * TextLayout берёт FontRenderContext из того же Graphics2D,
         * которым потом рисуем. Это важно для совпадения measure/paint.
         */
        return new TextLayout(
                run.text(),
                font,
                g.getFontRenderContext()
        );
    }

    /** */
    private Font toAwtFont(F2Style style) {
        String fontName = style == null || style.fontName() == null
                ? "Courier New"
                : style.fontName();

        int fontSize = style == null
                ? 10
                : style.fontSize();

        if (fontSize <= 0)
            fontSize = 10;

        int awtStyle = Font.PLAIN;

        if (style != null && style.bold())
            awtStyle |= Font.BOLD;

        if (style != null && style.italic())
            awtStyle |= Font.ITALIC;

        return new Font( fontName, awtStyle, fontSize );
    }

    /** */
    private void paintUnderline (
        Graphics2D g,
        double x,
        double baselineY,
        double width,
        double descent
    )
    {
        /*
         * MVP:
         * underline вручную, чтобы не зависеть от AttributedString.
         * Позиция немного ниже baseline.
         */
        double y = baselineY + Math.max(1.0d, descent * 0.35d);

        g.draw( new java.awt.geom.Line2D.Double( x, y, x + width, y  ));
    }

    /** */
    private static boolean isEmpty(F2StyledTextRun run) {
        return run == null || run.isEmpty();
    }
}