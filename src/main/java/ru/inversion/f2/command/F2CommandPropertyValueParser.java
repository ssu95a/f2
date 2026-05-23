package ru.inversion.f2.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inversion.utils.S;
import ru.inversion.utils.converter.TypeConverter;

import javax.print.attribute.standard.OrientationRequested;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class F2CommandPropertyValueParser {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** */
    public List<F2CommandPropertyValue> parse( String value )
    {
        final List<F2CommandPropertyValue> result = new ArrayList<>();

        if( S.isNullOrEmpty(value) )
            return result;

        String[] parts = value.split(";", -1);

        for( String part : parts )
        {
            String item = part.trim();

            if( item.isEmpty() )
                continue;

            int eq = item.indexOf('=');

            if( eq <= 0 )
            {
                result.add( new F2CommandPropertyValue( null, item, null, null ) );
                continue;
            }

            String rawName  = item.substring(0, eq).trim();
            String rawValue = item.substring(eq + 1).trim();

            F2CommandProperty property = F2CommandProperty.fromIniName(rawName);

            Object typedValue = parseTypedValue(property, rawValue);

            result.add( new F2CommandPropertyValue( property, rawName, rawValue, typedValue ) );
        }

        return result;
    }

    /** */
    private Object parseTypedValue(F2CommandProperty property, String rawValue) {

        if (property == null)
            return rawValue;

        Class<?> type = property.valueType();

        if (type == F2CommandCall.class)
            return F2CommandCallParser.parse(rawValue);

        if( type == OrientationRequested.class )
            return parseOrientation(rawValue);

        if( type == Double.class )
            return Double.valueOf(rawValue);

        return TypeConverter.convert(rawValue, type);
    }


    /** */
    private static OrientationRequested parseOrientation(String value) {

        if( value == null )
            return OrientationRequested.PORTRAIT;

        String s = value.trim()
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ENGLISH);

        switch (s) {
            case "LANDSCAPE":
            case "L":
                return OrientationRequested.LANDSCAPE;
            case "REVERSE LANDSCAPE":
            case "RL":
                return OrientationRequested.REVERSE_LANDSCAPE;
            case "REVERSE PORTRAIT":
            case "RP":
                return OrientationRequested.REVERSE_PORTRAIT;
        }

        return OrientationRequested.PORTRAIT;
    }
}