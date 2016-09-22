package com.flipkart.zjsonpatch;

import java.util.EnumSet;

/**
 * Created by tomerga on 04/09/2016.
 */
public enum CompatibilityFlags {
    PATCH_IN_PLACE,
    MISSING_VALUES_AS_NULLS,
    DISABLE_DIFF_OPTIMIZATION;

    public static EnumSet<CompatibilityFlags> defaults() {
        return EnumSet.noneOf(CompatibilityFlags.class);
    }
}
