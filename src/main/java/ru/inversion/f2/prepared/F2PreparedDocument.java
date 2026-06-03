package ru.inversion.f2.prepared;

import ru.inversion.utils.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class F2PreparedDocument {

    private final List<F2PreparedToken> tokens;
    private final F2PreparedContentMode contentMode;

    public F2PreparedDocument( List<F2PreparedToken> tokens, F2PreparedContentMode contentMode )
    {
        this.tokens = tokens == null || tokens.isEmpty()
                ? Collections.<F2PreparedToken>emptyList()
                : Collections.unmodifiableList(
                new ArrayList<F2PreparedToken>(tokens)
        );

        this.contentMode = Checks.Require.object(contentMode, "contentMode");
    }
    /* **/
    public List<F2PreparedToken> tokens() {
        return tokens;
    }

    public F2PreparedContentMode contentMode() {
        return contentMode;
    }
}