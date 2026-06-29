package tutorialmaker.io;

import java.awt.Color;
import java.awt.Font;

/**
 * Minimal JSON builder — no external libraries.
 * Produces pretty-printed JSON strings.
 */
public class JsonWriter {

    private final StringBuilder sb = new StringBuilder();
    private int indent = 0;

    public JsonWriter() { sb.append("{\n"); indent++; }

    public String toJson() {
        return sb.toString().replaceAll(",\n$", "\n") + "}";
    }

    // ── Primitives ────────────────────────────────────────────────────────────

    public JsonWriter str(String key, String value) {
        append(quoted(key) + ": " + quoted(escape(value)));
        return this;
    }

    public JsonWriter num(String key, int value) {
        append(quoted(key) + ": " + value);
        return this;
    }

    public JsonWriter num(String key, double value) {
        append(quoted(key) + ": " + value);
        return this;
    }

    public JsonWriter bool(String key, boolean value) {
        append(quoted(key) + ": " + value);
        return this;
    }

    public JsonWriter color(String key, Color c) {
        if (c == null) return str(key, "null");
        return str(key, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
    }

    public JsonWriter font(String key, Font f) {
        if (f == null) return str(key, "null");
        return str(key, f.getName() + "," + f.getStyle() + "," + f.getSize());
    }

    // ── Objects ───────────────────────────────────────────────────────────────

    public JsonWriter beginObject(String key) {
        appendRaw(ind() + quoted(key) + ": {\n");
        indent++;
        return this;
    }

    public JsonWriter endObject() {
        indent--;
        // Remove trailing comma from last entry
        trimLastComma();
        appendRaw(ind() + "},\n");
        return this;
    }

    // ── Arrays ────────────────────────────────────────────────────────────────

    public JsonWriter beginArray(String key) {
        appendRaw(ind() + quoted(key) + ": [\n");
        indent++;
        return this;
    }

    public JsonWriter endArray() {
        indent--;
        trimLastComma();
        appendRaw(ind() + "],\n");
        return this;
    }

    public JsonWriter beginArrayObject() {
        appendRaw(ind() + "{\n");
        indent++;
        return this;
    }

    public JsonWriter endArrayObject() {
        indent--;
        trimLastComma();
        appendRaw(ind() + "},\n");
        return this;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void append(String kv)    { appendRaw(ind() + kv + ",\n"); }
    private void appendRaw(String s)  { sb.append(s); }
    private String ind()              { StringBuilder s=new StringBuilder(); for(int i=0;i<indent;i++) s.append("  "); return s.toString(); }
    private String quoted(String s)   { return "\"" + s + "\""; }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void trimLastComma() {
        // Find last ",\n" and remove the comma
        int idx = sb.lastIndexOf(",\n");
        if (idx >= 0) sb.replace(idx, idx+1, "");
    }
}
