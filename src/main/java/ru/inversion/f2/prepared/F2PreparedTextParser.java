package ru.inversion.f2.prepared;

import ru.inversion.f2.error.F2Errors;
import ru.inversion.f2.command.F2CommandCall;
import ru.inversion.utils.ReaderScanner;
import ru.inversion.utils.S;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class F2PreparedTextParser {

    public List<F2PreparedToken> parse( String text )
    {
        final List<F2PreparedToken> result = new ArrayList<>();

        final StringBuilder plain = new StringBuilder();

        if( S.isNullOrEmpty(text) )
            return result;

        final Iterator<ReaderScanner.IContext> iter = ReaderScanner.newIterable(text).iterator();

        while( iter.hasNext() )
        {
            ReaderScanner.IContext ctx = iter.next();

            char ch = ctx.current();

            if( ch == '\n' )
            {
                flushText ( result, plain );

                result.add( F2PreparedToken.newLine() );

                continue;
            }

            if( ch == '`' )
            {
                flushText( result, plain );

                F2CommandCall call = readCommandCall( iter, text, ctx );

                result.add( F2PreparedToken.command(call) );

                continue;
            }

            plain.append(ch);
        }

        flushText(result, plain);

        return result;
    }

    /** */
    private F2CommandCall readCommandCall(
        Iterator<ReaderScanner.IContext> iter,
        String sourceText,
        ReaderScanner.IContext startCtx
    )
    {
        final ReaderScanner.Position position = startCtx.position();

        final StringBuilder sb = new StringBuilder();

        boolean closed = false;

        while( iter.hasNext() )
        {
            ReaderScanner.IContext ctx = iter.next();

            char ch = ctx.current();

            if( ch == '`' ) {
                closed = true;
                break;
            }

            /*
             * Команда через физический перевод строки выглядит подозрительно.
             * Лучше падать сразу, чем потом получить "команду" из половины файла.
             */
            if( ch == '\n' )
            {
                throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                        .param("reason", "Command quote crosses line break")
                        .param("line", position.lineNum() )
                        .param("symb", position.symbNum() )
                        .param("text", sourceText );
            }

            sb.append(ch);
        }//end while

        if (!closed)
        {
            throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                    .param("reason", "Unclosed command quote")
                    .param("line", position.lineNum() )
                    .param("symb", position.symbNum() )
                    .param("text", sourceText);
        }

        if( sb.length() == 0 )
        {
            throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                    .param("reason", "Empty command")
                    .param("line", position.lineNum() )
                    .param("symb", position.symbNum() )
                    .param("text", sourceText);
        }

        return F2CommandCall.parse( sb.toString() );
    }

    /** */
    private void flushText( List<F2PreparedToken> result, StringBuilder plain )
    {
        if( plain.length() == 0 )
            return;

        result.add( F2PreparedToken.text(plain.toString()) );
        plain.setLength(0);
    }
}