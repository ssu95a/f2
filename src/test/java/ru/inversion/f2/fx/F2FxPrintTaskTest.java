package ru.inversion.f2.fx;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.prepared.F2StyledPage;
import ru.inversion.f2.print.*;

import javax.print.PrintService;
import java.awt.print.PageFormat;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class F2FxPrintTaskTest {
    private static final long T = 10L;

    @BeforeClass
    public static void fx() throws Exception {
        CountDownLatch l = new CountDownLatch(1);
        try { Platform.startup(l::countDown); }
        catch (IllegalStateException ex) { Platform.runLater(l::countDown); }
        assertTrue(l.await(T, TimeUnit.SECONDS));
    }

    @Test
    public void success() throws Exception {
        F2FxPrintTask task = new F2FxPrintTask(job -> {
            F2PrintJob j = job.resolveCopies();
            F2PrintListener x = j.listener();
            x.onCopiesResolved(j, j.resolvedCopies());
            x.onBeginPrint(j);
            x.onPagePrinted(j, 0);
            x.onEndPrint(j);
            x.onFinalPrint(j, null);
            return new F2PrintResult(j.printerName(), j.driverRef(), 1);
        }, job());

        assertEquals(Worker.State.SUCCEEDED, run(task));
        assertEquals(1.0d, task.getProgress(), 0.000001d);
        assertEquals(1, task.getValue().pageCount());
    }

    @Test
    public void cancel() throws Exception {
        F2PrintJob source = job();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        F2FxPrintTask task = new F2FxPrintTask(job -> {
            entered.countDown();
            release.await(T, TimeUnit.SECONDS);
            return new F2PrintResult(job.printerName(), job.driverRef(), 1);
        }, source);

        AtomicReference<Worker.State> state = new AtomicReference<>();
        CountDownLatch done = start(task, state);
        assertTrue(entered.await(T, TimeUnit.SECONDS));
        task.cancelPrint();
        release.countDown();
        assertTrue(done.await(T, TimeUnit.SECONDS));
        assertEquals(Worker.State.CANCELLED, state.get());
        assertTrue(source.isCancelled());
    }

    private static Worker.State run(F2FxPrintTask task) throws Exception {
        AtomicReference<Worker.State> state = new AtomicReference<>();
        CountDownLatch done = start(task, state);
        assertTrue(done.await(T, TimeUnit.SECONDS));
        return state.get();
    }

    private static CountDownLatch start(
            F2FxPrintTask task,
            AtomicReference<Worker.State> state
    ) {
        CountDownLatch done = new CountDownLatch(1);
        task.stateProperty().addListener((o, a, b) -> {
            if (b == Worker.State.SUCCEEDED || b == Worker.State.FAILED
                    || b == Worker.State.CANCELLED) {
                state.set(b);
                done.countDown();
            }
        });
        Thread thread = new Thread(task, "f2-print-test");
        thread.setDaemon(true);
        thread.start();
        return done;
    }

    private static F2PrintJob job() {
        PrintService service = mock(PrintService.class);
        when(service.getName()).thenReturn("Test printer");
        F2PrintPageSetup setup = F2PrintPageSetup.builder()
                .printService(service)
                .pageFormat(new PageFormat())
                .build();
        F2StyledDocument doc = new F2StyledDocument(
                Collections.singletonList(
                        new F2StyledPage(Collections.emptyList())
                )
        );
        return new F2PrintJob(doc, setup, "driver", () -> 2, F2PrintListener.NONE);
    }
}
