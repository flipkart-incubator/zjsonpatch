package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class PathUtils {
    private static final Pattern ENCODED_TILDA_PATTERN = Pattern.compile("~");
    private static final Pattern ENCODED_SLASH_PATTERN = Pattern.compile("/");

    private static final Pattern DECODED_TILDA_PATTERN = Pattern.compile("~0");
    private static final Pattern DECODED_SLASH_PATTERN = Pattern.compile("~1");

    private static String encodePath(Object object) {
        String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
        path = ENCODED_TILDA_PATTERN.matcher(path).replaceAll("~0");
        return ENCODED_SLASH_PATTERN.matcher(path).replaceAll("~1");
    }

    private static String decodePath(Object object) {
        String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
        path = DECODED_TILDA_PATTERN.matcher(path).replaceAll("~");
        return DECODED_SLASH_PATTERN.matcher(path).replaceAll("/");
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

    static List<String> getPath(JsonNode path) {
        List<String> result = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        String cleanPath = path.toString().replaceAll("\"", "");
        for (int index = 0; index < cleanPath.length(); index++) {
            char c = cleanPath.charAt(index);
            if (c == '/') {
                result.add(decodePath(builder.toString()));
                builder.delete(0,  builder.length());
            } else {
                builder.append(c);
            }
        }
        result.add(decodePath(builder.toString()));
        return result;
    }
}
