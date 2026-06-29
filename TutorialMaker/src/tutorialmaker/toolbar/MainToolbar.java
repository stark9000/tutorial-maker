package tutorialmaker.toolbar;

import tutorialmaker.canvas.CanvasPanel;
import tutorialmaker.io.ProjectSerializer;
import tutorialmaker.commands.UndoManager;
import tutorialmaker.tiles.*;
import tutorialmaker.annotation.AnnotationTool;
import tutorialmaker.annotation.DrawingLayer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;

public class MainToolbar extends JPanel {

    private CanvasPanel   canvas;
    private JLabel        zoomLabel;
    private JToggleButton gridBtn, borderBtn;
    private JPanel        drawRow;
    private JToggleButton drawModeBtn;
    private JToggleButton layerVisBtn;
    private ButtonGroup   toolGroup  = new ButtonGroup();
    private JButton       colorSwatch;
    private Color         drawColor  = new Color(220, 50, 50);
    private File          currentFile = null;
    private GridBagConstraints gbc;
    private Timer         zoomTimer;

    public MainToolbar(CanvasPanel canvas) {
        this.canvas = canvas;
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        buildRow1();
        buildRow2();
        buildDrawToggleRow();
        buildDrawToolRow();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (zoomTimer != null) zoomTimer.stop();
    }

    // ── ROW 1: File, Edit, Add ────────────────────────────────────────────────

    private void buildRow1() {
        JPanel row = row();

        addSep(row, "FILE");
        row.add(btn("New",      e -> newProject()));
        row.add(btn("💾 Save",  e -> saveProject()));
        row.add(btn("📂 Open",  e -> loadProject()));

        addDiv(row);
        addSep(row, "EDIT");
        JButton undoBtn = btn("↩ Undo", e -> canvas.undo());
        JButton redoBtn = btn("↪ Redo", e -> canvas.redo());
        undoBtn.setEnabled(false);
        redoBtn.setEnabled(false);
        row.add(undoBtn);
        row.add(redoBtn);
        canvas.getUndoManager().setChangeListener(() -> {
            UndoManager um = canvas.getUndoManager();
            undoBtn.setEnabled(um.canUndo());
            redoBtn.setEnabled(um.canRedo());
            undoBtn.setToolTipText(um.canUndo() ? "Undo: " + um.undoDescription() : "Nothing to undo");
            redoBtn.setToolTipText(um.canRedo() ? "Redo: " + um.redoDescription() : "Nothing to redo");
        });

        addDiv(row);
        addSep(row, "ADD");
        row.add(btn("🖼 Image",   e -> addImageTile()));
        row.add(btn("📝 Text",    e -> addTextTile()));
        row.add(btn("🏷 Label",   e -> addLabel(LabelTile.Style.CAPTION)));
        row.add(btn("💬 Callout", e -> addLabel(LabelTile.Style.CALLOUT)));
        row.add(btn("① Step",    e -> addLabel(LabelTile.Style.STEP_BADGE)));

        addRow(row);
    }

    // ── ROW 2: Canvas, Zoom, Page, Export ────────────────────────────────────

    private void buildRow2() {
        JPanel row = row();
        row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        addSep(row, "CANVAS");
        gridBtn   = toggle("⊞ Grid",    canvas.isSnapToGrid(),  e -> canvas.setSnapToGrid(gridBtn.isSelected()));
        borderBtn = toggle("▭ Borders", canvas.isShowBorders(), e -> canvas.setShowBorders(borderBtn.isSelected()));
        row.add(gridBtn);
        row.add(borderBtn);

        // Grid size spinner — only meaningful when grid is on, but always editable
        JLabel gridSzLabel = new JLabel("Grid:");
        gridSzLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        row.add(gridSzLabel);
        JSpinner gridSzSpinner = new JSpinner(new SpinnerNumberModel(canvas.getGridSize(), 5, 100, 5));
        gridSzSpinner.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gridSzSpinner.setPreferredSize(new Dimension(58, 24));
        gridSzSpinner.setToolTipText("Grid size in pixels (5–100)");
        gridSzSpinner.addChangeListener(e -> canvas.setGridSize((Integer) gridSzSpinner.getValue()));
        row.add(gridSzSpinner);

        addDiv(row);
        addSep(row, "ZOOM");
        row.add(btn("−", e -> canvas.setZoom(canvas.getZoom() - 0.1)));
        zoomLabel = new JLabel("100%");
        zoomLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        zoomLabel.setPreferredSize(new Dimension(48, 24));
        zoomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(zoomLabel);
        row.add(btn("+", e -> canvas.setZoom(canvas.getZoom() + 0.1)));
        row.add(btn("⟳", e -> canvas.setZoom(1.0)));
        zoomTimer = new Timer(200, e -> zoomLabel.setText(
                String.format("%.0f%%", canvas.getZoom() * 100)));
        zoomTimer.start();

        addDiv(row);
        addSep(row, "PAGE");
        JComboBox<String> pageCombo = new JComboBox<>(new String[]{
            "A4 Portrait", "A4 Landscape", "A3 Portrait", "A3 Landscape", "Letter"
        });
        pageCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pageCombo.setPreferredSize(new Dimension(130, 26));
        pageCombo.addActionListener(e -> applyPageSize((String) pageCombo.getSelectedItem()));
        row.add(pageCombo);

        addDiv(row);
        addSep(row, "EXPORT");
        row.add(btn("🖨 Print", e -> printPage()));
        row.add(btn("💾 PNG",   e -> exportPng()));

        addRow(row);
    }

