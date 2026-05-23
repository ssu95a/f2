package ru.inversion.f2.command;

import ru.inversion.utils.S;
import ru.inversion.utils.U;

import javax.print.attribute.standard.OrientationRequested;

import static ru.inversion.f2.command.F2CommandEffect.*;

// Under, Bold, Size Font, Cmd
public enum F2CommandProperty {

    UNDER        ( "Under",         Boolean.class, PAINT_ONLY   ),
    BOLD         ( "Bold",          Boolean.class, TEXT_METRICS ),
    ITALIC       ( "Italic",        Boolean.class, TEXT_METRICS ),

    FONT_NAME    ( "Name Font",     String.class,  TEXT_METRICS ),
    FONT_SIZE    ( "Size Font",     Integer.class, TEXT_METRICS ),

    VERTICAL_MOVE( "Vertical Move", String.class,  LINE_LAYOUT ),
    LEFT         ( "Left",          Double.class,  LINE_LAYOUT ),

    PAGE_END     ( "Page End",      Boolean.class, FLOW ),
    LF           ( "Lf",            Boolean.class, FLOW ),

    CMD          ( "Cmd",           F2CommandCall.class, null ),

    ORIENTATION  ( "Orientation",   OrientationRequested.class, DOCUMENT_SETUP ),
    SET_COPIES   ( "Set Copies",    Integer.class,              DOCUMENT_SETUP );

    private final String iniName;
    private final F2CommandEffect effect;
    private final Class<?> valueType;

    F2CommandProperty( String iniName, Class<?> valueType, F2CommandEffect effect )
    {
        this.iniName   = iniName;
        this.effect    = effect;
        this.valueType = valueType;
    }

    /** */
    public String iniName() {
        return iniName;
    }

    /** */
    public Class<?> valueType() {
        return valueType;
    }

    /** */
    public F2CommandEffect effect( ) {
        return effect;
    }

    /** */
    public boolean isCmd() {
        return this == CMD;
    }

    /** */
    public boolean isPaintOnly() {
        return effect == PAINT_ONLY;
    }

    public boolean affectsLayout() {
        return U.in( effect, TEXT_METRICS, LINE_LAYOUT, FLOW,  DOCUMENT_SETUP );
    }

    /** */
    public static F2CommandProperty fromIniName(String name)
    {
        if( name == null )
            return null;

        String normalized = normalize(name);

        for( F2CommandProperty property : values() )
        {
            if( normalize(property.iniName).equals(normalized))
                return property;
        }

        return null;
    }

    /** */
    private static String normalize(String value) {
        return value == null
               ? S.EMPTY_STRING
               :
               value.trim().replace('_', ' ').replace('-', ' ').replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ENGLISH);
    }
}
