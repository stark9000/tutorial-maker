package tutorialmaker.io;

import tutorialmaker.canvas.CanvasPanel;
import tutorialmaker.tiles.*;
import tutorialmaker.annotation.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Saves and loads a TutorialMaker project (.tmk) as hand-rolled JSON.
 * Images are stored by file path only — no copying.
 */
public class ProjectSerializer {

    // ── SAVE ─────────────────────────────────────────────────────────────────

    public static void save(CanvasPanel canvas, File file) throws IOException {
        JsonWriter w = new JsonWriter();

        // Canvas settings
        w.str("version", "1.0");
        w.num("pageW",   canvas.getPageW());
        w.num("pageH",   canvas.getPageH());
        w.num("zoom",    canvas.getZoom());
        w.bool("snapToGrid",  canvas.isSnapToGrid());
        w.bool("showBorders", canvas.isShowBorders());

        // Tiles
        w.beginArray("tiles");
        for (TileComponent t : canvas.getTiles()) {
            w.beginArrayObject();
            writeTile(w, t);
            w.endArrayObject();
        }
        w.endArray();

        // Drawing layer shapes
        w.beginArray("annotations");
        writeAnnotations(w, canvas.getDrawingLayer());
        w.endArray();

        String json = w.toJson();
        Files.write(file.toPath(), json.getBytes("UTF-8"));
    }

    private static void writeTile(JsonWriter w, TileComponent t) {
        // Common fields
        w.str("type",   tileType(t));
        w.num("docX",   t.getDocX());
        w.num("docY",   t.getDocY());
        w.num("docW",   t.getDocW());
        w.num("docH",   t.getDocH());
        w.bool("showBorder", t.isShowBorder());

        if (t instanceof ImageTile) {
            ImageTile it = (ImageTile) t;
            w.str("imagePath", it.getImagePath());

        } else if (t instanceof TextTile) {
            TextTile tt = (TextTile) t;
            w.str("text",      tt.getText());
            w.font("font",     tt.getTileFont());
            w.color("textColor", tt.getTextColor());
            w.color("bgColor",   tt.getBgColor());
            w.str("alignment", tt.getAlignment());

        } else if (t instanceof LabelTile) {
            LabelTile lt = (LabelTile) t;
            w.str("text",      lt.getText());
            w.str("style",     lt.getLabelStyle().name());
            w.color("bgColor",   lt.getBgColor());
            w.color("textColor", lt.getTextColor());
        }
    }

    private static void writeAnnotations(JsonWriter w, DrawingLayer dl) {
        if (dl == null) return;
        for (AnnotationShape s : dl.getShapes()) {
            w.beginArrayObject();
            w.str("type",  shapeType(s));
            w.color("color", s.color);
            w.num("thickness", s.thickness);

            if (s instanceof ArrowShape) {
                ArrowShape a = (ArrowShape) s;
                w.num("fx1",a.fx1); w.num("fy1",a.fy1);
                w.num("fx2",a.fx2); w.num("fy2",a.fy2);
            } else if (s instanceof LineShape) {
                LineShape l = (LineShape) s;
                w.num("fx1",l.fx1); w.num("fy1",l.fy1);
                w.num("fx2",l.fx2); w.num("fy2",l.fy2);
            } else if (s instanceof RectShape) {
                RectShape r = (RectShape) s;
                w.num("fx",r.fx); w.num("fy",r.fy);
                w.num("fw",r.fw); w.num("fh",r.fh);
                w.bool("filled", r.filled);
            } else if (s instanceof OvalShape) {
                OvalShape o = (OvalShape) s;
                w.num("fx",o.fx); w.num("fy",o.fy);
                w.num("fw",o.fw); w.num("fh",o.fh);
                w.bool("filled", o.filled);
            } else if (s instanceof FreehandShape) {
                FreehandShape f = (FreehandShape) s;
                StringBuilder pts = new StringBuilder();
                for (float[] p : f.pts) pts.append(p[0]).append(",").append(p[1]).append(";");
                w.str("pts", pts.toString());
            } else if (s instanceof TextPinShape) {
                TextPinShape tp = (TextPinShape) s;
                w.num("fx", tp.fx); w.num("fy", tp.fy);
                w.str("text", tp.text);
            }
            w.endArrayObject();
        }
    }

