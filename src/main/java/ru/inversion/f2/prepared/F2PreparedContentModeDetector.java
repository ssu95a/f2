package ru.inversion.f2.prepared;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandDef;
import ru.inversion.f2.command.F2CommandProperty;
import ru.inversion.f2.command.F2CommandPropertyValue;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.utils.Checks;
import ru.inversion.utils.S;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class F2PreparedContentModeDetector {

    /** */
    public F2PreparedContentMode detect( List<F2PreparedToken> tokens, F2CommandRegistry registry )
    {
        Checks.Require.object( registry, "registry" );

        if( tokens == null || tokens.isEmpty() )
            return F2PreparedContentMode.PLAIN;

        boolean payloadStarted      = false;
        boolean headerHasFormatting = false;

        for( F2PreparedToken token : tokens )
        {
            if( token == null )
                continue;

            switch( token.type() )
            {
                case TEXT:
                    if( !S.isNullOrEmpty(token.text()))
                        payloadStarted = true;
                    break;
                case NEW_LINE:
                    /*
                     * Пустые строки до payload считаем частью header.
                     */
                    break;
                case COMMAND:
                    if( hasFormatting( token.commandCall(), registry) )
                    {
                        if( payloadStarted )
                            return F2PreparedContentMode.STYLED;

                        headerHasFormatting = true;
                    }
                    break;
                default:
                    break;
            }
        }
        return headerHasFormatting ? F2PreparedContentMode.PLAIN_WITH_HEADER : F2PreparedContentMode.PLAIN;
    }

    /** */
    private boolean hasFormatting( F2CommandCall call, F2CommandRegistry registry )
    {
        return hasFormatting( call, registry, new HashSet<>() );
    }

    private boolean hasFormatting(F2CommandCall call, F2CommandRegistry registry, Set<String> visited )
    {
        if( call == null )
            return false;

        String name = call.name();

        if( S.isNullOrEmpty(name) )
            return false;

        if( !visited.add(name) )
             return false;

        F2CommandDef def = registry.find(name);

        if( def == null )
            return false;

        for( F2CommandPropertyValue pv : def.properties() )
        {
            if( pv == null )
                continue;

            F2CommandProperty property = pv.property();

            if( property == null )
                continue;

            if( property.isFormatting() )
                return true;

            if( property == F2CommandProperty.CMD )
            {
                F2CommandCall nested = pv.valueAs( F2CommandCall.class, null );

                if( hasFormatting( nested, registry, visited ) )
                    return true;
            }
        }

        return false;
    }
}