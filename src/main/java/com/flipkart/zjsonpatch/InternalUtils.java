package com.flipkart.zjsonpatch;

import com.flipkart.zjsonpatch.mapping.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class InternalUtils {

    static List<JsonNodeWrapper> toList(ArrayNodeWrapper input) {
        int size = input.size();
        List<JsonNodeWrapper> toReturn = new ArrayList<JsonNodeWrapper>(size);
        for (int i = 0; i < size; i++) {
            toReturn.add(input.get(i));
        }
        return toReturn;
    }

    static List<JsonNodeWrapper> longestCommonSubsequence(final List<JsonNodeWrapper> a, final List<JsonNodeWrapper> b) {
        if (a == null || b == null) {
            throw new NullPointerException("List must not be null for longestCommonSubsequence");
        }

        List<JsonNodeWrapper> toReturn = new LinkedList<JsonNodeWrapper>();

        int aSize = a.size();
        int bSize = b.size();
        int temp[][] = new int[aSize + 1][bSize + 1];

        for (int i = 1; i <= aSize; i++) {
            for (int j = 1; j <= bSize; j++) {
                if (i == 0 || j == 0) {
                    temp[i][j] = 0;
                } else if (a.get(i - 1).equals(b.get(j - 1))) {
                    temp[i][j] = temp[i - 1][j - 1] + 1;
                } else {
                    temp[i][j] = Math.max(temp[i][j - 1], temp[i - 1][j]);
                }
            }
        }
        int i = aSize, j = bSize;
        while (i > 0 && j > 0) {
            if (a.get(i - 1).equals(b.get(j - 1))) {
                toReturn.add(a.get(i - 1));
                i--;
                j--;
            } else if (temp[i - 1][j] > temp[i][j - 1])
                i--;
            else
                j--;
        }
        Collections.reverse(toReturn);
        return toReturn;
    }
}
