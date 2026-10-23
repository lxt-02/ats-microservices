package com.ats.shared.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allow-list sanitizer for rich text written in the internal portal and shown on the public Career Site.
 * The output is rebuilt from scratch: allowed tags are re-emitted without any attribute (except a checked
 * {@code href} on links), everything else is dropped and all text is escaped. Script-like elements are removed
 * together with their content.
 */
public final class HtmlSanitizer {

    private static final Set<String> ALLOWED_TAGS = Set.of(
            "p", "br", "b", "strong", "i", "em", "u", "s", "h1", "h2", "h3", "h4", "ul", "ol", "li",
            "a", "blockquote", "code", "pre", "div", "hr");
    private static final Set<String> VOID_TAGS = Set.of("br", "hr");
    private static final Set<String> BLOCK_TAGS = Set.of(
            "p", "br", "h1", "h2", "h3", "h4", "ul", "ol", "li", "blockquote", "pre", "div", "hr", "tr", "table");
    private static final Set<String> DROP_WITH_CONTENT = Set.of(
            "script", "style", "iframe", "object", "embed", "noscript", "template", "svg", "math", "textarea", "title");

    private static final Pattern TAG = Pattern.compile("<(/?)([a-zA-Z][a-zA-Z0-9]*)\\b([^<>]*)>");
    private static final Pattern HREF = Pattern.compile(
            "\\bhref\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+))", Pattern.CASE_INSENSITIVE);
    private static final Pattern SAFE_URL = Pattern.compile(
            "^(?:https?://|mailto:)[^\\s\"'<>`\\\\\\x00-\\x1f]*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENTITY = Pattern.compile("&(?:[a-zA-Z][a-zA-Z0-9]{1,31}|#[0-9]{1,7}|#[xX][0-9a-fA-F]{1,6});");

    private HtmlSanitizer() {
    }

    /** Returns safe HTML, or null for null input. */
    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        StringBuilder out = new StringBuilder(html.length());
        Deque<String> open = new ArrayDeque<>();
        Matcher matcher = TAG.matcher(html);
        int position = 0;
        while (matcher.find(position)) {
            appendText(out, html, position, matcher.start());
            String name = matcher.group(2).toLowerCase(Locale.ROOT);
            boolean closing = !matcher.group(1).isEmpty();
            position = matcher.end();

            if (!closing && DROP_WITH_CONTENT.contains(name)) {
                int end = indexOfIgnoreCase(html, "</" + name, position);
                if (end < 0) {
                    return closeAll(out, open);
                }
                int close = html.indexOf('>', end);
                position = close < 0 ? html.length() : close + 1;
                continue;
            }
            if (!ALLOWED_TAGS.contains(name)) {
                continue;
            }
            if (closing) {
                if (open.contains(name)) {
                    String top;
                    do {
                        top = open.pop();
                        out.append("</").append(top).append('>');
                    } while (!top.equals(name));
                }
                continue;
            }
            if (VOID_TAGS.contains(name)) {
                out.append('<').append(name).append('>');
                continue;
            }
            if ("a".equals(name)) {
                String href = safeHref(matcher.group(3));
                out.append(href == null ? "<a>" : "<a href=\"" + escape(href) + "\" rel=\"nofollow noopener\">");
            } else {
                out.append('<').append(name).append('>');
            }
            open.push(name);
        }
        appendText(out, html, position, html.length());
        return closeAll(out, open);
    }

    /** Plain text of an HTML fragment: tags removed, common entities decoded, whitespace collapsed. */
    public static String toPlainText(String html) {
        if (html == null) {
            return null;
        }
        StringBuilder text = new StringBuilder(html.length());
        Matcher matcher = TAG.matcher(html);
        int position = 0;
        while (matcher.find(position)) {
            text.append(html, position, matcher.start());
            String name = matcher.group(2).toLowerCase(Locale.ROOT);
            position = matcher.end();
            if (matcher.group(1).isEmpty() && DROP_WITH_CONTENT.contains(name)) {
                int end = indexOfIgnoreCase(html, "</" + name, position);
                int close = end < 0 ? -1 : html.indexOf('>', end);
                position = close < 0 ? html.length() : close + 1;
            }
            text.append(BLOCK_TAGS.contains(name) ? ' ' : "");
        }
        text.append(html, position, html.length());
        return decodeEntities(text.toString()).replaceAll("\\s+", " ").trim();
    }

    /** First {@code maxLength} characters of the plain text, cut on a word and suffixed with "...". */
    public static String excerpt(String html, int maxLength) {
        String text = toPlainText(html);
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        int cut = text.lastIndexOf(' ', maxLength - 3);
        return text.substring(0, cut > maxLength / 2 ? cut : maxLength - 3).trim() + "...";
    }

    private static String safeHref(String attributes) {
        Matcher matcher = HREF.matcher(attributes);
        if (!matcher.find()) {
            return null;
        }
        String value = matcher.group(1) != null ? matcher.group(1)
                : matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
        value = value.trim();
        return SAFE_URL.matcher(value).matches() && !value.contains("&#") ? value : null;
    }

    private static void appendText(StringBuilder out, String html, int from, int to) {
        String text = html.substring(from, to);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                case '&' -> {
                    Matcher entity = ENTITY.matcher(text).region(i, text.length());
                    if (entity.lookingAt()) {
                        out.append(entity.group());
                        i = entity.end() - 1;
                    } else {
                        out.append("&amp;");
                    }
                }
                default -> {
                    if (c >= 0x20 || c == '\n' || c == '\r' || c == '\t') {
                        out.append(c);
                    }
                }
            }
        }
    }

    private static String closeAll(StringBuilder out, Deque<String> open) {
        while (!open.isEmpty()) {
            out.append("</").append(open.pop()).append('>');
        }
        return out.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String decodeEntities(String text) {
        return text.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replace("&#39;", "'").replace("&apos;", "'").replace("&amp;", "&");
    }

    private static int indexOfIgnoreCase(String text, String needle, int from) {
        return text.toLowerCase(Locale.ROOT).indexOf(needle.toLowerCase(Locale.ROOT), from);
    }
}
