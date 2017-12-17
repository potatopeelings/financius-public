package com.code44.finance.graphs.pie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PieChartData {
    private final List<PieChartValue> expenseValues;
    private final Long totalBudgetValue;
    private final long totalExpenseValue;

    private PieChartData(List<PieChartValue> expenseValues, Long totalBudgetValue) {
        this.expenseValues = expenseValues;

        long totalExpenseValue = 0;
        for (PieChartValue expenseValue : expenseValues) {
            totalExpenseValue += expenseValue.getValue();
        }
        this.totalBudgetValue = totalBudgetValue;
        this.totalExpenseValue = totalExpenseValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<PieChartValue> getExpenseValues() {
        return expenseValues;
    }

    public Long getTotalBudgetValue() {
        return totalBudgetValue;
    }

    public long getTotalExpenseValue() {
        return totalExpenseValue;
    }

    public static class Builder {
        private Long budgetTotalValue;
        private List<PieChartValue> expenseValues;

        public Builder() {
        }

        public Builder setExpenseValues(List<PieChartValue> expenseValues) {
            this.expenseValues = expenseValues;
            return this;
        }

        public void setBudgetTotalValue(Long budgetTotalValue) {
            this.budgetTotalValue = budgetTotalValue;
        }

        public Builder addExpenseValues(PieChartValue value) {
            if (expenseValues == null) {
                expenseValues = new ArrayList<>();
            }

            expenseValues.add(value);
            return this;
        }

        public PieChartData build() {
            ensureSaneDefaults();
            return new PieChartData(expenseValues, budgetTotalValue);
        }

        private void ensureSaneDefaults() {
            if (expenseValues == null) {
                expenseValues = Collections.emptyList();
            }
        }

    }
}
