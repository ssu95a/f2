package ru.inversion.f2.diagnostic;

import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledLine;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.prepared.F2StyledTextRun;
import ru.inversion.f2.style.F2Style;

public final class F2StyledDocumentDumper {

    private F2StyledDocumentDumper() {
    }

    public static String dump(F2StyledDocument document) {
        StringBuilder sb = new StringBuilder(4096);

        if (document == null) {
            sb.append("<null document>\n");
            return sb.toString();
        }

        sb.append("F2StyledDocument\n");
        sb.append("pages=").append(document.pageCount()).append('\n');

        for (int p = 0; p < document.pages().size(); p++) {
            F2StyledPage page = document.pages().get(p);

            sb.append('\n');
            sb.append("PAGE ").append(p + 1)
                    .append(" lines=").append(page.lineCount())
                    .append('\n');

            dumpPage(sb, page);
        }

        return sb.toString();
    }

    private static void dumpPage(StringBuilder sb, F2StyledPage page) {
        for (int i = 0; i < page.lines().size(); i++) {
            F2StyledLine line = page.lines().get(i);

            sb.append("  LINE ").append(i + 1)
                    .append(" stepPt=").append(format(line.lineStepPt()))
                    .append(" leftPt=").append(format(line.leftIndentPt()))
                    .append(" text=\"").append(escape(line.plainText())).append('"')
                    .append('\n');

            dumpLineRuns(sb, line);
        }
    }

    private static void dumpLineRuns(StringBuilder sb, F2StyledLine line) {
        for (int r = 0; r < line.runs().size(); r++) {
            F2StyledTextRun run = line.runs().get(r);
            F2Style style = run.style();

            sb.append("    RUN ").append(r + 1)
                    .append(" text=\"").append(escape(run.text())).append('"');

            if (style != null) {
                sb.append(" font=").append(style.fontName())
                        .append(" size=").append(style.fontSize())
                        .append(" bold=").append(style.bold())
                        .append(" italic=").append(style.italic())
                        .append(" underline=").append(style.underline());
            }
            else {
                sb.append(" style=<null>");
            }

            sb.append('\n');
        }
    }

    private static String escape(String value) {
        if (value == null)
            return "";

        return value
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ENGLISH, "%.2f", value);
    }
}