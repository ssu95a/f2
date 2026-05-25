package ru.inversion.f2.prepared;

import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.f2.command.F2CommandCallParser;
import ru.inversion.f2.error.F2Errors;
import ru.inversion.utils.S;

import java.util.ArrayList;
import java.util.List;

public final class F2PreparedTextParser {

    /** */
    public List<F2PreparedToken> parse( String text )
    {
        List<F2PreparedToken> result = new ArrayList<>();

        if( S.isNullOrEmpty(text) )
            return result;

        final StringBuilder plain = new StringBuilder();

        for( int i = 0; i < text.length(); i++ )
        {
            char ch = text.charAt(i);

            if( ch == '`' )
            {
                flushText( result, plain );

                int end = text.indexOf('`', i + 1);

                if( end < 0 )
                    throw F2Errors.of( F2Errors.ErrorCode.COMMAND_CALL_INVALID).param("reason", "Unclosed command quote").param( "pos", i ).param("text", text);

                String commandText = text.substring(i, end + 1);
                F2CommandCall call = F2CommandCallParser.parse(commandText);

                result.add(F2PreparedToken.command(call));

                i = end;

                continue;
            }

            if( ch == '\n' )
            {
                flushText(result, plain);
                result.add( F2PreparedToken.newLine() );
                continue;
            }

            if (ch == '\r') {
                /*
                 * CRLF: съедаем CR, NEW_LINE создаём на LF.
                 * CR-only тоже считаем NEW_LINE.
                 */
                flushText(result, plain);

                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    result.add(F2PreparedToken.newLine());
                    i++;
                }
                else {
                    result.add(F2PreparedToken.newLine());
                }

                continue;
            }

            plain.append(ch);
        }

        flushText(result, plain);

        return result;
    }

    private static void flushText( List<F2PreparedToken> result, StringBuilder plain) {

        if( plain.length() == 0 )
            return;

        result.add( F2PreparedToken.text(plain.toString()) );

        plain.setLength(0);
    }
}