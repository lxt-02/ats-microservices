package com.ats.shared.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlSanitizerTest {

    @Test
    void keepsRichTextFormatting() {
        String html = "<h2>Role</h2><p>Build <b>APIs</b> with <i>Java</i></p><ul><li>One</li><li>Two</li></ul>";
        assertEquals(html, HtmlSanitizer.sanitize(html));
    }

    @Test
    void removesScriptsEventHandlersAndDangerousLinks() {
        String dirty = "<p onclick=\"alert(1)\">Hi<script>alert('x')</script></p>"
                + "<img src=x onerror=alert(1)><a href=\"javascript:alert(1)\">bad</a>"
                + "<a href=\"&#106;avascript:alert(1)\">bad2</a><a href='https://example.com/a?b=1&c=2'>ok</a>"
                + "<iframe src=\"https://evil\"></iframe><style>body{}</style>";
        String clean = HtmlSanitizer.sanitize(dirty);

        assertFalse(clean.toLowerCase().contains("script"), clean);
        assertFalse(clean.toLowerCase().contains("onerror"), clean);
        assertFalse(clean.toLowerCase().contains("onclick"), clean);
        assertFalse(clean.toLowerCase().contains("javascript"), clean);
        assertFalse(clean.contains("<img"), clean);
        assertFalse(clean.contains("iframe"), clean);
        assertTrue(clean.contains("<a href=\"https://example.com/a?b=1&amp;c=2\" rel=\"nofollow noopener\">ok</a>"),
                clean);
        assertTrue(clean.startsWith("<p>Hi</p>"), clean);
    }

    @Test
    void escapesStrayAngleBracketsAndClosesOpenTags() {
        assertEquals("<p>a &lt; b &amp;&amp; c &gt; d</p>", HtmlSanitizer.sanitize("<p>a < b && c > d"));
        assertEquals("&lt;!-- comment --&gt;", HtmlSanitizer.sanitize("<!-- comment -->"));
    }

    @Test
    void plainTextAndExcerpt() {
        String html = "<h2>Senior&nbsp;Java</h2><p>We are &amp; hiring</p><script>x()</script>";
        assertEquals("Senior Java We are & hiring", HtmlSanitizer.toPlainText(html));
        assertEquals("Senior Java...", HtmlSanitizer.excerpt(html, 15));
    }
}
