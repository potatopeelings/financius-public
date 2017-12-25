package com.code44.finance.ui.reports.categories;

import android.content.Context;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.data.model.Tag;
import com.code44.finance.money.AmountFormatter;

import java.text.DecimalFormatSymbols;
import java.util.List;

public class CategoriesReportAdapter extends BaseAdapter {
    private final Context context;
    private final AmountFormatter amountFormatter;
    private CategoriesReportData categoriesReportData;
    private long totalExpenseAmount = 0;

    public CategoriesReportAdapter(Context context, AmountFormatter amountFormatter) {
        this.context = context;
        this.amountFormatter = amountFormatter;
    }

    @Override public int getCount() {
        return categoriesReportData != null ? categoriesReportData.size() : 0;
    }

    @Override public Object getItem(int position) {
        return categoriesReportData.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.li_category_report, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CategoriesReportData.CategoriesReportItem item = categoriesReportData.get(position);

        Long budget = item.getBudgetAmount();
        updateBudgetGraph(
            item.getExpenseAmount(), budget,
            item.getCategory().getColor(), holder.budgetContainer_LL,
            holder.budgetPercentage100Container_LL, holder.budgetPercentage100_LL, holder.budgetPercentage100Multiplier_TV,
            holder.budgetPercentage100ModuloContainer_LL, holder.budgetPercentage100Modulo_LL, holder.budgetPercentage100ModuloNegative_LL);
        if (budget != null) {
            holder.budget_TV.setText(amountFormatter.format(budget));
        }

        holder.color_IV.setColorFilter(item.getCategory().getColor());
        final int percent = getPercent(item.getExpenseAmount());
        holder.percent_TV.setText((percent == 0 ? "<1" : (percent == 100 && getCount() > 1 ? ">99" : percent)) + "%");
        holder.title_TV.setText(item.getCategory().getTitle());
        holder.amount_TV.setText(amountFormatter.format(item.getExpenseAmount()));
        bindTags((ViewGroup) convertView, item.getTags(), item.getCategory().getColor());

        return convertView;
    }

    public void setData(CategoriesReportData categoriesReportData, long totalExpenseAmount) {
        this.categoriesReportData = categoriesReportData;
        this.totalExpenseAmount = totalExpenseAmount;
        notifyDataSetChanged();
    }

    private int getPercent(long amount) {
        return Math.round(100.0f * amount / totalExpenseAmount);
    }

    private void bindTags(ViewGroup parent, List<Pair<Tag, Long[]>> tags, int color) {
        final int staticViewCount = 1;
        final int currentCount = parent.getChildCount() - staticViewCount;
        final int newCount = tags.size();
        if (newCount > currentCount) {
            for (int i = currentCount; i < newCount; i++) {
                final View view = LayoutInflater.from(context).inflate(R.layout.li_category_report_tag, parent, false);
                new TagViewHolder(view);
                parent.addView(view);
            }
        } else {
            parent.removeViews(staticViewCount, currentCount - newCount);
        }

        for (int i = staticViewCount, size = staticViewCount + tags.size(); i < size; i++) {
            final TagViewHolder holder = (TagViewHolder) parent.getChildAt(i).getTag();
            final Pair<Tag, Long[]> tagAmount = tags.get(i - staticViewCount);

            Long expense = tagAmount.second[CategoriesReportData.EXPENSE];
            if (expense == null) {
                expense = 0L;
            }
            Long budget = tagAmount.second[CategoriesReportData.BUDGET];
            updateBudgetGraph(expense, budget,
                color, holder.budgetContainer_LL,
                holder.budgetPercentage100Container_LL, holder.budgetPercentage100_LL, holder.budgetPercentage100Multiplier_TV,
                holder.budgetPercentage100ModuloContainer_LL, holder.budgetPercentage100Modulo_LL, holder.budgetPercentage100ModuloNegative_LL);

            holder.title_TV.setText(tagAmount.first.getTitle());
            holder.amount_TV.setText(amountFormatter.format((tagAmount.second[CategoriesReportData.EXPENSE] == null) ? 0 : tagAmount.second[CategoriesReportData.EXPENSE]));
        }
    }

