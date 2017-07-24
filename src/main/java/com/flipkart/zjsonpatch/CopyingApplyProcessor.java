package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

class CopyingApplyProcessor extends InPlaceApplyProcessor {

    CopyingApplyProcessor(JsonNode target) {
        super(target.deepCopy());
    }
}
