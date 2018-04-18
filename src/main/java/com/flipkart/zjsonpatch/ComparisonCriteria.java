package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Comparator;

abstract class ComparisonCriteria implements Comparator<JsonNode> {
    static final ComparisonCriteria DEFAULT_CRITERIA = new ComparisonCriteria() {
        @Override
        public boolean equals(JsonNode firstNode, JsonNode secondNode) {
            return firstNode.equals(secondNode);
        }
    };

    /**
     * The criteria that will be used to determine <i>equality</i> between {@link JsonNode}s.
     * <p>
     * It is recommend that this method follow the same contract as {@link Object#equals(Object)}.
     *
     * @param firstNode  the first {@link JsonNode} to be compared.
     * @param secondNode the second {@link JsonNode} to be compared.
     * @return true if the described criteria is meet, false otherwise.
     */
    public abstract boolean equals(JsonNode firstNode, JsonNode secondNode);

    /**
     * This method as it is now, SHOULD NOT BE USED. It is only implemented as a way
     * to conform {@link JsonNode#equals(Comparator, JsonNode)} method contract.
     *
     * @return 0 if {@link ComparisonCriteria#equals(JsonNode, JsonNode)} is <i>true</i>
     * and -1 if <i>false</i>. 0 is used to conform {@link JsonNode#equals(Comparator, JsonNode)} contract,
     * -1 is used as a rogue value.
     */
    @Deprecated
    @Override
    public final int compare(JsonNode o1, JsonNode o2) {
        return equals(o1, o2) ? 0 : -1;
    }
}
