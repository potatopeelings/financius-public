package com.code44.finance.ui.reports.categories;

import android.support.v4.util.Pair;

import com.code44.finance.data.model.Tag;

import java.util.Comparator;

class TagExpenseComparator implements Comparator<Pair<Tag, Long[]>> {
    @Override public int compare(Pair<Tag, Long[]> leftValue, Pair<Tag, Long[]> rightValue) {
        if (leftValue.second[0] == null && rightValue.second[0] != null) {
            return 1;
        } else if (leftValue.second[0] != null && rightValue.second[0] == null) {
            return -1;
        } else if (leftValue.second[0] == null && rightValue.second[0] == null) {
            return 0;
        } else if (leftValue.second[0] < rightValue.second[0]) {
            return 1;
        } else if (leftValue.second[0] > rightValue.second[0]) {
            return -1;
        } else {
            return 0;
        }
    }
}
