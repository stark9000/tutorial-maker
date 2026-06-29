package tutorialmaker.io;

import java.util.*;

/**
 * Minimal JSON parser — no external libraries.
 * Parses JSON into nested Map/List/String/Number/Boolean structures.
 */
public class JsonParser {

    private final String src;
    private int pos = 0;

    public JsonParser(String src) { this.src = src.trim(); }

    /** Parse and return the root value. */
    public Object parse() { return parseValue(); }

    private Object parseValue() {
        skipWs();
        if (pos >= src.length()) return null;
        char c = src.charAt(pos);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't') { pos+=4; return Boolean.TRUE; }
        if (c == 'f') { pos+=5; return Boolean.FALSE; }
        if (c == 'n') { pos+=4; return null; }
        return parseNumber();
    }

    private Map<String,Object> parseObject() {
        Map<String,Object> map = new LinkedHashMap<>();
        pos++; // '{'
        skipWs();
        while (pos < src.length() && src.charAt(pos) != '}') {
            skipWs();
            String key = parseString();
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ':') pos++; // ':'
            skipWs();
            Object val = parseValue();
            map.put(key, val);
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ',') pos++;
            skipWs();
        }
        if (pos < src.length()) pos++; // '}'
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        pos++; // '['
        skipWs();
        while (pos < src.length() && src.charAt(pos) != ']') {
            list.add(parseValue());
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ',') pos++;
            skipWs();
        }
        if (pos < src.length()) pos++; // ']'
        return list;
    }

    private String parseString() {
        pos++; // opening '"'
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') break;
            if (c == '\\' && pos < src.length()) {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    default:   sb.append(esc);  break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Object parseNumber() {
        int start = pos;
        while (pos < src.length() && "0123456789.-+eE".indexOf(src.charAt(pos)) >= 0) pos++;
        String num = src.substring(start, pos);
        try {
            if (num.contains(".") || num.contains("e") || num.contains("E"))
                return Double.parseDouble(num);
            return Long.parseLong(num);
        } catch (NumberFormatException e) { return 0; }
    }

    private void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    // ── Typed accessors ───────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static Map<String,Object> asMap(Object o) {
        return o instanceof Map ? (Map<String,Object>) o : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object o) {
        return o instanceof List ? (List<Object>) o : Collections.emptyList();
    }

    public static String asStr(Object o, String def) {
        return o instanceof String ? (String) o : def;
    }

    public static int asInt(Object o, int def) {
        if (o instanceof Long)   return ((Long) o).intValue();
        if (o instanceof Double) return ((Double) o).intValue();
        if (o instanceof String) try { return Integer.parseInt((String)o); } catch(Exception e){}
        return def;
    }

    public static double asDouble(Object o, double def) {
        if (o instanceof Double) return (Double) o;
        if (o instanceof Long)   return ((Long) o).doubleValue();
        return def;
    }

    public static boolean asBool(Object o, boolean def) {
        if (o instanceof Boolean) return (Boolean) o;
        return def;
    }
}
