package ru.inversion.f2.print;

import org.junit.Test;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledPage;

import javax.print.PrintService;
import java.awt.print.PageFormat;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class F2PrintJobLifecycleTest {

    @Test
    public void listenerCopyKeepsCopiesAndCancellation() {
        AtomicInteger reads = new AtomicInteger();
        F2PrintJob source = newJob(() -> {
            reads.incrementAndGet();
            return 3;
        });
        F2PrintListener listener = new F2PrintListener() {};

        F2PrintJob copy = F2PrintJobs.withListener(source, listener);
        F2PrintJob resolved = copy.resolveCopies();

        assertSame(listener, copy.listener());
        assertEquals(3, resolved.resolvedCopies());
        assertEquals(1, reads.get());
        assertSame(resolved, resolved.resolveCopies());

        source.cancel();
        assertTrue(copy.isCancelled());
        assertTrue(resolved.isCancelled());
    }

    private static F2PrintJob newJob(java.util.function.IntSupplier copies) {
        PrintService service = mock(PrintService.class);
        when(service.getName()).thenReturn("Test printer");
        F2PrintPageSetup setup = F2PrintPageSetup.builder()
                .printService(service)
                .pageFormat(new PageFormat())
                .build();
        F2StyledDocument document = new F2StyledDocument(
                Collections.singletonList(
                        new F2StyledPage(Collections.emptyList())
                )
        );
        return new F2PrintJob(
                document, setup, "test-driver", copies, F2PrintListener.NONE
        );
    }
}
