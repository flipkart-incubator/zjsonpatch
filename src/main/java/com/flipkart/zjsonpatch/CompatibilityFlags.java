package com.flipkart.zjsonpatch;

import java.util.EnumSet;

/**
 * Created by tomerga on 04/09/2016.
 */
public enum CompatibilityFlags {
    /**
     * Represent nulls as missing values and vice versa.
     */
    MISSING_VALUES_AS_NULLS,

    /**
     * Allows to patch the JSON Object instead of applying the patched on a deep copy.
     */
    ENABLE_PATCH_IN_PLACE,

    /**
     * Disable optimization of patch creation.
     */
    DISABLE_PATCH_OPTIMIZATION;

    public static EnumSet<CompatibilityFlags> defaults() {
        return EnumSet.noneOf(CompatibilityFlags.class);
    }
}
