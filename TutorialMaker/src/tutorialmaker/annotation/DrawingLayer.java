package tutorialmaker.annotation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Global transparent drawing layer over the canvas page.
 *
 * KEY DESIGN: This panel is ONLY visible (and interactive) when draw mode is
 * active. When draw mode is off it is hidden completely — so tiles underneath
 * receive all mouse events normally (drag, resize, select).
 *
 * Shapes use 0.0-1.0 fractional coords relative to the layer size.
 */
public class DrawingLayer extends JPanel {

    private List<AnnotationShape> shapes     = new ArrayList<>();
    private AnnotationShape       selected   = null;
    private AnnotationShape       inProgress = null;

    private AnnotationTool tool      = AnnotationTool.SELECT;
    private Color          drawColor = new Color(220, 50, 50);
    private float          drawThick = 2.5f;
    private boolean        filled    = false;
    private boolean        drawModeOn = false;
    private double          zoom = 1.0;

    private Point dragStart  = null;
    private Point pressPoint = null;

    private static final int ERASER_R = 20;

    /** Called by CanvasPanel.setZoom() so shape fractions stay zoom-independent. */
    public void setZoom(double z) { this.zoom = z; }

    public DrawingLayer() {
        setOpaque(false);
        setLayout(null);
        setVisible(true);    // always visible — draw mode controls interactivity, not visibility
        setFocusable(true);
        setupMouse();
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_DELETE && selected!=null) {
                    shapes.remove(selected); selected=null; repaint();
                }
            }
        });
    }

    /**
     * When draw mode is OFF, report that no point is "inside" this panel.
     * Swing uses contains() for mouse-event dispatch, so returning false here
     * makes all mouse events fall through to tiles underneath — without hiding
     * the panel (shapes stay visible).
     */
    @Override
    public boolean contains(int x, int y) {
        return drawModeOn && super.contains(x, y);
    }

    // ── MOUSE ─────────────────────────────────────────────────────────────────

    private void setupMouse() {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                pressPoint = e.getPoint();
                if (tool==AnnotationTool.SELECT)   { handleSelectPress(pressPoint); return; }
                if (tool==AnnotationTool.ERASER)   { eraseAt(pressPoint); return; }
                if (tool==AnnotationTool.TEXT_PIN) { askTextPin(pressPoint); return; }
                float fx=fx(pressPoint.x), fy=fy(pressPoint.y);
                switch(tool) {
                    case ARROW:    inProgress=new ArrowShape(fx,fy,fx,fy,drawColor,drawThick); break;
                    case LINE:     inProgress=new LineShape(fx,fy,fx,fy,drawColor,drawThick); break;
                    case RECT:     inProgress=new RectShape(fx,fy,0,0,drawColor,drawThick,filled); break;
                    case OVAL:     inProgress=new OvalShape(fx,fy,0,0,drawColor,drawThick,filled); break;
                    case FREEHAND: FreehandShape fs=new FreehandShape(drawColor,drawThick); fs.addPoint(fx,fy); inProgress=fs; break;
                }
                repaint();
            }

            @Override public void mouseDragged(MouseEvent e) {
                Point cur = e.getPoint();
                if (tool==AnnotationTool.SELECT) { handleSelectDrag(cur); return; }
                if (tool==AnnotationTool.ERASER) { eraseAt(cur); return; }
                if (inProgress==null||pressPoint==null) return;
                float fx=fx(cur.x),fy=fy(cur.y),px=fx(pressPoint.x),py=fy(pressPoint.y);
                if      (inProgress instanceof ArrowShape)    { ArrowShape a=(ArrowShape)inProgress;    a.fx2=fx; a.fy2=fy; }
                else if (inProgress instanceof LineShape)     { LineShape l=(LineShape)inProgress;      l.fx2=fx; l.fy2=fy; }
                else if (inProgress instanceof RectShape)     { RectShape r=(RectShape)inProgress;      r.fx=Math.min(px,fx); r.fy=Math.min(py,fy); r.fw=Math.abs(fx-px); r.fh=Math.abs(fy-py); }
                else if (inProgress instanceof OvalShape)     { OvalShape o=(OvalShape)inProgress;      o.fx=Math.min(px,fx); o.fy=Math.min(py,fy); o.fw=Math.abs(fx-px); o.fh=Math.abs(fy-py); }
                else if (inProgress instanceof FreehandShape) { ((FreehandShape)inProgress).addPoint(fx,fy); }
                repaint();
            }

            @Override public void mouseReleased(MouseEvent e) {
                dragStart = null;
                if (tool==AnnotationTool.SELECT||tool==AnnotationTool.ERASER) return;
                if (inProgress==null) return;
                if (isMeaningful(inProgress)) { shapes.add(inProgress); selectShape(inProgress); }
                inProgress=null; repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    // ── SELECT ────────────────────────────────────────────────────────────────

    private void handleSelectPress(Point p) {
        // Convert screen point to doc-space point for hit testing
        int w=(int)(getWidth()/Math.max(0.01,zoom)), h=(int)(getHeight()/Math.max(0.01,zoom));
        Point dp = new Point((int)(p.x/zoom), (int)(p.y/zoom));
        AnnotationShape hit=null;
        for (int i=shapes.size()-1;i>=0;i--)
            if (shapes.get(i).contains(dp,w,h)) { hit=shapes.get(i); break; }
        selectShape(hit);
        if (selected!=null) dragStart=p;
        repaint();
    }

    private void handleSelectDrag(Point cur) {
        if (selected==null||dragStart==null) return;
        // Convert screen delta to page-doc fraction delta (same as fx/fy: multiply by zoom)
        float dfx=(float)(cur.x-dragStart.x)/Math.max(1,getWidth());
        float dfy=(float)(cur.y-dragStart.y)/Math.max(1,getHeight());
        selected.translate(dfx,dfy);
        dragStart=cur;
        repaint();
    }

    private void eraseAt(Point p) {
        int w=(int)(getWidth()/Math.max(0.01,zoom)), h=(int)(getHeight()/Math.max(0.01,zoom));
        Point dp=new Point((int)(p.x/zoom),(int)(p.y/zoom));
        shapes.removeIf(s -> {
            if (s.contains(dp,w,h)) return true;
            Rectangle b=s.getBounds(w,h);
            if (b==null) return false;
            return new Point(b.x+b.width/2, b.y+b.height/2).distance(dp) < ERASER_R;
        });
        if (selected!=null && !shapes.contains(selected)) selected=null;
        repaint();
    }

    private void askTextPin(Point p) {
        String text=JOptionPane.showInputDialog(
            SwingUtilities.getWindowAncestor(this),"Enter label text:","Text Pin",JOptionPane.PLAIN_MESSAGE);
        if (text!=null&&!text.trim().isEmpty()) {
            TextPinShape pin=new TextPinShape(fx(p.x),fy(p.y),text.trim(),drawColor,drawThick);
            shapes.add(pin); selectShape(pin); repaint();
        }
    }

    private void selectShape(AnnotationShape s) {
        if (selected!=null) selected.selected=false;
        selected=s;
        if (selected!=null) { selected.selected=true; requestFocusInWindow(); }
        if (selectionChangeCallback != null) selectionChangeCallback.run();
    }

    private boolean isMeaningful(AnnotationShape s) {
        if (s instanceof FreehandShape) return ((FreehandShape)s).pts.size()>2;
        int w=Math.max(1,(int)(getWidth()/zoom)), h=Math.max(1,(int)(getHeight()/zoom));
        Rectangle b=s.getBounds(w, h);
        return b!=null&&(b.width>3||b.height>3);
    }

    // DrawingLayer is sized to pageW*zoom × pageH*zoom pixels.
    // A pixel coordinate px maps to page-doc fraction simply as px / getWidth() —
    // no extra zoom factor: that is already baked into the panel's physical size.
    private float fx(int px){ return getWidth()<=0?0:Math.max(0,Math.min(1,(float)px/getWidth())); }
    private float fy(int py){ return getHeight()<=0?0:Math.max(0,Math.min(1,(float)py/getHeight())); }

    // ── PAINT ─────────────────────────────────────────────────────────────────

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);
        // Shapes stored in page-doc fractions. Panel is pageW*zoom wide.
        // Scale graphics by zoom so fractions map to screen pixels correctly.
        int sw=getWidth(), sh=getHeight();
        g2.scale(zoom, zoom);
        int w=(int)(sw/zoom), h=(int)(sh/zoom);
        for (AnnotationShape s:shapes) s.draw(g2,w,h);
        if (inProgress!=null) inProgress.draw(g2,w,h);
        g2.dispose();
    }

    public void paintForExport(Graphics2D g2, int w, int h) {
        if (shapes.isEmpty()) return;
        Graphics2D pg=(Graphics2D)g2.create();
        pg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        AnnotationShape tmp=selected;
        if (selected!=null) selected.selected=false;
        for (AnnotationShape s:shapes) s.draw(pg,w,h);
        if (tmp!=null) tmp.selected=true;
        pg.dispose();
    }

    // ── PUBLIC API ─────────────────────────────────────────────────────────────

    /**
     * Turn draw mode on or off.
     *
     * OFF: panel stays visible so shapes are painted, but it becomes
     *      non-interactive — mouse events fall through to tiles below.
     * ON:  panel is interactive and sits on top.
     */
    public void setDrawMode(boolean on) {
        drawModeOn = on;
        // Always keep the panel visible (shapes must show in both modes).
        // Control interactivity via enabled/focusable instead.
        setVisible(true);
        setEnabled(on);
        setFocusable(on);
        if (on)  requestFocusInWindow();
        else     selectShape(null);
        repaint();
    }

    public void setLayerVisible(boolean on) {
        setVisible(on);
        repaint();
    }

    public boolean isDrawModeOn()   { return drawModeOn; }
    public boolean isLayerVisible() { return isVisible(); }

    public void setActiveTool(AnnotationTool t) {
        tool=t; selectShape(null);
        switch(t) {
            case SELECT:   setCursor(Cursor.getDefaultCursor()); break;
            case TEXT_PIN: setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)); break;
            default:       setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)); break;
        }
    }

    public void setDrawColor(Color c) { drawColor=c; }
    public void setDrawThick(float t) { drawThick=t; }
    public void setFilled(boolean f)  { filled=f; }

    /** Recolour the currently selected shape (if any). */
    public void setSelectedShapeColor(Color c) {
        if (selected != null) { selected.color = c; repaint(); }
    }

    /**
     * Returns the colour of the selected shape, or the current draw colour
     * if nothing is selected. Used to sync the toolbar swatch.
     */
    public Color getActiveColor() {
        return (selected != null) ? selected.color : drawColor;
    }

    /** Notify callback — called whenever selection changes so toolbar can sync the swatch. */
    private Runnable selectionChangeCallback;
    public void setSelectionChangeCallback(Runnable r) { selectionChangeCallback = r; }
    public java.util.List<AnnotationShape> getShapes() { return shapes; }
    public void addShape(AnnotationShape s) { shapes.add(s); repaint(); }

    public void deleteSelected()      { if(selected!=null){shapes.remove(selected);selected=null;repaint();} }
    public void clearAll()            { shapes.clear();selected=null;inProgress=null;repaint(); }
    public boolean hasShapes()        { return !shapes.isEmpty(); }
    public AnnotationTool getActiveTool() { return tool; }
    public Color getDrawColor()       { return drawColor; }
    public float getDrawThick()       { return drawThick; }
    public boolean isFilled()         { return filled; }
}
