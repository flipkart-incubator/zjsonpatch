package com.flipkart.zjsonpatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;

public class JsonPatchBenchmark {
    private static final int SOURCE_ITEMS = 2;
    private static final int TARGET_ITEMS = 2;
    private static final int SOURCE_PRICES = 5;
    private static final int TARGET_PRICES = 5;
    private static final int[] TARGET_PROPS = new int[] { 7, 3, 8, 3 };
    private static final int[] SOURCE_PROPS = new int[] { 7, 2, 8, 3 };// new int[] { 4, 2, 2, 0 };
    private static final int MAX_CHANGES = 800; // Integer.MAX_VALUE;

    private static final int SIZE = 1000;
    private static final int ITER = Integer.MAX_VALUE;
    private static final int WARMS = 2;
    private static final int RUNS = 20;
    private static final int LOOPS = 20;

    private static final Map<String, Map<Integer, String>> KEYNAMEMAP = new HashMap<String, Map<Integer, String>>();

    public static void main(String[] args) {
        final Random random = new Random();
        final JsonNodeFactory factory = new JsonNodeFactory(true);
        final ObjectNode[] source = new ObjectNode[RUNS];
        final ObjectNode[] target = new ObjectNode[RUNS];
        @SuppressWarnings("unchecked")
        final List<JsonNode>[] patch = new List[RUNS];

        checkJsonDiff(random, factory, source, target, patch);
    }

    public static void checkJsonDiff(final Random random, final JsonNodeFactory factory, final ObjectNode[] source,
            final ObjectNode[] target, final List<JsonNode>[] patch) {
        for (int size = 512; size <= 32768; size *= 2) {
            create(random, factory, source, target, patch, size);
            evaluate("actual", factory, source, target, patch, RUNS);
        }
    }

    public static void checkJsonPatch(final Random random, final JsonNodeFactory factory, final ObjectNode[] source,
            final ObjectNode[] target, final List<JsonNode>[] patch) {
        create(random, factory, source, target, patch, SIZE);
        evaluate("warmup", factory, source, target, patch, WARMS);
        evaluate("actual", factory, source, target, patch, RUNS);
    }

    private static List<JsonNode> createJsonPatchList(JsonNode source, JsonNode target) {
        JsonNode patch = JsonDiff.asJson(source, target);
        List<JsonNode> list = new ArrayList<JsonNode>(patch.size());
        Iterator<JsonNode> iter = patch.iterator();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }

    private static void create(final Random random, final JsonNodeFactory factory, final ObjectNode[] source,
            final ObjectNode[] target, final List<JsonNode>[] patch, int size) {
        System.out.print("Setup runs [size=" + size + "] => ");
        long count = 0, start = System.nanoTime();
        for (int run = 0; run < RUNS; run++) {
            source[run] = createRandomTour(random, new ObjectNode(factory),
                    new int[] { size, SOURCE_ITEMS, SOURCE_PRICES }, SOURCE_PROPS);
            System.out.print(".");
            target[run] = extendRandomTour(random, source[run].deepCopy(),
                    new int[] { size, TARGET_ITEMS, TARGET_PRICES }, TARGET_PROPS, MAX_CHANGES);
            System.out.print(".");
            for (int loop = 0; loop < LOOPS; loop++) {
                patch[run] = createJsonPatchList(source[run], target[run]);
                // System.out.print("->" + patch[run].size() + " ");
                count += patch[run].size();
            }
        }
        double time = (System.nanoTime() - start) / 1e9;
        System.out.println("\nSetup runs [size=" + size + ", runs=" + RUNS + ", time=" + time + "s] => " + count);
    }

    private static void evaluate(String name, JsonNodeFactory factory, ObjectNode[] source, ObjectNode[] target,
            final List<JsonNode>[] patch, int runs) {
        final JsonNode[] update = new JsonNode[RUNS];
        System.out.print("Running " + name + " => ");
        long count = 0, start = System.nanoTime();
        for (int run = 0; run < runs; run++) {
            update[run] = source[run].deepCopy();
            for (int max = patch[run].size(), from = 0, to = Math.min(max, ITER); from < to; from =
                    Math.min(max, to), to = Math.min(max, to + ITER)) {
                update[run] = JsonPatch.apply(new ArrayNode(factory).addAll(patch[run].subList(from, to)), update[run]);
                System.out.print(".");
            }
            // System.out.print("->" + patch[run].size() + " ");
            count += patch[run].size();
        }
        double time = (System.nanoTime() - start) / 1e9;
        System.out.println("\nRun " + name + " [runs=" + runs + ", time=" + time + "s] => " + count);

        count = 0;
        start = System.nanoTime();
        // System.out.print("Validate " + name + " => ");
        for (int run = 0; run < runs; run++) {
            List<JsonNode> diffs = createJsonPatchList(update[run], target[run]);
            if (diffs.size() > 0) {
                throw new RuntimeException("not equals" +
                        "\n  source = " + source[run] +
                        "\n  update = " + update[run] +
                        "\n  target = " + target[run] +
                        "\n  patch = " + patch[run] +
                        "\n  diffs = " + diffs);
            }
            // System.out.print(".");
            count += diffs.size();
        }
        time = (System.nanoTime() - start) / 1e9;
        // System.out.println("\nValidate " + name + " [runs=" + runs + ", time=" + time + "s] => " + count);
    }

