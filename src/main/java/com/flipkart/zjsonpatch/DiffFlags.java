package com.flipkart.zjsonpatch;

import java.util.EnumSet;

public enum DiffFlags {
    OMIT_VALUE_ON_REMOVE,
    OMIT_MOVE_OPERATION, //only have ADD, REMOVE, REPLACE, COPY Don't normalize operations into MOVE
    OMIT_COPY_OPERATION, //only have ADD, REMOVE, REPLACE, MOVE, Don't normalize operations into COPY
    OMIT_ORIGINAL_VALUE_ON_REPLACE;
    
    public static EnumSet<DiffFlags> defaults() {
        return EnumSet.of(OMIT_VALUE_ON_REMOVE, OMIT_ORIGINAL_VALUE_ON_REPLACE);
    }

    public static EnumSet<DiffFlags> dontNormalizeOpIntoMoveAndCopy() {
        return EnumSet.of(OMIT_MOVE_OPERATION, OMIT_COPY_OPERATION);
    }
}
