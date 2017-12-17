package com.code44.finance.ui.reports.categories;

import android.support.v4.util.Pair;

import com.code44.finance.data.model.Category;

import java.util.Comparator;

class CategoryExpenseComparator implements Comparator<Pair<Category, Long[]>> {
    @Override public int compare(Pair<Category, Long[]> leftValue, Pair<Category, Long[]> rightValue) {
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