    private static ObjectNode createRandomTour(final Random random, final ObjectNode tour, int[] nums, int[] props) {
        createRandomProps(tour, random, "tour-prop", props[0]);
        final ArrayNode shipments = tour.putArray("shipments");
        for (int scount = 0; scount < nums[0]; scount++) {
            final ObjectNode shipment = createRandomProps(shipments.addObject(), random, "shipment-prop", props[1]);
            final ArrayNode items = shipment.putArray("items");
            for (int icount = 0; icount < ((nums[1] > 0) ? random.nextInt(nums[1]) + 1 : 0); icount++) {
                final ObjectNode item = createRandomProps(items.addObject(), random, "item-prop", props[2]);
                final ArrayNode prices = item.putArray("prices");
                int num = (nums[2] > 0) ? random.nextInt(nums[2]) : 0;
                for (int pcount = 0; pcount < num; pcount++) {
                    createRandomProps(prices.addObject(), random, "price-prop", props[3]);
                }
            }
        }
        return tour;
    }

    private static ObjectNode createRandomProps(final ObjectNode node, final Random random, final String name,
            int props) {
        for (int prop = 0; prop < random.nextInt(props); prop++) {
            node.put(createKey(name, prop), UUID.randomUUID().toString());
        }
        return node;
    }

    private static ObjectNode extendRandomTour(final Random random, final ObjectNode node,
            int[] nums, int[] props, int max) {
        int changes = extendRandomProps(node, random, "tour-prop", props[0]);
        final ArrayNode shipments = putArrayIfAbsent(node, "shipments");
        for (int scount = 0; scount < nums[0] && changes < max; scount++) {
            final ObjectNode shipment =
                    (scount < shipments.size()) ? (ObjectNode) shipments.get(scount) : shipments.addObject();
            changes += extendRandomProps(shipment, random, "shipment-prop", props[1]);
            final ArrayNode items = putArrayIfAbsent(shipment, "items");
            int snum = (nums[1] > 0) ? random.nextInt(nums[1]) + 1 : 0;
            for (int icount = 0; icount < snum && changes < max; icount++) {
                final ObjectNode item = (icount < items.size()) ? (ObjectNode) items.get(icount) : items.addObject();
                changes += extendRandomProps(item, random, "item-prop", props[2]);
                final ArrayNode prices = putArrayIfAbsent(item, "prices");
                int pnum = (nums[2] > 0) ? random.nextInt(nums[2]) : 0;
                for (int pcount = 0; pcount < pnum && changes < max; pcount++) {
                    ObjectNode price = (pcount < prices.size()) ? (ObjectNode) prices.get(pcount) : prices.addObject();
                    changes += extendRandomProps(price, random, "price-prop", props[3]);
                }
            }
        }
        return node;
    }

    private static int extendRandomProps(final ObjectNode node, final Random random, final String name,
            int props) {
        int size = node.size();
        for (int prop = 0; prop < random.nextInt(props); prop++) {
            String key = createKey(name, prop);
            if (!node.has(key)) {
                node.put(key, UUID.randomUUID().toString());
            }
        }
        return node.size() - size;
    }

    private static String createKey(final String name, int prop) {
        Map<String, Map<Integer, String>> keynamemap = KEYNAMEMAP;
        Map<Integer, String> keypropmap = keynamemap.get(name);
        if (keypropmap == null) {
            keypropmap = new HashMap<Integer, String>();
            keynamemap.put(name, keypropmap);
        }
        String key = keypropmap.get(prop);
        if (key == null) {
            key = String.format("%s-%02d", name, prop).intern();
            keypropmap.put(prop, key);
        }
        return key;
    }

    private static ArrayNode putArrayIfAbsent(final ObjectNode node, String fieldName) {
        return (node.has(fieldName) ? (ArrayNode) node.get(fieldName) : node.putArray(fieldName));
    }
}