    private void updateBudgetGraph(Long expense, Long budget,
                                   int color, LinearLayout budgetContainer,
                                   LinearLayout budgetPercentage100Container, LinearLayout budgetPercentage100, TextView budgetPercentage100Multiplier,
                                   LinearLayout budgetPercentage100ModuloContainer, LinearLayout budgetPercentage100Modulo, LinearLayout budgetPercentage100ModuloNegative) {
        budgetContainer.setVisibility(View.GONE);
        budgetPercentage100Container.setVisibility(View.GONE);
        budgetPercentage100Multiplier.setVisibility(View.INVISIBLE);
        budgetPercentage100Multiplier.setText(null);
        if (budget != null) {
            budgetContainer.setVisibility(View.VISIBLE);

            String multiplier = null;
            if (budget != 0) {
                double percentage = (double)expense / (double)budget;
                if (percentage > 1) {
                    if ((int)percentage == 1) {
                        // if not specified, a multiplier of 1 is implied
                        multiplier = "";
                    } else {
                        multiplier = context.getString(R.string.multiply) + " " + Integer.toString((int) percentage);
                    }
                }
                budgetPercentage100ModuloContainer.setVisibility(View.VISIBLE);
                budgetPercentage100Modulo.setBackgroundColor(color);
                // set height based on modulo percentage
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)budgetPercentage100Modulo.getLayoutParams();
                LinearLayout.LayoutParams layoutParamsNegative = (LinearLayout.LayoutParams)budgetPercentage100ModuloNegative.getLayoutParams();
                layoutParams.weight = (percentage == 1) ? 1 : (float)(percentage - (int)percentage);
                layoutParamsNegative.weight = 1 - layoutParams.weight;
                budgetPercentage100ModuloContainer.requestLayout();
            }
            else {
                budgetPercentage100ModuloContainer.setVisibility(View.GONE);
                multiplier = context.getString(R.string.multiply) + " " + DecimalFormatSymbols.getInstance().getInfinity();
            }

            if (multiplier != null) {
                budgetPercentage100Container.setVisibility(View.VISIBLE);
                budgetPercentage100.setBackgroundColor(color);
                budgetPercentage100Multiplier.setVisibility(View.VISIBLE);
                budgetPercentage100Multiplier.setText(multiplier);
            }
        }
    }

    private static final class ViewHolder {
        public final LinearLayout budgetContainer_LL;
        public final LinearLayout budgetPercentage100Container_LL;
        public final LinearLayout budgetPercentage100_LL;
        public final TextView budgetPercentage100Multiplier_TV;
        public final LinearLayout budgetPercentage100ModuloContainer_LL;
        public final LinearLayout budgetPercentage100Modulo_LL;
        public final LinearLayout budgetPercentage100ModuloNegative_LL;
        public final TextView budget_TV;
        public final ImageView color_IV;
        public final TextView percent_TV;
        public final TextView title_TV;
        public final TextView amount_TV;

        public ViewHolder(View view) {
            budgetContainer_LL = (LinearLayout) view.findViewById(R.id.budgetContainer);
            budgetPercentage100Container_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100Container);
            budgetPercentage100_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100);
            budgetPercentage100Multiplier_TV = (TextView) view.findViewById(R.id.budgetPercentage100Multiplier);
            budgetPercentage100ModuloContainer_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100ModuloContainer);
            budgetPercentage100Modulo_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100Modulo);
            budgetPercentage100ModuloNegative_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100ModuloNegative);
            budget_TV = (TextView) view.findViewById(R.id.budgetAmountTextView);
            color_IV = (ImageView) view.findViewById(R.id.colorImageView);
            percent_TV = (TextView) view.findViewById(R.id.percent_TV);
            title_TV = (TextView) view.findViewById(R.id.titleTextView);
            amount_TV = (TextView) view.findViewById(R.id.amountTextView);
            view.setTag(this);
        }
    }

    private static final class TagViewHolder {
        public final LinearLayout budgetContainer_LL;
        public final LinearLayout budgetPercentage100Container_LL;
        public final LinearLayout budgetPercentage100_LL;
        public final TextView budgetPercentage100Multiplier_TV;
        public final LinearLayout budgetPercentage100ModuloContainer_LL;
        public final LinearLayout budgetPercentage100Modulo_LL;
        public final LinearLayout budgetPercentage100ModuloNegative_LL;
        public final TextView title_TV;
        public final TextView amount_TV;

        public TagViewHolder(View view) {
            budgetContainer_LL = (LinearLayout) view.findViewById(R.id.budgetContainer);
            budgetPercentage100Container_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100Container);
            budgetPercentage100_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100);
            budgetPercentage100Multiplier_TV = (TextView) view.findViewById(R.id.budgetPercentage100Multiplier);
            budgetPercentage100ModuloContainer_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100ModuloContainer);
            budgetPercentage100Modulo_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100Modulo);
            budgetPercentage100ModuloNegative_LL = (LinearLayout) view.findViewById(R.id.budgetPercentage100ModuloNegative);
            title_TV = (TextView) view.findViewById(R.id.titleTextView);
            amount_TV = (TextView) view.findViewById(R.id.amountTextView);
            view.setTag(this);
        }
    }
}
