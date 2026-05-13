package com.chenghua.security;

import org.apache.commons.text.StringEscapeUtils;

public final class XssSanitizer {

    private XssSanitizer() {
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return StringEscapeUtils.escapeHtml4(input);
    }
}

