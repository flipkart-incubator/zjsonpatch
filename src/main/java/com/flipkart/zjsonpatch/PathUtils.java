package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class PathUtils {
    // FIXME inline
    static String getPathRepresentation(JsonPointer path) {
        return path.toString();
    }

    static JsonPointer getPath(JsonNode path) {
        return JsonPointer.parse(path.textValue());
    }
}
