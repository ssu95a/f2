package ru.inversion.f2.fx;

import javafx.application.Application;

public final class F2FxPreviewManualSmokeLauncher {

    private F2FxPreviewManualSmokeLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(
                F2FxPreviewManualSmokeApp.class,
                args
        );
    }
}
