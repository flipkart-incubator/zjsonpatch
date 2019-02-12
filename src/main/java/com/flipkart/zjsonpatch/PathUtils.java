package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class PathUtils {
    private static final Pattern ENCODED_TILDA_PATTERN = Pattern.compile("~");
    private static final Pattern ENCODED_SLASH_PATTERN = Pattern.compile("/");

    private static String encodePath(Object object) {
        String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
        path = ENCODED_TILDA_PATTERN.matcher(path).replaceAll("~0");
        return ENCODED_SLASH_PATTERN.matcher(path).replaceAll("~1");
    }

    // FIXME inline
    static String getPathRepresentation(JsonPointer path) {
        return path.toString();
    }

    // FIXME remove
    static <T> String getPathRepresentation(List<T> path) {
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

    static JsonPointer getPath(JsonNode path) {
        return JsonPointer.parse(path.textValue());
    }
}
