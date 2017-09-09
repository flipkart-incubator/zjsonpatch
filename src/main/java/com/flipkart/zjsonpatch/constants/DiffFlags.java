package com.flipkart.zjsonpatch.constants;

import java.util.Arrays;
import java.util.EnumSet;

public enum DiffFlags {
	OMIT_VALUE_ON_REMOVE,
	OMIT_MOVE_OPERATION, // only have ADD, REMOVE, REPLACE, COPY Don't normalize operations into MOVE
	OMIT_COPY_OPERATION; // only have ADD, REMOVE, REPLACE, MOVE, Don't normalize operations into COPY

	public static EnumSet<DiffFlags> defaults() {
		return EnumSet.of(OMIT_VALUE_ON_REMOVE);
	}

	public static EnumSet<DiffFlags> dontNormalizeOpIntoMoveAndCopy() {
		return EnumSet.copyOf(Arrays.asList(OMIT_MOVE_OPERATION, OMIT_COPY_OPERATION));
	}
}
