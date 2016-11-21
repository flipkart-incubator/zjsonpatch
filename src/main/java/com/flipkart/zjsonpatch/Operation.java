/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
