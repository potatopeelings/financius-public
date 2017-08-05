package com.code44.finance.ui.transactions.list;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.code44.finance.R;
import com.code44.finance.data.Query;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Tag;
import com.code44.finance.data.model.Transaction;
import com.code44.finance.data.providers.TransactionsProvider;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.adapters.ModelsAdapter;
import com.code44.finance.ui.common.presenters.ModelsActivityPresenter;
import com.code44.finance.ui.common.recycler.DividerDecoration;
import com.code44.finance.ui.common.recycler.SectionsDecoration;
import com.code44.finance.ui.transactions.detail.TransactionActivity;
import com.code44.finance.ui.transactions.edit.TransactionEditActivity;
import com.code44.finance.ui.transactions.filter.TransactionFilterActivity;
import com.code44.finance.utils.EventBus;
import com.code44.finance.utils.filter.TransactionFilter;
import com.code44.finance.utils.interval.CurrentInterval;
import com.squareup.otto.Subscribe;

class TransactionsActivityPresenter extends ModelsActivityPresenter<Transaction> {
    private static final int LOADER_TRANSACTIONS = 1;

    private final EventBus eventBus;
    private final AmountFormatter amountFormatter;
    private final CurrentInterval currentInterval;
    private final TransactionFilter transactionFilter;

    public TransactionsActivityPresenter(EventBus eventBus, AmountFormatter amountFormatter, CurrentInterval currentInterval, TransactionFilter transactionFilter) {
        this.eventBus = eventBus;
        this.amountFormatter = amountFormatter;
        this.currentInterval = currentInterval;
        this.transactionFilter = transactionFilter;
    }

    @Override public void onCreate(BaseActivity activity, Bundle savedInstanceState) {
        super.onCreate(activity, savedInstanceState);
        eventBus.register(this);
    }

    @Override public void onDestroy(BaseActivity activity) {
        eventBus.unregister(this);
        super.onDestroy(activity);
    }

    @Override public boolean onCreateOptionsMenu(BaseActivity activity, Menu menu) {
        super.onCreateOptionsMenu(activity, menu);
        activity.getMenuInflater().inflate(R.menu.transactions, menu);

        MenuItem newAction = menu.findItem(R.id.action_new);
        newAction.setVisible(!transactionFilter.isApplied());

        return true;
    }

    @Override public boolean onOptionsItemSelected(BaseActivity activity, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                TransactionFilterActivity.start(activity);
                return true;
            default:
                return super.onOptionsItemSelected(activity, item);
        }
    }

    @Override protected ModelsAdapter<Transaction> createAdapter(ModelsAdapter.OnModelClickListener<Transaction> defaultOnModelClickListener) {
        return new TransactionsAdapter(defaultOnModelClickListener, amountFormatter, currentInterval);
    }

    @Override protected CursorLoader getModelsCursorLoader(Context context) {
        Query query = Tables.Transactions.getQuery().clearSelection().clearArgs().selection(" 1 = 1 ");

        if (transactionFilter.isApplied()) {
            if (transactionFilter.getTransactionType() != null) {
                query = query.selection(" and " + Tables.Transactions.TYPE + " = ? ", transactionFilter.getTransactionType().asString());
            }
            if (transactionFilter.getAmountFrom() != null) {
                query = query.selection(" and " + Tables.Transactions.AMOUNT + " >= ?", transactionFilter.getAmountFrom().toString());
            }
            if (transactionFilter.getAmountTo() != null) {
                query = query.selection(" and " + Tables.Transactions.AMOUNT + " <= ?", transactionFilter.getAmountTo().toString());
            }
            if (transactionFilter.getDateFrom() != null) {
                query = query.selection(" and " + Tables.Transactions.DATE + " >= ?", transactionFilter.getDateFrom().toString());
            }
            if (transactionFilter.getDateTo() != null) {
                query = query.selection(" and " + Tables.Transactions.DATE + " <= ?", transactionFilter.getDateTo().toString());
            }
            if (transactionFilter.getAccountFrom() != null) {
                query = query.selection(" and " + Tables.Transactions.ACCOUNT_FROM_ID + " = ?", transactionFilter.getAccountFrom().getId());
            }
            if (transactionFilter.getAccountTo() != null) {
                query = query.selection(" and " + Tables.Transactions.ACCOUNT_TO_ID + " = ?", transactionFilter.getAccountTo().getId());
            }
            if (transactionFilter.getCategory() != null) {
                query = query.selection(" and " + Tables.Transactions.CATEGORY_ID + " = ?", transactionFilter.getCategory().getId());
            }
            if (transactionFilter.getNote() != null) {
                query = query.selection(" and lower(" + Tables.Transactions.NOTE + ") like '%' || ? || '%'", transactionFilter.getNote().toLowerCase());
            }
            if (transactionFilter.getTags().size() != 0) {
                query = query.havingSelection(" 1 = 1 ");
                for (Tag tag : transactionFilter.getTags()) {
                    query = query.havingSelection(" and '" + Tables.CONCAT_SEPARATOR + "' ||  group_concat(" + Tables.Tags.ID + ",'" + Tables.CONCAT_SEPARATOR + "') || '" + Tables.CONCAT_SEPARATOR + "' like " +
                                                  " '%" + Tables.CONCAT_SEPARATOR + tag.getId() + Tables.CONCAT_SEPARATOR + "%'");
                }
            }
        }

        return query.asCursorLoader(context, TransactionsProvider.uriTransactions());
    }

    @Override protected void onModelClick(Context context, View view, Transaction model, Cursor cursor, int position) {
        TransactionActivity.start(context, model.getId());
    }

    @Override protected void startModelEdit(Context context, String modelId) {
        TransactionEditActivity.start(context, modelId);
    }

    @Subscribe public void onIntervalChanged(CurrentInterval interval) {
        getAdapter().notifyDataSetChanged();
    }

    @Override protected RecyclerView.ItemDecoration[] getItemDecorations() {
        final Context context = getActivity();
        final RecyclerView.ItemDecoration dividerDecoration = new DividerDecoration(context).setPaddingLeft(context.getResources().getDimensionPixelSize(R.dimen.keyline_content));
        final RecyclerView.ItemDecoration sectionDecoration = new SectionsDecoration(true);
        return new RecyclerView.ItemDecoration[]{dividerDecoration, sectionDecoration};
    }

    @Subscribe public void onTransactionFilterChanged(TransactionFilter transactionFilter) {
        // TODO : there should be a simpler way to update the adapter data and refresh the recycler view
        getActivity().recreate();
    }
}