    private static String tileType(TileComponent t) {
        if (t instanceof ImageTile) return "image";
        if (t instanceof TextTile)  return "text";
        if (t instanceof LabelTile) return "label";
        return "unknown";
    }

    private static String shapeType(AnnotationShape s) {
        if (s instanceof ArrowShape)   return "arrow";
        if (s instanceof LineShape)    return "line";
        if (s instanceof RectShape)    return "rect";
        if (s instanceof OvalShape)    return "oval";
        if (s instanceof FreehandShape) return "freehand";
        if (s instanceof TextPinShape) return "textpin";
        return "unknown";
    }

    // ── LOAD ─────────────────────────────────────────────────────────────────

    public static void load(CanvasPanel canvas, File file) throws IOException {
        String json = new String(Files.readAllBytes(file.toPath()), "UTF-8");
        JsonParser parser = new JsonParser(json);
        Map<String,Object> root = JsonParser.asMap(parser.parse());

        // Canvas settings
        int pageW  = JsonParser.asInt(root.get("pageW"), CanvasPanel.PAGE_W);
        int pageH  = JsonParser.asInt(root.get("pageH"), CanvasPanel.PAGE_H);
        double zoom = JsonParser.asDouble(root.get("zoom"), 1.0);
        boolean snap    = JsonParser.asBool(root.get("snapToGrid"), false);
        boolean borders = JsonParser.asBool(root.get("showBorders"), true);

        canvas.clearAll();
        canvas.setPageSize(pageW, pageH);
        canvas.setSnapToGrid(snap);
        canvas.setShowBorders(borders);

        // Tiles
        List<Object> tiles = JsonParser.asList(root.get("tiles"));
        for (Object obj : tiles) {
            Map<String,Object> m = JsonParser.asMap(obj);
            TileComponent tile = readTile(m);
            if (tile != null) canvas.addTileDirect(tile);
        }

        // Annotations
        List<Object> anns = JsonParser.asList(root.get("annotations"));
        DrawingLayer dl = canvas.getDrawingLayer();
        if (dl != null) {
            dl.clearAll();
            for (Object obj : anns) {
                Map<String,Object> m = JsonParser.asMap(obj);
                AnnotationShape s = readShape(m);
                if (s != null) dl.addShape(s);
            }
        }

        // Apply zoom last (repositions everything)
        canvas.setZoom(zoom);
        canvas.repaint();
    }

    private static TileComponent readTile(Map<String,Object> m) {
        String type = JsonParser.asStr(m.get("type"), "");
        int x = JsonParser.asInt(m.get("docX"), 20);
        int y = JsonParser.asInt(m.get("docY"), 20);
        int w = JsonParser.asInt(m.get("docW"), 200);
        int h = JsonParser.asInt(m.get("docH"), 150);
        boolean border = JsonParser.asBool(m.get("showBorder"), true);

        TileComponent tile = null;

        if ("image".equals(type)) {
            String path = JsonParser.asStr(m.get("imagePath"), "");
            ImageIcon icon = null;
            if (!path.isEmpty()) {
                File f = new File(path);
                if (f.exists()) icon = new ImageIcon(path);
            }
            tile = new ImageTile(x, y, w, h, icon, path);

        } else if ("text".equals(type)) {
            String text  = JsonParser.asStr(m.get("text"), "");
            Font   font  = parseFont(JsonParser.asStr(m.get("font"), "SansSerif,0,14"));
            Color  tc    = parseColor(JsonParser.asStr(m.get("textColor"), "#000000"));
            Color  bg    = parseColor(JsonParser.asStr(m.get("bgColor"),   "#ffffff"));
            String align = JsonParser.asStr(m.get("alignment"), "Left");
            TextTile tt  = new TextTile(x, y, w, h, text);
            tt.setTileFont(font); tt.setTextColor(tc); tt.setBgColor(bg); tt.setAlignment(align);
            tile = tt;

        } else if ("label".equals(type)) {
            String text   = JsonParser.asStr(m.get("text"), "");
            String styleName = JsonParser.asStr(m.get("style"), "CAPTION");
            LabelTile.Style style;
            try { style = LabelTile.Style.valueOf(styleName); }
            catch (Exception e) { style = LabelTile.Style.CAPTION; }
            Color bg = parseColor(JsonParser.asStr(m.get("bgColor"),   "#f0f0f0"));
            Color tc = parseColor(JsonParser.asStr(m.get("textColor"), "#000000"));
            LabelTile lt = new LabelTile(x, y, text, style);
            lt.setBgColor(bg); lt.setTextColor(tc);
            lt.setDocW(w); lt.setDocH(h);
            tile = lt;
        }

        if (tile != null) tile.applyBorderStyle(border);
        return tile;
    }

