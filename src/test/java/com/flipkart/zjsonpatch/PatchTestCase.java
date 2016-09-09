package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class PatchTestCase {

    private final boolean operation;
    private final JsonNode node;

    private PatchTestCase(boolean isOperation, JsonNode node) {
        this.operation = isOperation;
        this.node = node;
    }

    public boolean isOperation() {
        return operation;
    }

    public JsonNode getNode() {
        return node;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Collection<PatchTestCase> load(String fileName) throws IOException {
        String path = "/testdata/" + fileName + ".json";
        InputStream resourceAsStream = PatchTestCase.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        JsonNode tree = MAPPER.readTree(testData);

        List<PatchTestCase> result = new ArrayList<PatchTestCase>();
        for (JsonNode node : tree.get("errors")) {
            if (isEnabled(node)) {
                result.add(new PatchTestCase(false, node));
            }
        }
        for (JsonNode node : tree.get("ops")) {
            if (isEnabled(node)) {
                result.add(new PatchTestCase(true, node));
            }
        }
        return result;
    }

    private static boolean isEnabled(JsonNode node) {
        JsonNode disabled = node.get("disabled");
        return (disabled == null || !disabled.booleanValue());
    }
}
