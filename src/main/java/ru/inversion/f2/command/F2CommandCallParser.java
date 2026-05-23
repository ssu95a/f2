package ru.inversion.f2.command;

import java.util.ArrayList;
import java.util.List;

public final class F2CommandCallParser {

    private F2CommandCallParser() {
    }

    public static F2CommandCall parse(String text) {
        if (text == null)
            throw new IllegalArgumentException("Command call is null");

        String raw = text;
        String s = text.trim();

        if (s.length() == 0)
            throw new IllegalArgumentException("Command call is empty");

        /*
         * Cmd=`PAGE_END`
         * prepared text: `UNDER+`
         */
        if (s.startsWith("`") && s.endsWith("`") && s.length() >= 2)
            s = s.substring(1, s.length() - 1).trim();

        if (s.length() == 0)
            throw new IllegalArgumentException("Command call is empty: " + raw);

        List<String> parts = splitArgs(s);

        String name = parts.get(0);

        if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("Command name is empty: " + raw);

        List<String> args = new ArrayList<String>();

        for (int i = 1; i < parts.size(); i++)
            args.add(parts.get(i));

        return new F2CommandCall(name, args, raw);
    }

    private static List<String> splitArgs(String text) {
        List<String> result = new ArrayList<String>();
        StringBuilder current = new StringBuilder();

        boolean inDoubleQuotes = false;
        boolean inSingleQuotes = false;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
                current.append(ch);
                continue;
            }

            if (ch == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
                current.append(ch);
                continue;
            }

            if (ch == ',' && !inDoubleQuotes && !inSingleQuotes) {
                result.add(unquote(current.toString().trim()));
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        result.add(unquote(current.toString().trim()));

        return result;
    }

    private static String unquote(String value) {
        if (value == null)
            return null;

        String s = value.trim();

        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);

            if ((first == '"' && last == '"')
                    || (first == '\'' && last == '\'')) {
                return s.substring(1, s.length() - 1);
            }
        }

        return s;
    }
}