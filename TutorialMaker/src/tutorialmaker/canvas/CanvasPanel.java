package tutorialmaker.canvas;

import tutorialmaker.tiles.TileComponent;
import tutorialmaker.commands.*;
import tutorialmaker.tiles.LabelTile;
import tutorialmaker.annotation.DrawingLayer;
import tutorialmaker.annotation.AnnotationTool;
import tutorialmaker.properties.PropertiesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * COORDINATE MODEL (fixed):
 *
 * Doc coords — tile position relative to the TOP-LEFT OF THE PAGE (not the
 * canvas). docX=0,docY=0 means top-left corner of the white page. These are
 * zoom-independent and what you'd save to disk.
 *
 * Screen coords — where the Swing component actually sits on the canvas panel.
 * screenX = PAGE_MARGIN*zoom + docX*zoom screenY = PAGE_MARGIN*zoom + docY*zoom
 *
 * The old bug was that PAGE_MARGIN was baked into docX/docY, so when zooming
 * the margin got scaled too — causing the anchor-point jump on drag.
 */
public class CanvasPanel extends JPanel {

    public static final int PAGE_W = 794;
    public static final int PAGE_H = 1123;
    public static final int PAGE_MARGIN = 60;

    private double zoom = 1.0;
    private boolean snapToGrid = false;
    private boolean showBorders = true;
    private int gridSize = 20;   // mutable — replaces old GRID_SIZE constant

    private int pageW = PAGE_W;
    private int pageH = PAGE_H;

    private JScrollPane scrollPane;   // set after construction so we can read the viewport

    private TileComponent selectedTile = null;
    private TileComponent clipboard     = null;
    private int           pasteCount   = 0;   // increments per paste; resets on new copy/cut
    private UndoManager   undoManager  = new UndoManager();
    private List<TileComponent> tiles = new ArrayList<>();
    private JLabel statusBar;
    private PropertiesPanel propsPanel;   // ← was missing
    private DrawingLayer      drawingLayer;

    public CanvasPanel() {
        this(null);
    }

