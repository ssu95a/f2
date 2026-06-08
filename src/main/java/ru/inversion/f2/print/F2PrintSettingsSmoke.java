package ru.inversion.f2.print;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Sides;

public final class F2PrintSettingsSmoke {

    public static void main(String[] args) {

        PrintRequestAttributeSet attrs =
                new HashPrintRequestAttributeSet();

        attrs.add(Sides.ONE_SIDED);
        attrs.add(new Copies(2));

        F2PrintSettings settings =
                new F2PrintSettings(null, attrs);

        PrintRequestAttributeSet copy1 =
                settings.attributesCopy();

        assertEquals(
                Sides.ONE_SIDED,
                copy1.get(Sides.class)
        );

        assertEquals(
                new Copies(2),
                copy1.get(Copies.class)
        );

        copy1.add(Sides.TWO_SIDED_LONG_EDGE);
        copy1.add(new Copies(5));

        PrintRequestAttributeSet copy2 =
                settings.attributesCopy();

        assertEquals(
                Sides.ONE_SIDED,
                copy2.get(Sides.class)
        );

        assertEquals(
                new Copies(2),
                copy2.get(Copies.class)
        );

        System.out.println("F2 print settings smoke OK");
    }

    private static void assertEquals(Object expected, Object actual) {

        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new IllegalStateException(
                    "Expected [" + expected + "], actual [" + actual + "]"
            );
        }
    }
}