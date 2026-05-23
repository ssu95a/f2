package ru.inversion.f2.command;

import ru.inversion.f2.style.F2StyleProgram;
import ru.inversion.utils.Checks;
import ru.inversion.utils.S;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// named command: UNDER+=Under=Yes;
public final class F2CommandDef {

    private final String name;
    private final String description;
    private final List<F2CommandPropertyValue> properties;

    private final F2StyleProgram styleProgram;

    /** */
    public F2CommandDef( String name, String description, List<F2CommandPropertyValue> properties, F2StyleProgram styleProgram )
    {
        Checks.Require.text(name,"name");

        this.description  = description;
        this.name         = name.trim();

        if (properties == null)
            this.properties = Collections.emptyList();
        else
            this.properties = Collections.unmodifiableList(new ArrayList<>(properties));

        this.styleProgram = styleProgram == null ? F2StyleProgram.empty() : styleProgram;

    }

    public String name() {
        return name;
    }

    public List<F2CommandPropertyValue> properties() {
        return properties;
    }

    public F2StyleProgram styleProgram() {
        return styleProgram;
    }
}