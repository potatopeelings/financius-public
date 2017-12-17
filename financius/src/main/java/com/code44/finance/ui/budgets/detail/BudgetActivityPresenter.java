package com.code44.finance.ui.budgets.detail;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Budget;
import com.code44.finance.data.model.Tag;
import com.code44.finance.data.providers.BudgetsProvider;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.ModelActivityPresenter;
import com.code44.finance.ui.budgets.edit.BudgetEditActivity;
import com.code44.finance.ui.dialogs.recurrencepicker.EventRecurrence;
import com.code44.finance.ui.dialogs.recurrencepicker.EventRecurrenceFormatter;
import com.code44.finance.utils.EventBus;
import com.code44.finance.utils.TextBackgroundSpan;
import com.code44.finance.utils.ThemeUtils;

class BudgetActivityPresenter extends ModelActivityPresenter<Budget> implements LoaderManager.LoaderCallbacks<Cursor> {
    private final AmountFormatter amountFormatter;
    private EventRecurrence eventRecurrence = new EventRecurrence();

    private TextView amountTextView;
    private TextView recurrenceTextView;
    private ImageView colorImageView;
    private TextView categoryTextView;
    private View tagsContainerView;
    private TextView tagsTextView;

    protected BudgetActivityPresenter(EventBus eventBus, AmountFormatter amountFormatter) {
        super(eventBus);
        this.amountFormatter = amountFormatter;
    }

    @Override public void onCreate(BaseActivity activity, Bundle savedInstanceState) {
        super.onCreate(activity, savedInstanceState);

        amountTextView = findView(activity, R.id.amountTextView);
        recurrenceTextView = findView(activity, R.id.recurrenceTextView);
        colorImageView = findView(activity, R.id.colorImageView);
        categoryTextView = findView(activity, R.id.categoryTextView);
        tagsContainerView = findView(activity, R.id.tagsContainerView);
        tagsTextView = findView(activity, R.id.tagsTextView);
    }

    @Override protected CursorLoader getModelCursorLoader(Context context, String modelId) {
        return Tables.Budgets.getQuery().asCursorLoader(context, BudgetsProvider.uriBudget(modelId));
    }

    @Override protected Budget getModelFrom(Cursor cursor) {
        return Budget.from(cursor);
    }

    @Override protected void onModelLoaded(Budget budget) {
        amountTextView.setText(amountFormatter.format(budget));

        long dateStart = budget.getDateStart();
        String rrule = budget.getRecurrence();
        eventRecurrence.parse(rrule);
        String repeatString = EventRecurrenceFormatter.getString(getActivity(), getActivity().getResources(), dateStart, eventRecurrence, true);
        recurrenceTextView.setText(repeatString);

        colorImageView.setColorFilter(budget.getCategory().getColor());
        categoryTextView.setText(budget.getCategory().getTitle());

        if (budget.getTags().size() > 0) {
            final int tagBackgroundColor = ThemeUtils.getColor(getActivity(), R.attr.backgroundColorSecondary);
            final float tagBackgroundRadius = getActivity().getResources().getDimension(R.dimen.tag_radius);
            final SpannableStringBuilder tags = new SpannableStringBuilder();
            for (Tag tag : budget.getTags()) {
                tags.append(tag.getTitle());
                tags.setSpan(new TextBackgroundSpan(tagBackgroundColor, tagBackgroundRadius), tags.length() - tag.getTitle().length(), tags.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tags.append(" ");
            }
            tagsContainerView.setVisibility(View.VISIBLE);
            tagsTextView.setText(tags);
        } else {
            tagsContainerView.setVisibility(View.GONE);
        }
    }

    @Override protected void startModelEdit(Context context, String modelId) {
        BudgetEditActivity.start(context, modelId);
    }

    @Override protected Uri getDeleteUri() {
        return BudgetsProvider.uriBudgets();
    }

    @Override protected Pair<String, String[]> getDeleteSelection(String modelId) {
        return Pair.create(Tables.Budgets.ID + "=?", new String[]{modelId});
    }
}