    // ── ROW 3: Draw mode toggle (always visible) ───────────────────────────────

    private void buildDrawToggleRow() {
        JPanel row = row();
        row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        addSep(row, "DRAW LAYER");
        drawModeBtn = new JToggleButton("✏ Draw Mode");
        drawModeBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        drawModeBtn.addActionListener(e -> toggleDrawMode(drawModeBtn.isSelected()));
        row.add(drawModeBtn);

        addRow(row);
    }

    // ── ROW 4: Draw tools (shown only when draw mode on) ──────────────────────

    private void buildDrawToolRow() {
        drawRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        drawRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
        drawRow.setAlignmentX(0.0f);
        drawRow.setVisible(false);

        DrawingLayer dl = canvas.getDrawingLayer();

        addSep(drawRow, "TOOL");
        String[][]      labels   = {{"↖","Select"},{"➜","Arrow"},{"╱","Line"},
                                    {"▭","Rect"},  {"⬭","Oval"}, {"✏","Free"},
                                    {"📍","Pin"},  {"◌","Erase"}};
        AnnotationTool[] toolEnums = {
            AnnotationTool.SELECT,   AnnotationTool.ARROW, AnnotationTool.LINE,
            AnnotationTool.RECT,     AnnotationTool.OVAL,  AnnotationTool.FREEHAND,
            AnnotationTool.TEXT_PIN, AnnotationTool.ERASER
        };

        JToggleButton first = null;
        for (int i = 0; i < labels.length; i++) {
            final AnnotationTool tool = toolEnums[i];
            JToggleButton tb = new JToggleButton(labels[i][0] + " " + labels[i][1]);
            tb.setFont(new Font("SansSerif", Font.PLAIN, 11));
            tb.addActionListener(e -> { dl.setActiveTool(tool); dl.requestFocusInWindow(); });
            toolGroup.add(tb);
            drawRow.add(tb);
            if (first == null) { first = tb; tb.setSelected(true); }
        }

        addDiv(drawRow);
        addSep(drawRow, "COLOR");
        colorSwatch = new JButton();
        colorSwatch.setBackground(drawColor);
        colorSwatch.setOpaque(true);
        colorSwatch.setPreferredSize(new Dimension(30, 26));
        colorSwatch.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        colorSwatch.setToolTipText("Pick draw colour (also recolours selected shape)");
        colorSwatch.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Draw Colour", colorSwatch.getBackground());
            if (c != null) {
                drawColor = c;
                colorSwatch.setBackground(c);
                dl.setDrawColor(c);
                dl.setSelectedShapeColor(c);   // recolour selected shape if any
            }
        });
        drawRow.add(colorSwatch);
        // Sync swatch to selected shape's colour whenever selection changes
        dl.setSelectionChangeCallback(() ->
            colorSwatch.setBackground(dl.getActiveColor()));

        addDiv(drawRow);
        addSep(drawRow, "SIZE");
        JSpinner thickSpin = new JSpinner(new SpinnerNumberModel(3, 1, 30, 1));
        thickSpin.setFont(new Font("SansSerif", Font.PLAIN, 11));
        thickSpin.setPreferredSize(new Dimension(54, 26));
        thickSpin.addChangeListener(e -> dl.setDrawThick(((Integer) thickSpin.getValue()).floatValue()));
        drawRow.add(thickSpin);

        addDiv(drawRow);
        addSep(drawRow, "FILL");
        JToggleButton fillBtn = new JToggleButton("Filled", false);
        fillBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fillBtn.addActionListener(e -> dl.setFilled(fillBtn.isSelected()));
        drawRow.add(fillBtn);

        addDiv(drawRow);
        addSep(drawRow, "LAYER");
        layerVisBtn = new JToggleButton("👁 Visible", true);
        layerVisBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        layerVisBtn.addActionListener(e -> dl.setLayerVisible(layerVisBtn.isSelected()));
        drawRow.add(layerVisBtn);

        drawRow.add(btn("🗑 Clear", e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Clear all annotations?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) dl.clearAll();
        }));

        addRow(drawRow);
    }

    // ── DRAW MODE TOGGLE ──────────────────────────────────────────────────────

    private void toggleDrawMode(boolean on) {
        drawRow.setVisible(on);
        canvas.getDrawingLayer().setDrawMode(on);
        if (on) canvas.getDrawingLayer().setActiveTool(AnnotationTool.SELECT);
        revalidate();
        repaint();
    }

    // ── FILE OPS ──────────────────────────────────────────────────────────────

    private void newProject() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Start a new project? Unsaved changes will be lost.",
                "New Project", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) { canvas.clearAll(); currentFile = null; updateTitle(); }
    }

    private void saveProject() {
        if (currentFile == null) saveProjectAs(); else doSave(currentFile);
    }

    private void saveProjectAs() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Tutorial Maker Project (*.tmk)", "tmk"));
        if (currentFile != null) fc.setSelectedFile(currentFile);
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".tmk"))
            f = new File(f.getAbsolutePath() + ".tmk");
        doSave(f);
    }

    private void doSave(File f) {
        try {
            ProjectSerializer.save(canvas, f);
            currentFile = f; updateTitle();
            JOptionPane.showMessageDialog(this, "Saved: " + f.getName(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProject() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Tutorial Maker Project (*.tmk)", "tmk"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try {
            ProjectSerializer.load(canvas, f);
            currentFile = f; updateTitle();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTitle() {
        java.awt.Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JFrame)
            ((JFrame) w).setTitle("Tutorial Maker — " +
                    (currentFile != null ? currentFile.getName() : "Untitled"));
    }

    // ── TILE HELPERS ──────────────────────────────────────────────────────────

    private void addImageTile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images","png","jpg","jpeg","gif","bmp"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        ImageIcon icon = new ImageIcon(f.getAbsolutePath());
        int iw = icon.getIconWidth(), ih = icon.getIconHeight();
        double scale = Math.min(1.0, Math.min(360.0/iw, 280.0/ih));
        int tw = (int)(iw*scale), th = (int)(ih*scale);
        Point p = canvas.getViewportDocPoint(tw, th);
        ImageTile tile = new ImageTile(p.x, p.y, tw, th, icon, f.getAbsolutePath());
        canvas.addTile(tile);
        canvas.selectTile(tile);
    }

    private void addTextTile() {
        Point p = canvas.getViewportDocPoint(240, 100);
        canvas.addTile(new TextTile(p.x, p.y, 240, 100, ""));
    }

    private void addLabel(LabelTile.Style style) {
        String def = style == LabelTile.Style.STEP_BADGE
                ? String.valueOf(canvas.nextStepNumber()) : "Label text";
        int tw = style == LabelTile.Style.STEP_BADGE ? 48 : 180;
        int th = style == LabelTile.Style.STEP_BADGE ? 48 : 40;
        Point p = canvas.getViewportDocPoint(tw, th);
        canvas.addTile(new LabelTile(p.x, p.y, def, style));
    }

    private void applyPageSize(String name) {
        switch (name) {
            case "A4 Portrait":  canvas.setPageSize(794,  1123); break;
            case "A4 Landscape": canvas.setPageSize(1123, 794);  break;
            case "A3 Portrait":  canvas.setPageSize(1123, 1587); break;
            case "A3 Landscape": canvas.setPageSize(1587, 1123); break;
            case "Letter":       canvas.setPageSize(816,  1056); break;
        }
    }

    // ── PRINT / EXPORT ────────────────────────────────────────────────────────

    private void printPage() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((g, pf, pi) -> {
            if (pi > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(pf.getImageableX(), pf.getImageableY());
            canvas.printPage(g2, pf.getImageableWidth(), pf.getImageableHeight());
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage());
            }
        }
    }

    private void exportPng() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));
        fc.setSelectedFile(new File("tutorial.png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".png"))
            f = new File(f.getAbsolutePath() + ".png");
        try {
            BufferedImage img = canvas.exportPageImage();
            javax.imageio.ImageIO.write(img, "PNG", f);
            JOptionPane.showMessageDialog(this, "Saved: " + f.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
        }
    }

    // ── WIDGET FACTORIES ──────────────────────────────────────────────────────

    private JPanel row() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        p.setAlignmentX(0.0f);
        return p;
    }

    private JButton btn(String label, ActionListener al) {
        JButton b = new JButton(label);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        return b;
    }

    private JToggleButton toggle(String label, boolean init, ActionListener al) {
        JToggleButton b = new JToggleButton(label, init);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.addActionListener(al);
        return b;
    }

    private void addSep(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 9));
        l.setForeground(new Color(100, 100, 100));
        l.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 2));
        p.add(l);
    }

    private void addRow(JPanel p) {
        add(p, gbc);
    }

    private void addDiv(JPanel p) {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 28));
        s.setForeground(new Color(160, 160, 160));
        p.add(s);
    }
}