    public CanvasPanel(PropertiesPanel propsPanel) {
        this.propsPanel = propsPanel;
        setLayout(null);
        setBackground(new Color(60, 60, 60));
        drawingLayer = new DrawingLayer();
        add(drawingLayer);
        updatePreferredSize();
        setupKeyBindings();
        positionDrawingLayer();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectTile(null);
                requestFocusInWindow();
            }
        });

        addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                double delta = e.getWheelRotation() < 0 ? 0.1 : -0.1;
                setZoom(zoom + delta);
            }
        });
    }

    // ── KEY BINDINGS ──────────────────────────────────────────────────────────
    private void setupKeyBindings() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (selectedTile == null) {
                    return;
                }
                int step = snapToGrid ? gridSize : 1;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_BACK_SPACE:
                        removeTile(selectedTile);
                        break;
                    case KeyEvent.VK_LEFT:
                        nudgeTile(selectedTile, -step, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        nudgeTile(selectedTile, step, 0);
                        break;
                    case KeyEvent.VK_UP:
                        nudgeTile(selectedTile, 0, -step);
                        break;
                    case KeyEvent.VK_DOWN:
                        nudgeTile(selectedTile, 0, step);
                        break;
                    case KeyEvent.VK_D:
                        if (e.isControlDown()) {
                            duplicateTile(selectedTile);
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        selectTile(null);
                        break;
                    case KeyEvent.VK_C:
                        if (e.isControlDown()) copySelected();
                        break;
                    case KeyEvent.VK_V:
                        if (e.isControlDown()) pasteClipboard();
                        break;
                    case KeyEvent.VK_X:
                        if (e.isControlDown()) cutSelected();
                        break;
                }
            }
        });
    }

    // ── TILE MANAGEMENT ───────────────────────────────────────────────────────
    /** Called by toolbar — goes through undo system. */
    public void addTile(TileComponent tile) {
        undoManager.execute(new AddTileCommand(this, tile));
        selectTile(tile);
    }

    /** Called directly by undo/redo system — no undo entry created. */
    public void addTileDirect(TileComponent tile) {
        tiles.add(tile);
        add(tile);
        tile.setCanvas(this);
        tile.applyBorderStyle(showBorders);
        applyZoomToTile(tile);
        if (tile instanceof LabelTile &&
            ((LabelTile)tile).getLabelStyle()==LabelTile.Style.STEP_BADGE) {
            setComponentZOrder(tile, 0);
        }
        if (drawingLayer!=null && drawingLayer.isDrawModeOn()) setComponentZOrder(drawingLayer, 0);
        revalidate(); repaint(); updateStatus();
    }

    /** Called by keyboard/menu — goes through undo system. */
    public void removeTile(TileComponent tile) {
        undoManager.execute(new DeleteTileCommand(this, tile));
        if (selectedTile == tile) selectTile(null);
    }

    /** Called directly by undo/redo system — no undo entry created. */
    public void removeTileDirect(TileComponent tile) {
        tiles.remove(tile);
        remove(tile);
        if (selectedTile == tile) selectedTile = null;
        repaint(); updateStatus();
    }

    public void duplicateTile(TileComponent tile) {
        TileComponent copy = tile.duplicate();
        addTile(copy);
        selectTile(copy);
    }

    public void selectTile(TileComponent tile) {
        if (selectedTile != null) {
            selectedTile.setSelected(false);
        }
        selectedTile = tile;
        if (selectedTile != null) {
            selectedTile.setSelected(true);
            setComponentZOrder(selectedTile, 0);
        }
        repaint();
        updateStatus();
        if (propsPanel != null) {
            if (selectedTile != null) {
                propsPanel.showTile(selectedTile);
            } else {
                propsPanel.showEmpty();
            }
        }
    }

    private void nudgeTile(TileComponent tile, int dx, int dy) {
        int oldX = tile.getDocX();
        int oldY = tile.getDocY();
        int newX = snap(oldX + dx);
        int newY = snap(oldY + dy);
        if (newX == oldX && newY == oldY) return;
        tile.setDocX(newX);
        tile.setDocY(newY);
        applyZoomToTile(tile);
        repaint();
        updateStatus();
        // Register as undoable — skip execute() since the move already happened
        undoManager.execute(new tutorialmaker.commands.MoveTileCommand(
                this, tile, oldX, oldY, newX, newY) {
            @Override public void execute() {} // already applied above
        });
    }

    // ── ZOOM ──────────────────────────────────────────────────────────────────
    public void setZoom(double z) {
        this.zoom = Math.max(0.25, Math.min(3.0, z));
        if (drawingLayer != null) drawingLayer.setZoom(this.zoom);
        for (TileComponent t : tiles) {
            applyZoomToTile(t);
        }
        updatePreferredSize();
        positionDrawingLayer();
        revalidate();
        repaint();
        updateStatus();
    }

    public void positionDrawingLayer() {
        if (drawingLayer==null) return;
        int marginPx=(int)Math.round(PAGE_MARGIN*zoom);
        int pw=(int)Math.round(pageW*zoom);
        int ph=(int)Math.round(pageH*zoom);
        drawingLayer.setBounds(marginPx,marginPx,pw,ph);
        // Only force to top when draw mode is active
        if (drawingLayer.isDrawModeOn()) setComponentZOrder(drawingLayer, 0);
    }

    /**
     * Converts a tile's doc coords → screen bounds and calls setBounds().
     *
     * screenX = margin*zoom + docX*zoom screenY = margin*zoom + docY*zoom
     *
     * This means zoom ONLY scales the doc offset from the page origin. The
     * margin pixels stay visually the same regardless of zoom, so the anchor
     * point never shifts.
     */
    public void applyZoomToTile(TileComponent t) {
        int marginPx = (int) Math.round(PAGE_MARGIN * zoom);
        int sx = marginPx + (int) Math.round(t.getDocX() * zoom);
        int sy = marginPx + (int) Math.round(t.getDocY() * zoom);
        int sw = (int) Math.round(t.getDocW() * zoom);
        int sh = (int) Math.round(t.getDocH() * zoom);
        t.setBounds(sx, sy, sw, sh);
    }

    /**
     * Converts a screen point (from mouse event on the canvas) back to doc
     * coords. Used by TileComponent for drag math.
     */
    public Point screenToDoc(Point screenPt) {
        int marginPx = (int) Math.round(PAGE_MARGIN * zoom);
        int dx = (int) Math.round((screenPt.x - marginPx) / zoom);
        int dy = (int) Math.round((screenPt.y - marginPx) / zoom);
        return new Point(dx, dy);
    }

    private void updatePreferredSize() {
        int w = (int) Math.round((pageW + PAGE_MARGIN * 2) * zoom);
        int h = (int) Math.round((pageH + PAGE_MARGIN * 2) * zoom);
        setPreferredSize(new Dimension(w, h));
    }

    // ── GRID / SNAP ───────────────────────────────────────────────────────────
    public int snap(int docValue) {
        if (!snapToGrid) {
            return docValue;
        }
        return (int) Math.round((double) docValue / gridSize) * gridSize;
    }

    // ── PAINTING ──────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int marginPx = (int) Math.round(PAGE_MARGIN * zoom);
        int pw = (int) Math.round(pageW * zoom);
        int ph = (int) Math.round(pageH * zoom);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(marginPx + 6, marginPx + 6, pw, ph, 4, 4);

        // White page
        g2.setColor(Color.WHITE);
        g2.fillRect(marginPx, marginPx, pw, ph);

        // Grid
        if (snapToGrid) {
            int gs = (int) Math.round(gridSize * zoom);
            if (gs >= 4) {
                g2.setColor(new Color(210, 210, 210));
                g2.setStroke(new BasicStroke(0.5f));
                for (int x = marginPx; x <= marginPx + pw; x += gs) {
                    g2.drawLine(x, marginPx, x, marginPx + ph);
                }
                for (int y = marginPx; y <= marginPx + ph; y += gs) {
                    g2.drawLine(marginPx, y, marginPx + pw, y);
                }
            }
        }

        // Page border
        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(marginPx, marginPx, pw, ph);

        g2.dispose();
    }

    // ── PRINT (page only, no background) ─────────────────────────────────────
    public void printPage(Graphics2D g2, double targetW, double targetH) {
        double scale = Math.min(targetW / pageW, targetH / pageH);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, (int) targetW, (int) targetH);

        Graphics2D pg = (Graphics2D) g2.create();
        pg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        pg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        pg.scale(scale, scale);

        for (TileComponent t : tiles) {
            Graphics2D tg = (Graphics2D) pg.create();
            tg.translate(t.getDocX(), t.getDocY());
            tg.clipRect(0, 0, t.getDocW(), t.getDocH());
            t.paintForPrint(tg, t.getDocW(), t.getDocH());
            tg.dispose();
        }
        // Draw annotation layer on top
        // Pass unscaled pageW/pageH — pg graphics is already scaled, so
        // fractional coords correctly map to full page dimensions
        if (drawingLayer!=null && drawingLayer.hasShapes())
            drawingLayer.paintForExport(pg, pageW, pageH);
        pg.dispose();
    }

    // ── PNG EXPORT ────────────────────────────────────────────────────────────
    public BufferedImage exportPageImage() {
        BufferedImage img = new BufferedImage(pageW, pageH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, pageW, pageH);

        for (TileComponent t : tiles) {
            Graphics2D tg = (Graphics2D) g2.create();
            tg.translate(t.getDocX(), t.getDocY());
            tg.clipRect(0, 0, t.getDocW(), t.getDocH());
            t.paintForPrint(tg, t.getDocW(), t.getDocH());
            tg.dispose();
        }
        if (drawingLayer!=null && drawingLayer.hasShapes())
            drawingLayer.paintForExport(g2, pageW, pageH);
        g2.dispose();
        return img;
    }

    // ── SETTERS / GETTERS ─────────────────────────────────────────────────────
    public void setScrollPane(JScrollPane sp) { this.scrollPane = sp; }

    /**
     * Returns the doc-coord point at the centre of the currently visible
     * viewport area, clamped so a tile spawned there fits on the page.
     * Falls back to (20, 20) if no scroll pane is set yet.
     */
    public Point getViewportDocPoint(int tileW, int tileH) {
        if (scrollPane == null) return new Point(20, 20);
        JViewport vp = scrollPane.getViewport();
        // Viewport position and size in screen/canvas pixels
        Point vpPos  = vp.getViewPosition();
        Dimension vpSz = vp.getExtentSize();
        // Centre of the visible area in canvas pixels
        int screenCx = vpPos.x + vpSz.width  / 2;
        int screenCy = vpPos.y + vpSz.height / 2;
        // Convert canvas pixels → doc coords
        int marginPx = (int) Math.round(PAGE_MARGIN * zoom);
        int docCx = (int) Math.round((screenCx - marginPx) / zoom);
        int docCy = (int) Math.round((screenCy - marginPx) / zoom);
        // Centre the tile on that point
        int x = docCx - tileW / 2;
        int y = docCy - tileH / 2;
        // Clamp so the tile stays inside the page
        x = Math.max(0, Math.min(pageW - tileW, x));
        y = Math.max(0, Math.min(pageH - tileH, y));
        return new Point(snap(x), snap(y));
    }

    public void setSnapToGrid(boolean snap) {
        this.snapToGrid = snap;
        repaint();
    }

    public void setShowBorders(boolean show) {
        this.showBorders = show;
        for (TileComponent t : tiles) {
            t.applyBorderStyle(show);
        }
        repaint();
    }

    public void setGridSize(int size) {
        this.gridSize = Math.max(5, Math.min(100, size));
        repaint();
    }

    public int getGridSize() { return gridSize; }

    public void setPageSize(int w, int h) {
        this.pageW = w; this.pageH = h;
        updatePreferredSize(); positionDrawingLayer(); repaint();
    }

    public void setStatusBar(JLabel bar) {
        this.statusBar = bar;
    }


    public int nextStepNumber() {
        int max=0;
        for (TileComponent t : tiles) {
            if (t instanceof LabelTile && ((LabelTile)t).getLabelStyle()==LabelTile.Style.STEP_BADGE)
                max=Math.max(max,((LabelTile)t).getStepNumber());
        }
        return max+1;
    }

    public DrawingLayer getDrawingLayer() { return drawingLayer; }


    // ── CLIPBOARD ─────────────────────────────────────────────────────────────

    public void copySelected() {
        if (selectedTile != null) { clipboard = selectedTile.duplicate(); pasteCount = 0; }
    }

    public void pasteClipboard() {
        if (clipboard == null) return;
        pasteCount++;
        TileComponent copy = clipboard.duplicate();
        // Each successive paste offsets by another 20 px from the *original* clipboard position
        copy.setDocX(copy.getDocX() + 20 * pasteCount);
        copy.setDocY(copy.getDocY() + 20 * pasteCount);
        addTile(copy);
        selectTile(copy);
        // clipboard itself is never mutated — repeated paste always diffs from original
    }

    public void cutSelected() {
        if (selectedTile == null) return;
        clipboard = selectedTile.duplicate();
        pasteCount = 0;
        removeTile(selectedTile);
    }


    // ── UNDO / REDO ───────────────────────────────────────────────────────────

    public void undo() { undoManager.undo(); }
    public void redo() { undoManager.redo(); }
    public UndoManager getUndoManager() { return undoManager; }

    /** Clear everything — used by load. */
    public void clearAll() {
        for (TileComponent t : new java.util.ArrayList<>(tiles)) removeTileDirect(t);
        tiles.clear();
        selectedTile = null;
        clipboard = null;
        undoManager.clear();
        if (drawingLayer != null) drawingLayer.clearAll();
        repaint(); updateStatus();
    }

    private void updateStatus() {
        if (statusBar == null) {
            return;
        }
        String sel = selectedTile != null
                ? "  |  " + selectedTile.getTileTypeName()
                + " @ (" + selectedTile.getDocX() + ", " + selectedTile.getDocY() + ")"
                + "  " + selectedTile.getDocW() + "×" + selectedTile.getDocH()
                : "";
        String grid = snapToGrid ? "  |  Grid ON" : "";
        statusBar.setText(String.format("  Tiles: %d  |  %.0f%%%s%s",
                tiles.size(), zoom * 100, grid, sel));
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public boolean isShowBorders() {
        return showBorders;
    }

    public double getZoom() {
        return zoom;
    }

    public int getPageMargin() {
        return PAGE_MARGIN;
    }

    public int getPageW() {
        return pageW;
    }

    public int getPageH() {
        return pageH;
    }

    public TileComponent getSelectedTile() {
        return selectedTile;
    }

    public List<TileComponent> getTiles() {
        return tiles;
    }
}
