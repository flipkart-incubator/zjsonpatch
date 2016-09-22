package com.flipkart.zjsonpatch;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
enum Operation {
    ADD,
    REPLACE,
    REMOVE,
    MOVE,
    COPY,
    TEST;

    private final String name = this.name().toLowerCase().intern();

    public static Operation fromRfcName(String name) throws InvalidJsonPatchException {
        for (Operation  op : Operation.values()) {
            if (op.name.equalsIgnoreCase(name)) {
                return op;
            }
        }
        throw new InvalidJsonPatchException("unknown / unsupported operation " + name);
    }

    public String getName() {
        return this.name;
    }
}
