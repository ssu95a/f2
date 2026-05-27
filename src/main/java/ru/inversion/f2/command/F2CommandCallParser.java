package ru.inversion.f2.command;

import ru.inversion.f2.error.F2Errors;
import ru.inversion.utils.S;

import java.util.ArrayList;
import java.util.List;

public final class F2CommandCallParser {

    private F2CommandCallParser() {
    }

    public static F2CommandCall parse( String text )
    {

        if( S.isNullOrEmpty(text) )
        {
            throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                    .param("reason", "Command call is empty")
                    .param("raw", text);
        }

        final String raw = text;

        String s = text.trim();

        if (s.startsWith("`") || s.endsWith("`")) {
            if (!(s.startsWith("`") && s.endsWith("`") && s.length() >= 2)) {
                throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                        .param("reason", "Invalid command quote")
                        .param("raw", raw);
            }

            s = s.substring(1, s.length() - 1).trim();
        }

        if( s.length() == 0) {
            throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                    .param("reason", "Command name is empty")
                    .param("raw", raw);
        }

        String[] parts = s.split(",");

        String name = parts[0].trim();

        if (name.length() == 0) {
            throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                    .param("reason", "Command name is empty")
                    .param("raw", raw);
        }

        List<String> args = new ArrayList<String>();

        for (int i = 1; i < parts.length; i++) {
            String arg = parts[i].trim();

            if (arg.length() == 0) {
                throw F2Errors.of(F2Errors.ErrorCode.COMMAND_CALL_INVALID)
                        .param("reason", "Empty command argument")
                        .param("raw", raw)
                        .param("index", Integer.valueOf(i - 1));
            }

            args.add(arg);
        }

        return new F2CommandCall(name, args, raw);
    }
}