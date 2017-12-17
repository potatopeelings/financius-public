package com.code44.finance.ui.reports.categories;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.Pair;
import android.text.format.Time;

import com.code44.finance.R;
import com.code44.finance.common.model.TransactionState;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.model.Budget;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Tag;
import com.code44.finance.data.model.Transaction;
import com.code44.finance.graphs.pie.PieChartData;
import com.code44.finance.graphs.pie.PieChartValue;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.utils.Logger;
import com.code44.finance.utils.ThemeUtils;
import com.code44.finance.utils.recurrence.DateException;
import com.code44.finance.utils.recurrence.EventRecurrence;
import com.code44.finance.utils.recurrence.RecurrenceProcessor;

import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class CategoriesReportData {
    public static final int EXPENSE = 0;
    public static final int BUDGET = 1;

    private final Logger logger = Logger.with(CategoriesReportData.class.getSimpleName());
    private final PieChartData pieChartData;
    private final List<CategoriesReportItem> categoriesReportItems;

    public CategoriesReportData(Context context, Cursor transactionsCursor, Cursor budgetsCursor, Interval interval, CurrenciesManager currenciesManager, TransactionType transactionType) {
        final Map<Category, Long[]> categoryExpensesBudgets = new HashMap<>();
        final Map<Category, Map<Tag, Long[]>> categoryTagExpensesBudgets = new HashMap<>();

        if (transactionsCursor.moveToFirst()) {
            final Category noCategory = createNoCategory(context, transactionType);
            do {
                final Transaction transaction = Transaction.from(transactionsCursor);
                if (isTransactionValid(transaction, transactionType)) {
                    final Long amount = getAmount(transaction, currenciesManager);
                    final Category category = transaction.getCategory() == null ? noCategory : transaction.getCategory();
                    increaseCategoryAmount(EXPENSE, categoryExpensesBudgets, category, amount);
                    increaseCategoryTagsAmount(EXPENSE, categoryTagExpensesBudgets, transaction.getTags(), category, amount);
                }
            } while (transactionsCursor.moveToNext());
        }

        if (budgetsCursor != null && budgetsCursor.moveToFirst()) {
            do {
                final Budget budget = Budget.from(budgetsCursor);
                long amount = budget.getAmount();
                long startDate = budget.getDateStart();
                String rrule = budget.getRecurrence();
                Category category = budget.getCategory();
                if (rrule == null) {
                    if (startDate >= interval.getStartMillis()) {
                        increaseCategoryAmount(BUDGET, categoryExpensesBudgets, category, amount);
                        increaseCategoryTagsAmount(BUDGET, categoryTagExpensesBudgets, budget.getTags(), category, amount);
                    }
                } else {
                    RecurrenceProcessor recurrenceProcessor = new RecurrenceProcessor();
                    EventRecurrence eventRecurrence = new EventRecurrence();
                    eventRecurrence.parse(rrule);
                    Time startTime = new Time();
                    startTime.set(startDate);
                    TreeSet<Long> instances = new TreeSet<Long>();
                    try {
                        recurrenceProcessor.expand(startTime, eventRecurrence, interval.getStart().getMillis(), interval.getEnd().getMillis() + 1, true, instances);
                        if (instances.size() != 0) {
                            increaseCategoryAmount(BUDGET, categoryExpensesBudgets, category, amount * instances.size());
                            increaseCategoryTagsAmount(BUDGET, categoryTagExpensesBudgets, budget.getTags(), category, amount * instances.size());
                        }
                    } catch (DateException ex) {
                        logger.error("Error expanding " + rrule);
                    }
                }
            } while (budgetsCursor.moveToNext());
        }

        final List<Pair<Category, Long[]>> sortedExpenses = new ArrayList<>();
        for (Category category : categoryExpensesBudgets.keySet()) {
            sortedExpenses.add(Pair.create(category, categoryExpensesBudgets.get(category)));
        }
        Collections.sort(sortedExpenses, new CategoryExpenseComparator());

        categoriesReportItems = new ArrayList<>();
        final PieChartData.Builder builder = PieChartData.builder();
        Long budgetTotalValue = null;
        for (Pair<Category, Long[]> category : sortedExpenses) {
            final Long expenseAmount = category.second[0];
            final Long budgetAmount = category.second[1];
            if (expenseAmount != null) {
                builder.addExpenseValues(new PieChartValue(expenseAmount, category.first.getColor()));
            }
            if (budgetAmount != null) {
                if (budgetTotalValue == null) {
                    budgetTotalValue = 0L;
                }
                budgetTotalValue += budgetAmount;
            }

            final List<Pair<Tag, Long[]>> tags;
            final Map<Tag, Long[]> tagExpensesBudgets = categoryTagExpensesBudgets.get(category.first);
            if (tagExpensesBudgets != null) {
                tags = new ArrayList<>();
                for (Tag tag : tagExpensesBudgets.keySet()) {
                    tags.add(Pair.create(tag, tagExpensesBudgets.get(tag)));
                }
                Collections.sort(tags, new TagExpenseComparator());
            } else {
                tags = Collections.emptyList();
            }

            categoriesReportItems.add(new CategoriesReportItem(category.first, category.second, tags));
        }
        builder.setBudgetTotalValue(budgetTotalValue);
        pieChartData = builder.build();
    }

    public PieChartData getPieChartData() {
        return pieChartData;
    }

    public int size() {
        return categoriesReportItems.size();
    }

    public CategoriesReportItem get(int position) {
        return categoriesReportItems.get(position);
    }

    private Category createNoCategory(Context context, TransactionType transactionType) {
        final Category noCategory = new Category();
        noCategory.setId("0");
        noCategory.setTitle(context.getString(R.string.no_category));
        noCategory.setColor(ThemeUtils.getColor(context, transactionType == TransactionType.Expense ? R.attr.textColorNegative : R.attr.textColorPositive));
        return noCategory;
    }

    private boolean isTransactionValid(Transaction transaction, TransactionType transactionType) {
        return transaction.includeInReports() && transaction.getTransactionType() == transactionType && transaction.getTransactionState() == TransactionState.Confirmed;
    }

    private Long getAmount(Transaction transaction, CurrenciesManager currenciesManager) {
        final String currencyCode = transaction.getTransactionType() == TransactionType.Expense ? transaction.getAccountFrom().getCurrencyCode() : transaction.getAccountTo().getCurrencyCode();
        if (currenciesManager.isMainCurrency(currencyCode)) {
            return transaction.getAmount();
        } else {
            return Math.round(transaction.getAmount() * currenciesManager.getExchangeRate(currencyCode, currenciesManager.getMainCurrencyCode()));
        }
    }

    private void increaseCategoryAmount(int index, Map<Category, Long[]> categoryAmounts, Category category, Long amount) {
        Long[] totalAmountForCategory = categoryAmounts.get(category);
        if (totalAmountForCategory == null) {
            Long[] amounts = new Long[] { null, null };
            amounts[index] = amount;
            categoryAmounts.put(category, amounts);
        } else if (totalAmountForCategory[index] == null) {
            totalAmountForCategory[index] = amount;
        } else {
            totalAmountForCategory[index] += amount;
        }
    }

    private void increaseCategoryTagsAmount(int index, Map<Category, Map<Tag, Long[]>> categoryTagAmounts, List<Tag> tags, Category category, Long amount) {
        for (Tag tag : tags) {
            Map<Tag, Long[]> tagsAmounts = categoryTagAmounts.get(category);
            Long[] tagAmounts;
            if (tagsAmounts == null) {
                tagsAmounts = new HashMap<>();
                tagAmounts = new Long[] { null, null };
                tagAmounts[index] = amount;
            } else {
                tagAmounts = tagsAmounts.get(tag);
                if (tagAmounts == null) {
                    tagAmounts = new Long[] { null, null };
                    tagAmounts[index] = 0L;
                }
                else if (tagAmounts[index] == null) {
                    tagAmounts[index] = 0L;
                }
                tagAmounts[index] += amount;
            }
            tagsAmounts.put(tag, tagAmounts);
            categoryTagAmounts.put(category, tagsAmounts);
        }
    }

    public static class CategoriesReportItem {
        private final Category category;
        private final Long[] amount;
        private final List<Pair<Tag, Long[]>> tags;

        public CategoriesReportItem(Category category, Long[] amount, List<Pair<Tag, Long[]>> tags) {
            this.category = category;
            this.amount = amount;
            this.tags = tags;
        }

        public Category getCategory() {
            return category;
        }

        public long getExpenseAmount() {
            return (amount[0] == null) ? 0 : amount[0];
        }

        public Long getBudgetAmount() {
            return amount[1];
        }

        public List<Pair<Tag, Long[]>> getTags() {
            return tags;
        }
    }
}
