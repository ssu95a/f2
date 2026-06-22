package ru.inversion.f2.print;

@FunctionalInterface
public interface F2PrintExecutor {

    F2PrintResult print(
            F2PrintJob printJob
    ) throws Exception;
}
