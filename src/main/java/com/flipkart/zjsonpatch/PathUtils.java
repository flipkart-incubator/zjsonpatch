package com.flipkart.zjsonpatch;

import java.util.List;
import java.util.regex.Pattern;

class PathUtils {
    private static final Pattern TILDA_PATTERN = Pattern.compile("~");
    private static final Pattern SLASH_PATTERN = Pattern.compile("/");

    private static String encodePath(Object object) {
        String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
        path = TILDA_PATTERN.matcher(path).replaceAll("~0");
        return SLASH_PATTERN.matcher(path).replaceAll("~1");
    }

    static <T> String getArrayNodeRepresentation(List<T> path) {
        StringBuilder builder = new StringBuilder();
        builder.append('/');
        int count = 0;
        for (Object o : path) {
            if (++count > 1)
                builder.append('/');
            builder.append(encodePath(o));
        }
        return builder.toString();
    }
}
