package com.flipkart.zjsonpatch.constants;

import java.util.EnumSet;

public enum DiffFlags {
    OMIT_VALUE_ON_REMOVE;

    public static EnumSet<DiffFlags> defaults() {
        return EnumSet.of(OMIT_VALUE_ON_REMOVE);
    }
}
