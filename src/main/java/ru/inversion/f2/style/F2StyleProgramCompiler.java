package ru.inversion.f2.style;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.f2.command.*;
import ru.inversion.f2.style.op.*;
import ru.inversion.utils.Checks;
import ru.inversion.utils.S;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public final class F2StyleProgramCompiler {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** */
    public F2StyleProgram compile( F2CommandDef def )
    {

        Checks.Require.object(def,"def");

        List<F2StyleOp> ops = new ArrayList<>();

        for( F2CommandPropertyValue pv : def.properties() )
        {
            F2StyleOp op = compileProperty(def, pv);

            if( op != null )
                ops.add(op);
        }

        log.debug("Compiled F2 style program: command={}, ops={}", def.name(), ops.size());

        return new F2StyleProgram(ops);
    }

    /** */
    private F2StyleOp compileProperty(F2CommandDef def, F2CommandPropertyValue pv) {

        if( pv == null )
            return null;

        F2CommandProperty property = pv.property();

        if (property == null) {
            log.warn( "Unknown F2 command property: command={}, rawName={}, rawValue={}", def.name(), pv.rawName(), pv.rawValue() );
            return null;
        }

        switch (property) {
            case UNDER:
                return F2SetUnderline.of(pv.valueAs(Boolean.class, Boolean.FALSE));
            case BOLD:
                return F2SetBold.of(pv.valueAs(Boolean.class, Boolean.FALSE));
            case ITALIC:
                return F2SetItalic.of(pv.valueAs(Boolean.class, Boolean.FALSE));
            case FONT_NAME:
                return new F2SetFontName(pv.valueAs(String.class, null));
            case FONT_SIZE:
                return new F2SetFontSize  ( pv.valueAs(Integer.class, 10));
            case LEFT:
                return new F2SetLeftIndent( pv.valueAs(Double.class, 0.0d));
            case VERTICAL_MOVE:
                return compileVerticalMove( pv.rawValue());
            case CMD:
                return new F2InvokeCommand( pv.valueAs(F2CommandCall.class, null));
            case PAGE_END:
                return F2PageEnd.INSTANCE;
            case LF:
                return F2LineFeed.INSTANCE;
            default:
                log.warn("Unsupported F2 style property: command={}, property={}", def.name(), property);
                return null;
        }
    }
    /** */
    private F2StyleOp compileVerticalMove(String value) {

        if( S.isNullOrEmpty(value) )
            return null;

        String s = value.trim();

        if( "1/6".equals(s))
            return new F2SetLineStep(72.0d / 6.0d);

        if ("1/8".equals(s))
            return new F2SetLineStep(72.0d / 8.0d);

        if ("7/72".equals(s))
            return new F2SetLineStep(7.0d);

        if ("n/72".equalsIgnoreCase(s))
            return new F2SetLineStepFromArg(0, 72.0d);

        if ("n/216".equalsIgnoreCase(s))
            return new F2SetLineStepFromArg(0, 216.0d);

        log.warn("Unknown Vertical Move value: {}", value);

        return null;
    }
}