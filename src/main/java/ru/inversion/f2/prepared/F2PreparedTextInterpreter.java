package ru.inversion.f2.prepared;

import ru.inversion.f2.command.F2CommandRef;
import ru.inversion.f2.command.F2CommandRegistry;
import ru.inversion.f2.control.F2ControlState;
import ru.inversion.f2.style.F2RenderState;
import ru.inversion.utils.Checks;
import ru.inversion.utils.S;

import java.util.ArrayList;
import java.util.List;

public final class F2PreparedTextInterpreter {

    public F2StyledDocument interpret (
            List<F2PreparedToken> tokens,
            F2CommandRegistry registry
    )
    {
        Checks.Require.object( registry, "registry" );

        final Context ctx = new Context();

        if( tokens != null )
        {
            for( F2PreparedToken token : tokens ) {

                if (token == null)
                    continue;

                processToken( token, registry, ctx );
            }
        }

        ctx.finishDocument();

        return new F2StyledDocument( ctx.pages );
    }

    private void processToken(
            F2PreparedToken token,
            F2CommandRegistry registry,
            Context ctx
    )
    {
        switch (token.type()) {
            case TEXT:
                processText(token, ctx);
                break;

            case COMMAND:
                processCommand(token, registry, ctx);
                break;

            case NEW_LINE:
                ctx.finishCurrentLine();
                break;

            default:
                throw new IllegalStateException( "Unsupported token type: " + token.type() );
        }
    }

    /** */
    private void processText( F2PreparedToken token, Context ctx )
    {
        final String text = token.text( );

        if(S.isNullOrEmpty(text))
            return;

        ctx.currentRuns.add( new F2StyledTextChunk( text, ctx.state.style() ));
    }

    /** */
    private void processCommand( F2PreparedToken token, F2CommandRegistry registry, Context ctx )
    {
        F2CommandRef ref = registry.resolve( token.commandCall() );

        ctx.control.clear();
        ctx.state = ref.def().styleProgram().apply( ref.call(), ctx.state, registry, ctx.control );

        handleControl(ctx);
    }

    private void handleControl(Context ctx) {

        if( !ctx.control.hasSignals() )
            return;

        for( int i = 0; i < ctx.control.lineFeedCount(); i++ )
             ctx.finishCurrentLine();

        if( ctx.control.pageEndRequested() )
        {
            ctx.finishCurrentLine();
            ctx.finishCurrentPage();
        }

        ctx.control.clear();
    }

    private static final class Context {

        private final List<F2StyledPage> pages =
                new ArrayList<F2StyledPage>();

        private List<F2StyledLine> currentPageLines =
                new ArrayList<F2StyledLine>();

        private List<F2StyledTextChunk> currentRuns =
                new ArrayList<F2StyledTextChunk>();

        private F2RenderState state =
                F2RenderState.initial();

        private final F2ControlState control =
                new F2ControlState();

        private void finishCurrentLine() {
            currentPageLines.add(new F2StyledLine(
                    currentRuns,
                    state.lineStepPt(),
                    state.leftIndentPt()
            ));

            currentRuns = new ArrayList<F2StyledTextChunk>();
        }

        private void finishCurrentPage() {
            pages.add(new F2StyledPage(currentPageLines));
            currentPageLines = new ArrayList<F2StyledLine>();
        }

        private void finishDocument() {
            finishCurrentLine();
            finishCurrentPage();
        }
    }
}