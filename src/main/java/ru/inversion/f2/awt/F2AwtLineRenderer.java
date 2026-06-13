package ru.inversion.f2.awt;

import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledTextChunk;
import ru.inversion.f2.style.F2Style;
import ru.inversion.utils.Checks;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class F2AwtLineRenderer {

    /** */
    public F2AwtLineMetrics measure ( Graphics2D g, F2StyledLine line )
    {
        Checks.Require.object( g,"g");

        if( line == null || line.isEmpty() )
            return new F2AwtLineMetrics( 0.0d, 0.0d, 0.0d, 0.0d );

        double width      = 0.0d;
        double maxAscent  = 0.0d;
        double maxDescent = 0.0d;
        double maxLeading = 0.0d;

        final List<F2StyledTextChunk> runs = line.chunks();

        for( F2StyledTextChunk run : runs )
        {
            if( run == null || run.isEmpty() )
                continue;

            TextLayout layout = createTextLayout( g, run );

            width += layout.getAdvance();

            maxAscent  = Math.max( maxAscent,  layout.getAscent()  );
            maxDescent = Math.max( maxDescent, layout.getDescent());
            maxLeading = Math.max( maxLeading, layout.getLeading());
        }

        return new F2AwtLineMetrics( width, maxAscent, maxDescent, maxLeading );
    }

    /**
     * Рисует строку по-заданному baseline.
     * Все элементы строки рисуются на одной baselineY.
     */
    public F2AwtLineMetrics paint( Graphics2D g, F2StyledLine line, double xPt, double baselineYPt )
    {
        Checks.Require.object( g, "g" );

        final F2AwtLineMetrics metrics = measure( g, line );

        if( line == null || line.isEmpty() )
            return metrics;

        double x = xPt;

        for( F2StyledTextChunk chunk : line.chunks() )
        {
            if( chunk == null || chunk.isEmpty() )
                continue;

            TextLayout layout = createTextLayout( g, chunk );

            layout.draw( g, (float) x, (float) baselineYPt );

            if( chunk.style() != null && chunk.style().underline() )
                drawUnderline( g, x, baselineYPt, layout.getAdvance(), layout.getDescent() );

            x += layout.getAdvance();
        }

        return metrics;
    }

    /** */
    private TextLayout createTextLayout (
        Graphics2D g,
        F2StyledTextChunk chunk
    )
    {
        Font font = toAwtFont (chunk.style() );
        /*
         * TextLayout берёт FontRenderContext из того же Graphics2D,
         * которым потом рисуем. Это важно для совпадения measure/paint.
         */
        return new TextLayout (
            chunk.text(), font, g.getFontRenderContext()
        );
    }


    final static private Map<String, Font> fontCache = new ConcurrentHashMap<>();

    /** */
    private Font toAwtFont( F2Style style )
    {
        String fontName = style.fontName();

        final int[] fp = new int[2];

        int fontSize = style.fontSize();

        if( fontSize <= 0 )
            fontSize = 10;

        int awtStyle = Font.PLAIN;

        if( style.bold() )
            awtStyle |= Font.BOLD;

        if( style.italic() )
            awtStyle |= Font.ITALIC;

        fp[0] = fontSize;
        fp[1] = awtStyle;

        String key = String.join( "-", fontName,  Integer.toString(fontSize),  Integer.toString( awtStyle ) );

        return fontCache.computeIfAbsent( key, (k)-> new Font( fontName, fp[1], fp[0] )  );
    }

    /**
     * Underline вручную, чтобы не зависеть от AttributedString.
     * Позиция немного ниже baseline.
     */
    private void drawUnderline(
        Graphics2D g,
        double x,
        double baselineY,
        double width,
        double descent
    )
    {
        double y = baselineY + Math.max( 1.0d, descent * 0.35d );

        g.draw( new java.awt.geom.Line2D.Double( x, y, x + width, y  ));
    }

}