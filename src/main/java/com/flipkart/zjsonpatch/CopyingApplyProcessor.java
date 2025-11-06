package com.flipkart.zjsonpatch;

import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;

import java.util.EnumSet;

class CopyingApplyProcessor extends InPlaceApplyProcessor {

    CopyingApplyProcessor(JsonNodeWrapper target) {
        this(target, CompatibilityFlags.defaults());
    }

     CopyingApplyProcessor(JsonNodeWrapper target, EnumSet<CompatibilityFlags> flags) {
        super(target.deepCopy(), flags);
    }

    @Override
    protected boolean allowRootReplacement() {
        return true;
    }
}
