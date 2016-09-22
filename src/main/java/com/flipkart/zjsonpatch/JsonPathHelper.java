package com.flipkart.zjsonpatch;

import java.util.ArrayList;
import java.util.List;

class JsonPathHelper {

    public static List<String> getPath(String path) {
        List<String> paths = new ArrayList<String>();
        int index = 0, last = 0, len = path.length();
        while (index < len) {
            if (path.charAt(index) == '/') {
                paths.add(decodeSubPath(path.substring(last, index)));
                last = ++index;
            } else {
                index++;
            }
        }
        paths.add(decodeSubPath(path.substring(last, index)));
        return paths;
    }

    public static List<Object> getPathExt(List<Object> path, Object key) {
        List<Object> toReturn = new ArrayList<Object>();
        toReturn.addAll(path);
        toReturn.add(key);
        return toReturn;
    }

    public static String getPathRep(List<Object> path) {
        StringBuilder builder = new StringBuilder();
        for (Object elem : path) {
            builder.append('/').append(encodeSubPath(elem.toString()));
        }
        return builder.toString();
    }

    private static String decodeSubPath(String path) {
        // see http://tools.ietf.org/html/rfc6901#section-4
        return path.replaceAll("~1", "/").replaceAll("~0", "~");
    }


    private static String encodeSubPath(String path) {
        // see http://tools.ietf.org/html/rfc6901#section-4
        return path.replaceAll("~", "~0").replaceAll("/", "~1");
    }
}
