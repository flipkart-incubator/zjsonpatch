package com.flipkart.zjsonpatch;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

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

    public static Operation fromRfcName(String rfcName) {
        checkNotNull(rfcName, "rfcName cannot be null");
        return checkNotNull(OPS.get(rfcName.toLowerCase()), "unknown / unsupported operation %s", rfcName);
    }

    public String rfcName() {
        return this.rfcName;
    }


}
