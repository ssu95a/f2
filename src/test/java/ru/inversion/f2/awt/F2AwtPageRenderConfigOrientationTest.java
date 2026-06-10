package ru.inversion.f2.awt;

import org.junit.Test;
import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandDef;
import ru.inversion.f2.command.F2CommandPropertyValueParser;
import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.f2.style.F2StyleProgram;
import ru.inversion.f2.style.F2StyleProgramCompiler;

import javax.print.PrintService;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class F2AwtPageRenderConfigOrientationTest {

    @Test
    public void smokeLandscapeCommandTurnsRenderConfigSideways() {
        F2StyleProgram program = new F2StyleProgramCompiler().compile(
                new F2CommandDef(
                        "LANDSCAPE",
                        null,
                        new F2CommandPropertyValueParser().parse("Orientation=Landscape"),
                        null
                )
        );

        F2RenderState state = program.apply(
                F2CommandCall.of("LANDSCAPE"),
                F2RenderState.initial(),
                null,
                null
        );

        assertEquals(OrientationRequested.LANDSCAPE, state.orientation());

        F2StyledPage page = new F2StyledPage(
                Collections.<F2StyledLine>emptyList(),
                state.orientation()
        );

        assertTrue(page.isLandscape());

        F2PrintPageSetup setup = F2PrintPageSetup.builder()
                .printService(mock(PrintService.class))
                .pageFormat(newPortraitPageFormat())
                .build();

        F2AwtPageRenderConfig config = F2AwtPageRenderConfig.fromPrintPageSetup(
                setup,
                page,
                144.0d,
                false
        );

        assertEquals(841.9d, config.paperWidthPt(), 0.1d);
        assertEquals(595.3d, config.paperHeightPt(), 0.1d);

        assertEquals(813.5d, config.imageableWidthPt(), 0.1d);
        assertEquals(566.9d, config.imageableHeightPt(), 0.1d);
    }

    private static PageFormat newPortraitPageFormat() {
        Paper paper = new Paper();

        paper.setSize(
                595.3d,
                841.9d
        );

        paper.setImageableArea(
                14.2d,
                14.2d,
                566.9d,
                813.5d
        );

        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(paper);

        return pageFormat;
    }
}
