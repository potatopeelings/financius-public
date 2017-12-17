package com.code44.finance.ui.budgets.list;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.code44.finance.R;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Budget;
import com.code44.finance.data.providers.BudgetsProvider;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.ui.budgets.detail.BudgetActivity;
import com.code44.finance.ui.budgets.edit.BudgetEditActivity;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.adapters.ModelsAdapter;
import com.code44.finance.ui.common.presenters.ModelsActivityPresenter;
import com.code44.finance.ui.common.recycler.DividerDecoration;
import com.code44.finance.ui.common.recycler.SectionsDecoration;
import com.code44.finance.utils.EventBus;
import com.code44.finance.utils.interval.CurrentInterval;
import com.squareup.otto.Subscribe;

class BudgetsActivityPresenter extends ModelsActivityPresenter<Budget> {
    private final EventBus eventBus;
    private final AmountFormatter amountFormatter;
    private final CurrentInterval currentInterval;

    public BudgetsActivityPresenter(EventBus eventBus, AmountFormatter amountFormatter, CurrentInterval currentInterval) {
        this.eventBus = eventBus;
        this.amountFormatter = amountFormatter;
        this.currentInterval = currentInterval;
    }

    @Override public void onCreate(BaseActivity activity, Bundle savedInstanceState) {
        super.onCreate(activity, savedInstanceState);
        eventBus.register(this);
    }

    @Override public void onDestroy(BaseActivity activity) {
        eventBus.unregister(this);
        super.onDestroy(activity);
    }

    @Override public boolean onOptionsItemSelected(BaseActivity activity, MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(activity, item);
        }
    }

    @Override protected ModelsAdapter<Budget> createAdapter(ModelsAdapter.OnModelClickListener<Budget> defaultOnModelClickListener) {
        return new BudgetsAdapter(defaultOnModelClickListener, amountFormatter, currentInterval);
    }

    @Override protected CursorLoader getModelsCursorLoader(Context context) {
        return Tables.Budgets.getQuery().asCursorLoader(context, BudgetsProvider.uriBudgets());
    }

    @Override protected void onModelClick(Context context, View view, Budget model, Cursor cursor, int position) {
        BudgetActivity.start(context, model.getId());
    }

    @Override protected void startModelEdit(Context context, String modelId) {
        BudgetEditActivity.start(context, modelId);
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
}
