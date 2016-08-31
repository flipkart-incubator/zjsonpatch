package com.flipkart.zjsonpatch;

import java.util.*;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
enum Operation {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move");

    private static final Map<String, Operation> opMap = new HashMap<>();
    static {
        opMap.put(ADD.rfcName, ADD);
        opMap.put(REMOVE.rfcName, REMOVE);
        opMap.put(REPLACE.rfcName, REPLACE);
        opMap.put(MOVE.rfcName, MOVE);
    }
    private final static Map<String, Operation> OPS = Collections.unmodifiableMap(opMap);

    private String rfcName;

    Operation(String rfcName) {
        this.rfcName = rfcName;
    }

    public static Operation fromRfcName(String rfcName) {
        Objects.requireNonNull(rfcName, "rfcName cannot be null");
        Operation op = OPS.get(rfcName.toLowerCase());
        return Objects.requireNonNull(op,
                () -> ("unknown / unsupported operation " + rfcName));
    }

    public String rfcName() {
        return this.rfcName;
    }


}
