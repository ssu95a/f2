package ru.inversion.f2.fx;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import ru.inversion.f2.awt.F2AwtDocumentPaginator;
import ru.inversion.f2.awt.F2AwtPreviewRenderer;
import ru.inversion.f2.prepared.F2StyledDocument;
import ru.inversion.f2.print.F2PageLayout;
import ru.inversion.f2.print.F2PrintPageSetup;
import ru.inversion.utils.Checks;

import java.awt.image.BufferedImage;

public final class F2FxPreviewPane extends BorderPane {

    private static final double DEFAULT_DPI = 144.0d;

    private final F2AwtDocumentPaginator paginator =
            new F2AwtDocumentPaginator();
    private final F2AwtPreviewRenderer renderer = new F2AwtPreviewRenderer();
    private final ImageView imageView = new ImageView();
    private final ScrollPane scrollPane = new ScrollPane(imageView);

    private F2StyledDocument document;
    private F2PageLayout pageLayout;
    private F2PrintPageSetup pageSetup;
    private int pageIndex;
    private double dpi = DEFAULT_DPI;
    private boolean debugOverlay;

    public F2FxPreviewPane() {
        configureView();
    }

    public F2FxPreviewPane(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup
    ) {
        this();
        setPreview(document, pageSetup);
    }

    public void setPreview(
            F2StyledDocument document,
            F2PrintPageSetup pageSetup
    ) {
        this.document = Checks.Require.object(document, "document");
        this.pageSetup = Checks.Require.object(pageSetup, "pageSetup");
        this.pageLayout = paginator.layout(document, pageSetup);
        this.pageIndex = 0;
        renderCurrentPage();
    }

    public void setPageSetup(F2PrintPageSetup pageSetup) {
        this.pageSetup = Checks.Require.object(pageSetup, "pageSetup");

        if (document != null) {
            this.pageLayout = paginator.layout(document, pageSetup);
            this.pageIndex = Math.min(
                    pageIndex,
                    Math.max(0, pageLayout.pageCount() - 1)
            );
        }

        if (isPreviewReady())
            renderCurrentPage();
    }

    public void setPageIndex(int pageIndex) {
        ensureDocumentReady();
        checkPageIndex(pageIndex);
        this.pageIndex = pageIndex;
        renderCurrentPage();
    }

    public void nextPage() {
        ensureDocumentReady();

        if (pageIndex + 1 < pageLayout.pageCount())
            setPageIndex(pageIndex + 1);
    }

    public void previousPage() {
        ensureDocumentReady();

        if (pageIndex > 0)
            setPageIndex(pageIndex - 1);
    }

    public void setDpi(double dpi) {
        if (dpi <= 0.0d)
            throw new IllegalArgumentException("dpi must be positive");

        this.dpi = dpi;

        if (isPreviewReady())
            renderCurrentPage();
    }

    public void setDebugOverlay(boolean debugOverlay) {
        this.debugOverlay = debugOverlay;

        if (isPreviewReady())
            renderCurrentPage();
    }

    /** Логический документ до привязки к printable area. */
    public F2StyledDocument document() {
        return document;
    }

    /** Физическая раскладка для текущего принтера. */
    public F2PageLayout pageLayout() {
        return pageLayout;
    }

    public F2PrintPageSetup pageSetup() {
        return pageSetup;
    }

    public int pageIndex() {
        return pageIndex;
    }

    public int pageNumber() {
        return pageIndex + 1;
    }

    public int pageCount() {
        return pageLayout == null ? 0 : pageLayout.pageCount();
    }

    public ImageView imageView() {
        return imageView;
    }

    public ScrollPane scrollPane() {
        return scrollPane;
    }

    private void configureView() {
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);

        setCenter(scrollPane);
    }

    private void renderCurrentPage() {
        ensureDocumentReady();
        checkPageIndex(pageIndex);

        BufferedImage image = renderer.render(
                pageLayout.page(pageIndex),
                pageSetup,
                dpi,
                debugOverlay
        );

        imageView.setImage(
                SwingFXUtils.toFXImage(image, null)
        );
    }

    private boolean isPreviewReady() {
        return pageLayout != null && pageSetup != null;
    }

    private void ensureDocumentReady() {
        if (document == null)
            throw new IllegalStateException("document is not set");

        if (pageSetup == null)
            throw new IllegalStateException("pageSetup is not set");

        if (pageLayout == null || pageLayout.isEmpty())
            throw new IllegalStateException("document has no pages");
    }

    private void checkPageIndex(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pageLayout.pageCount())
            throw new IllegalArgumentException("pageIndex is out of range: " + pageIndex);
    }
}
