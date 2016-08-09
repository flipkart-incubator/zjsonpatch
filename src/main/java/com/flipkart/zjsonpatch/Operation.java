package com.flipkart.zjsonpatch;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
enum Operation {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move");

    private final static Map<String, Operation> OPS = ImmutableMap.of(
            ADD.rfcName, ADD,
            REMOVE.rfcName, REMOVE,
            REPLACE.rfcName, REPLACE,
            MOVE.rfcName, MOVE
            );

    private String rfcName;

    Operation(String rfcName) {
        this.rfcName = rfcName;
    }

    public static Operation fromRfcName(String rfcName) throws InvalidJsonPatchException {
        if (rfcName == null) throw new InvalidJsonPatchException("rfcName cannot be null");
        Operation op = OPS.get(rfcName.toLowerCase());
        if (op == null) throw new InvalidJsonPatchException("unknown / unsupported operation " + rfcName);
        return op;
    }

    public String rfcName() {
        return this.rfcName;
    }


}