    private static AnnotationShape readShape(Map<String,Object> m) {
        String type  = JsonParser.asStr(m.get("type"), "");
        Color  color = parseColor(JsonParser.asStr(m.get("color"), "#dc3232"));
        float  thick = (float) JsonParser.asDouble(m.get("thickness"), 2.5);

        switch (type) {
            case "arrow": {
                float x1=(float)JsonParser.asDouble(m.get("fx1"),0);
                float y1=(float)JsonParser.asDouble(m.get("fy1"),0);
                float x2=(float)JsonParser.asDouble(m.get("fx2"),0);
                float y2=(float)JsonParser.asDouble(m.get("fy2"),0);
                return new ArrowShape(x1,y1,x2,y2,color,thick);
            }
            case "line": {
                float x1=(float)JsonParser.asDouble(m.get("fx1"),0);
                float y1=(float)JsonParser.asDouble(m.get("fy1"),0);
                float x2=(float)JsonParser.asDouble(m.get("fx2"),0);
                float y2=(float)JsonParser.asDouble(m.get("fy2"),0);
                return new LineShape(x1,y1,x2,y2,color,thick);
            }
            case "rect": {
                float x=(float)JsonParser.asDouble(m.get("fx"),0);
                float y=(float)JsonParser.asDouble(m.get("fy"),0);
                float w=(float)JsonParser.asDouble(m.get("fw"),0);
                float h=(float)JsonParser.asDouble(m.get("fh"),0);
                boolean filled = JsonParser.asBool(m.get("filled"),false);
                return new RectShape(x,y,w,h,color,thick,filled);
            }
            case "oval": {
                float x=(float)JsonParser.asDouble(m.get("fx"),0);
                float y=(float)JsonParser.asDouble(m.get("fy"),0);
                float w=(float)JsonParser.asDouble(m.get("fw"),0);
                float h=(float)JsonParser.asDouble(m.get("fh"),0);
                boolean filled = JsonParser.asBool(m.get("filled"),false);
                return new OvalShape(x,y,w,h,color,thick,filled);
            }
            case "freehand": {
                FreehandShape fs = new FreehandShape(color, thick);
                String pts = JsonParser.asStr(m.get("pts"), "");
                for (String seg : pts.split(";")) {
                    String[] xy = seg.split(",");
                    if (xy.length == 2) {
                        try { fs.addPoint(Float.parseFloat(xy[0]), Float.parseFloat(xy[1])); }
                        catch (Exception ignored) {}
                    }
                }
                return fs;
            }
            case "textpin": {
                float x=(float)JsonParser.asDouble(m.get("fx"),0);
                float y=(float)JsonParser.asDouble(m.get("fy"),0);
                String text = JsonParser.asStr(m.get("text"),"");
                return new TextPinShape(x,y,text,color,thick);
            }
        }
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static Color parseColor(String hex) {
        if (hex == null || hex.isEmpty() || hex.equals("null")) return Color.BLACK;
        try {
            hex = hex.replace("#","");
            int r = Integer.parseInt(hex.substring(0,2),16);
            int g = Integer.parseInt(hex.substring(2,4),16);
            int b = Integer.parseInt(hex.substring(4,6),16);
            return new Color(r,g,b);
        } catch (Exception e) { return Color.BLACK; }
    }

    public static Font parseFont(String s) {
        if (s == null || s.isEmpty() || s.equals("null")) return new Font("SansSerif",Font.PLAIN,14);
        String[] p = s.split(",");
        if (p.length < 3) return new Font("SansSerif",Font.PLAIN,14);
        try {
            return new Font(p[0], Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()));
        } catch (Exception e) { return new Font("SansSerif",Font.PLAIN,14); }
    }
}
