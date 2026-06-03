package ru.inversion.f2.prepared;

import ru.inversion.f2.error.F2Exception;
import ru.inversion.f2.prepared.F2PreparedTextParser;
import ru.inversion.f2.prepared.F2PreparedToken;

import java.util.List;

public final class F2PreparedTextParserSmoke {

    public static void main(String[] args) {

        smokeSimple();
        //smokeNewLine();
        smokeUnclosedCommand();
        smokeEmptyCommand();
        smokeCommandCrossesLine();

        System.out.println("F2 prepared text parser smoke OK");
    }

    private static void smokeSimple() {
        List<F2PreparedToken> tokens = new F2PreparedTextParser()
                .parse("Ф.И.О. `UNDER+`     `UNDER-`|");

        assertEquals(Integer.valueOf(5), Integer.valueOf(tokens.size()));

        assertEquals(F2PreparedToken.Type.TEXT, tokens.get(0).type());
        assertEquals("Ф.И.О. ", tokens.get(0).text());

        assertEquals(F2PreparedToken.Type.COMMAND, tokens.get(1).type());
        assertEquals("UNDER+", tokens.get(1).commandCall().name());

        assertEquals(F2PreparedToken.Type.TEXT, tokens.get(2).type());
        assertEquals("     ", tokens.get(2).text());

        assertEquals(F2PreparedToken.Type.COMMAND, tokens.get(3).type());
        assertEquals("UNDER-", tokens.get(3).commandCall().name());

        assertEquals(F2PreparedToken.Type.TEXT, tokens.get(4).type());
        assertEquals("|", tokens.get(4).text());

        System.out.println("simple prepared text OK");
    }

    private static void smokeNewLine() {
        List<F2PreparedToken> tokens = new F2PreparedTextParser()
                .parse("A\r\nB\nC\rD");

        int newLines = 0;

        for (F2PreparedToken token : tokens) {
            if (token.type() == F2PreparedToken.Type.NEW_LINE)
                newLines++;
        }

        assertEquals(Integer.valueOf(3), Integer.valueOf(newLines));

        System.out.println("new lines OK");
    }

    private static void smokeUnclosedCommand() {
        try {
            new F2PreparedTextParser().parse("A `UNDER+ B");
            throw new IllegalStateException("Unclosed command was accepted");
        }
        catch (F2Exception expected) {
            System.out.println("unclosed command OK");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new IllegalStateException(
                    "Expected [" + expected + "], actual [" + actual + "]"
            );
        }
    }

    private static void smokeUnclosedWithText() {
        try {
            new F2PreparedTextParser().parse("A `UNDER+");
            throw new IllegalStateException("Unclosed command was accepted");
        }
        catch (F2Exception expected) {
            System.out.println("unclosed command with text OK");
        }
    }

    private static void smokeEmptyCommand() {
        try {
            new F2PreparedTextParser().parse("A `` B");
            throw new IllegalStateException("Empty command was accepted");
        }
        catch (F2Exception expected) {
            System.out.println("empty command OK");
        }
    }

    private static void smokeCommandCrossesLine() {
        try {
            new F2PreparedTextParser().parse("A `UNDER+\nB`");
            throw new IllegalStateException("Multiline command was accepted");
        }
        catch (F2Exception expected) {
            System.out.println("multiline command rejected OK");
        }
    }


}