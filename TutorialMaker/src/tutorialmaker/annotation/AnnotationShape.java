package tutorialmaker.annotation;

import java.awt.*;

/**
 * Base class for all annotation shapes.
 * Coords stored as 0.0-1.0 fractions of the DrawingLayer size
 * so shapes stay correct when the layer is resized or zoomed.
 */
public abstract class AnnotationShape {

    public Color   color;
    public float   thickness;
    public boolean selected;

    public AnnotationShape(Color c, float t) { color = c; thickness = t; }

    /** Draw shape. w,h = current pixel size of the DrawingLayer panel. */
    public abstract void draw(Graphics2D g2, int w, int h);

    /** Hit-test in pixel coords relative to the DrawingLayer panel. */
    public abstract boolean contains(Point p, int w, int h);

    public abstract Rectangle getBounds(int w, int h);

    /** Move shape by fractional deltas (0.0-1.0 space). */
    public abstract void translate(float dfx, float dfy);

    /** Draw a dashed selection box around getBounds(). */
    public void drawSelBox(Graphics2D g2, int w, int h) {
        Rectangle b = getBounds(w, h);
        if (b == null) return;
        g2.setColor(new Color(0, 120, 215, 150));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1, new float[]{4, 2}, 0));
        g2.drawRect(b.x-4, b.y-4, b.width+8, b.height+8);
        int hs = 6;
        int[][] pts = {
            {b.x-4,           b.y-4},
            {b.x+b.width+4-hs, b.y-4},
            {b.x-4,           b.y+b.height+4-hs},
            {b.x+b.width+4-hs, b.y+b.height+4-hs}
        };
        g2.setStroke(new BasicStroke(1f));
        for (int[] p : pts) {
            g2.setColor(Color.WHITE);          g2.fillRect(p[0], p[1], hs, hs);
            g2.setColor(new Color(0,120,215)); g2.drawRect(p[0], p[1], hs, hs);
        }
    }
}
