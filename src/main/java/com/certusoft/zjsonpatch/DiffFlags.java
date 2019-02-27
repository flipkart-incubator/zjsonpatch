package com.certusoft.zjsonpatch;

import java.util.EnumSet;

public enum DiffFlags {
    /**
     * This flag omits the <i>value</i> field on remove operations.
     * This is a default flag.
     */
    OMIT_VALUE_ON_REMOVE,

    /**
     * This flag omits all {@link Operation#MOVE} operations, leaving only
     * {@link Operation#ADD}, {@link Operation#REMOVE}, {@link Operation#REPLACE}
     * and {@link Operation#COPY} operations. In other words, without this flag,
     * {@link Operation#ADD} and {@link Operation#REMOVE} operations are not normalized
     * into {@link Operation#MOVE} operations.
     */
    OMIT_MOVE_OPERATION,

    /**
     * This flag omits all {@link Operation#COPY} operations, leaving only
     * {@link Operation#ADD}, {@link Operation#REMOVE}, {@link Operation#REPLACE}
     * and {@link Operation#MOVE} operations. In other words, without this flag,
     * {@link Operation#ADD} operations are not normalized into {@link Operation#COPY}
     * operations.
     */
    OMIT_COPY_OPERATION,

    /**
     * This flag adds a <i>fromValue</i> field to all {@link Operation#REPLACE}operations.
     * <i>fromValue</i> represents the value replaced by a {@link Operation#REPLACE}
     * operation, in other words, the original value.
     *
     * @since 0.4.1
     */
    ADD_ORIGINAL_VALUE_ON_REPLACE,

    /**
     * This flag adds a {@link Operation#LABEL}operation when a {@link Operation#REPLACE} occurs.
     * This value represents name of the object changed by the {@link Operation#REPLACE}
     * operation. This is useful for displaying diff data.
     *
     */
    INCLUDE_LABELS_OPERATION;


    public static EnumSet<DiffFlags> defaults() {
        return EnumSet.of(OMIT_VALUE_ON_REMOVE);
    }

    public static EnumSet<DiffFlags> dontNormalizeOpIntoMoveAndCopy() {
        return EnumSet.of(OMIT_MOVE_OPERATION, OMIT_COPY_OPERATION);
    }
}
