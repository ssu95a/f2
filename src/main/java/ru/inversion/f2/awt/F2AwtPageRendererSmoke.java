package ru.inversion.f2.awt;

import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.ini.F2AltIniModel;
import ru.inversion.f2.ini.F2MapAltIniModel;
import ru.inversion.f2.prepared.F2PreparedTextInterpreter;
import ru.inversion.f2.prepared.F2PreparedTextParser;
import ru.inversion.f2.prepared.F2PreparedToken;
import ru.inversion.f2.prepared.F2StyledDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class F2AwtPageRendererSmoke {

    public static void main(String[] args) throws Exception {

        F2CommandRegistry registry = F2CommandRegistry.from(createModel());

        String text =
                "`NORMAL`Получатель платежа: ОАО `BOLD+`ТЕПЛО-ЭНЕРГЕТИК`BOLD-`\n"
                        + "Ф.И.О. `UNDER+`              `UNDER-`|\n"
                        + "Сумма платежа `UNDER+`12-00`UNDER-`\n"
                        + "`FF`"
                        + "`NORMAL`Квитанция\n"
                        + "Подпись `UNDER+`          `UNDER-`";

        List<F2PreparedToken> tokens =
                new F2PreparedTextParser().parse(text);

        F2StyledDocument doc =
                new F2PreparedTextInterpreter().interpret(tokens, registry);

        F2AwtPageRenderConfig config =
                F2AwtPageRenderConfig.a4Portrait()
                        .withDpi(144.0d)
                        .withDebugOverlay(true);

        F2AwtPageRenderer renderer =
                new F2AwtPageRenderer();

        for (int i = 0; i < doc.pages().size(); i++) {
            BufferedImage image = renderer.render(
                    doc.pages().get(i),
                    config
            );

            Path out = Paths.get(
                    "d:\\Java\\Projects\\f2\\target\\f2-page-" + (i + 1) + ".png"
            );

            ImageIO.write(image, "png", out.toFile());

            System.out.println("written: " + out);
        }

        System.out.println("F2 AWT page renderer smoke OK");
    }

    private static F2AltIniModel createModel() {

        Map<String, String> graphics = new LinkedHashMap<String, String>();

        graphics.put("UNDER+", "Under=Yes;");
        graphics.put("UNDER-", "Under=No;");

        graphics.put("BOLD+", "Bold=Yes;");
        graphics.put("BOLD-", "Bold=No;");

        graphics.put("INTERVAL_6", "Vertical Move=1/6;");

        graphics.put(
                "NORMAL",
                "Name Font=Courier New;Size Font=10;Bold=No;Italic=No;Under=No;Cmd=`INTERVAL_6`;"
        );

        graphics.put("PAGE_END", "Page End=Yes;");
        graphics.put("FF", "Cmd=`PAGE_END`;");

        return new F2MapAltIniModel(
                Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(),
                graphics,
                Collections.<String, String>emptyMap()
        );
    }
}