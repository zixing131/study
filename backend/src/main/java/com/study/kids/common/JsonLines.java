package com.study.kids.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 轻量诗句 JSON 编解码，避免强依赖 Jackson 包名差异。
 * 格式：["句1","句2"]
 */
public final class JsonLines {

    private JsonLines() {
    }

    public static List<String> parse(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        String s = json.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) {
            return Collections.emptyList();
        }
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inString = false;
        boolean escape = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                cur.append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (c == ',' && !inString) {
                result.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            if (inString) {
                cur.append(c);
            }
        }
        result.add(cur.toString());
        return result;
    }

    public static String stringify(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(escape(lines.get(i))).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
