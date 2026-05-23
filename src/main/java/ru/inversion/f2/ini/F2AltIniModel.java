package ru.inversion.f2.ini;

import java.util.Map;

/**
    Абстрактный low-level доступ к секциям ALTPRNT5.INI.
    Только raw string values.
    Не парсит команды.
    Не компилирует style/raw programs.
*/
public interface F2AltIniModel {

    Map<String, String> commands();

    Map<String, String> codeText();

    Map<String, String> codeGraphics();

    Map<String, String> driverRef();

    String cleanCommandName(String name);

    String commandDescription(String name);

    String codeText(String name);

    String codeGraphics(String name);

    String driverRef(String name);

}