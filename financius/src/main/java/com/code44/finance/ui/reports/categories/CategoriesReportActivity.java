package com.code44.finance.ui.reports.categories;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.ListView;

import com.code44.finance.R;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.providers.BudgetsProvider;
import com.code44.finance.data.providers.TransactionsProvider;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.ui.common.navigation.NavigationScreen;
import com.code44.finance.ui.reports.BaseReportActivity;
import com.code44.finance.utils.analytics.Analytics;
import com.code44.finance.utils.interval.ActiveInterval;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class CategoriesReportActivity extends BaseReportActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_TRANSACTIONS = 1;
    private static final int LOADER_BUDGETS = 2;

    @Inject ActiveInterval activeInterval;
    @Inject CurrenciesManager currenciesManager;
    @Inject AmountFormatter amountFormatter;

    private CategoriesReportView categoriesReportView;

    private CategoriesReportAdapter adapter;
    private TransactionType transactionType = TransactionType.Expense;

    private Cursor transactionsCursor;
    private Cursor budgetsCursor;

    public static Intent makeIntent(Context context) {
        return makeIntentForActivity(context, CategoriesReportActivity.class);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShowDrawer(true);
        setShowDrawerToggle(true);
        setContentView(R.layout.activity_categories_report);

        // Get views
        categoriesReportView = (CategoriesReportView) findViewById(R.id.categoriesReportView);
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Setup
        adapter = new CategoriesReportAdapter(this, amountFormatter);
        listView.setAdapter(adapter);
    }

    @Override protected NavigationScreen getNavigationScreen() {
        return NavigationScreen.Reports;
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.CategoriesReport;
    }

    @Override public void onResume() {
        super.onResume();
        getEventBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        getEventBus().unregister(this);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_TRANSACTIONS:
                return Tables.Transactions
                        .getQuery()
                        .selection(" and " + Tables.Transactions.DATE + " between ? and ?", String.valueOf(activeInterval.getInterval().getStartMillis()), String.valueOf(activeInterval.getInterval().getEndMillis() - 1))
                        .asCursorLoader(this, TransactionsProvider.uriTransactions());
            case LOADER_BUDGETS:
                return Tables.Budgets
                        .getQuery()
                        .selection(" and " + Tables.Budgets.DATE_START + " <= ?", String.valueOf(activeInterval.getInterval().getEndMillis()))
                        .asCursorLoader(this, BudgetsProvider.uriBudgets());
        }
        return null;
    }

    @Override public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_TRANSACTIONS:
                transactionsCursor = cursor;
                break;
            case LOADER_BUDGETS:
                budgetsCursor = cursor;
                break;
        }

        if (transactionsCursor != null && budgetsCursor != null) {
            onTransactionsAndBudgetsLoaded(transactionsCursor, budgetsCursor);
        }
    }

    @Override public void onLoaderReset(final Loader<Cursor> loader) {
    }

    @Subscribe public void onActiveIntervalChanged(ActiveInterval interval) {
        transactionsCursor = null;
        budgetsCursor = null;
        getSupportLoaderManager().restartLoader(LOADER_TRANSACTIONS, null, this);
        getSupportLoaderManager().restartLoader(LOADER_BUDGETS, null, this);
    }

    private void onTransactionsAndBudgetsLoaded(Cursor transactionsCursor, Cursor budgetsCursor) {
        final CategoriesReportData categoriesReportData = new CategoriesReportData(this, transactionsCursor, budgetsCursor, activeInterval.getInterval(), currenciesManager, transactionType);
        categoriesReportView.setPieChartData(categoriesReportData.getPieChartData());
        categoriesReportView.setTotalBudget(categoriesReportData.getPieChartData().getTotalBudgetValue());
        categoriesReportView.setTotalExpense(categoriesReportData.getPieChartData().getTotalExpenseValue());
        adapter.setData(categoriesReportData, categoriesReportData.getPieChartData().getTotalExpenseValue());
    }
}
