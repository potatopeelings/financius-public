package com.code44.finance.ui.budgets.list;

import android.app.Activity;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.data.model.Budget;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Tag;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.ui.common.adapters.ModelsAdapter;
import com.code44.finance.ui.common.presenters.ModelsActivityPresenter;
import com.code44.finance.ui.common.recycler.SectionsDecoration;
import com.code44.finance.ui.dialogs.recurrencepicker.EventRecurrence;
import com.code44.finance.ui.dialogs.recurrencepicker.EventRecurrenceFormatter;
import com.code44.finance.utils.TextBackgroundSpan;
import com.code44.finance.utils.ThemeUtils;
import com.code44.finance.utils.interval.BaseInterval;

class BudgetsAdapter extends ModelsAdapter<Budget> implements SectionsDecoration.Adapter<BudgetsAdapter.HeaderViewHolder> {
    private final AmountFormatter amountFormatter;
    private final BaseInterval interval;

    public BudgetsAdapter(OnModelClickListener<Budget> onModelClickListener, AmountFormatter amountFormatter, BaseInterval interval) {
        super(onModelClickListener);
        this.amountFormatter = amountFormatter;
        this.interval = interval;
    }

    @Override protected ModelViewHolder<Budget> createModelViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.li_budget, parent, false), amountFormatter);
    }

    @Override protected Budget modelFromCursor(Cursor cursor) {
        return Budget.from(cursor);
    }

    @Override public long getHeaderId(int position) {
        getCursor().moveToPosition(position);
        Budget budget = Budget.from(getCursor());
        // this way, we can work around hash collisions by changing the color and have the same color
        return budget.getCategory().getId().hashCode() + budget.getCategory().getColor();
    }

    @Override public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.li_budget_header, parent, false));
    }

    @Override public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
        getCursor().moveToPosition(position);
        viewHolder.bind(getCursor(), interval);
    }

    private static class ViewHolder extends ModelViewHolder<Budget> {
        private final AmountFormatter amountFormatter;
        private final int tagBackgroundColor;
        private final float tagBackgroundRadius;
        private final View itemView;

        private final TextView recurrenceTextView;
        private final TextView amountTextView;
        private final TextView tagsTextView;

        private EventRecurrence eventRecurrence = new EventRecurrence();

        public ViewHolder(View itemView, AmountFormatter amountFormatter) {
            super(itemView);
            this.itemView = itemView;
            this.amountFormatter = amountFormatter;

            tagBackgroundColor = ThemeUtils.getColor(itemView.getContext(), R.attr.backgroundColorSecondary);
            tagBackgroundRadius = itemView.getContext().getResources().getDimension(R.dimen.tag_radius);

            recurrenceTextView = (TextView) itemView.findViewById(R.id.recurrenceTextView);
            amountTextView = (TextView) itemView.findViewById(R.id.amountTextView);
            tagsTextView = (TextView) itemView.findViewById(R.id.tagsTextView);
        }

        @Override protected void bind(Budget budget, Cursor cursor, int position, ModelsActivityPresenter.Mode mode, boolean isSelected) {
            // Set values
            String recurrenceTitle = getRecurrenceTitle(budget);
            recurrenceTextView.setText(recurrenceTitle);
            amountTextView.setText(amountFormatter.format(budget));
            CharSequence tagsTitle = getTagsTitle(budget);
            tagsTextView.setText(tagsTitle);
            tagsTextView.setVisibility(tagsTitle == null ? View.GONE : View.VISIBLE);
        }

        private String getRecurrenceTitle(Budget budget) {
            long dateStart = budget.getDateStart();
            String rrule = budget.getRecurrence();
            eventRecurrence.parse(rrule);
            String repeatString = EventRecurrenceFormatter.getString((Activity) itemView.getContext(), itemView.getResources(), dateStart, eventRecurrence, true);
            return repeatString;
        }

        private CharSequence getTagsTitle(Budget budget) {
            if (budget.getTags().size() > 0) {
                final SpannableStringBuilder subtitle = new SpannableStringBuilder();
                for (Tag tag : budget.getTags()) {
                    subtitle.append(tag.getTitle());
                    subtitle.setSpan(new TextBackgroundSpan(tagBackgroundColor, tagBackgroundRadius), subtitle.length() - tag.getTitle().length(), subtitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subtitle.append(" ");
                }
                return subtitle;
            }
            return null;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView title_TV;
        public ImageView color_IV;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            title_TV = (TextView) itemView.findViewById(R.id.titleTextView);
            color_IV = (ImageView) itemView.findViewById(R.id.colorImageView);
        }

        public void bind(Cursor cursor, BaseInterval baseInterval) {
            final String title;
            Budget budget = Budget.from(cursor);
            Category category = budget.getCategory();
            title = category.getTitle();
            title_TV.setText(title);
            color_IV.setColorFilter(category.getColor());
        }
    }
}
