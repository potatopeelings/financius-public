package com.code44.finance.ui.reports.categories;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.code44.finance.App;
import com.code44.finance.R;
import com.code44.finance.graphs.pie.PieChartData;
import com.code44.finance.graphs.pie.PieChartView;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.ui.common.ViewBackgroundTheme;
import com.code44.finance.utils.ThemeUtils;

import javax.inject.Inject;

public class CategoriesReportView extends LinearLayout {
    private final PieChartView pieChartView;
    private final TextView totalBudgetTextView;
    private final TextView totalExpenseTextView;

    @Inject AmountFormatter amountFormatter;

    public CategoriesReportView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CategoriesReportView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER_VERTICAL);
        final int padding = getResources().getDimensionPixelSize(R.dimen.keyline);
        setPadding(padding, padding, padding, padding);
        inflate(context, R.layout.view_categories_report, this);
        if (!isInEditMode()) {
            App.with(context).inject(this);
        }

        // Get views
        pieChartView = (PieChartView) findViewById(R.id.pieChartView);
        totalBudgetTextView = (TextView) findViewById(R.id.totalBudgetTextView);
        totalExpenseTextView = (TextView) findViewById(R.id.totalExpenseTextView);

        // Setup
        applyStyle(context, attrs);
        setPieChartData(null);
        if (isInEditMode()) {
            totalBudgetTextView.setText("0.00 $");
            totalExpenseTextView.setText("0.00 $");
        } else {
            setTotalBudget(0L);
            setTotalExpense(0);
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final LayoutParams params = (LayoutParams) pieChartView.getLayoutParams();
        params.height = pieChartView.getMeasuredWidth();
    }

    public void setPieChartData(PieChartData pieChartData) {
        pieChartView.setPieChartData(pieChartData);
    }

    public void setTotalBudget(Long totalBudget) {
        if (totalBudget != null) {
            totalBudgetTextView.setVisibility(VISIBLE);
            totalBudgetTextView.setText(getContext().getString(R.string.budgets_one) + ": " + amountFormatter.format(totalBudget));
        }
        else {
            totalBudgetTextView.setVisibility(GONE);
        }
    }

    public void setTotalExpense(long totalExpense) {
        totalExpenseTextView.setText(amountFormatter.format(totalExpense));
    }

    private void applyStyle(Context context, AttributeSet attrs) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieChartView, 0, 0);
        try {
            final ViewBackgroundTheme viewBackgroundTheme = ViewBackgroundTheme.from(a.getInteger(R.styleable.CategoriesReportView_viewBackgroundTheme, 0));
            pieChartView.setViewBackgroundTheme(viewBackgroundTheme);
            if (getOrientation() == HORIZONTAL) {
                pieChartView.setSizeBasedOn(PieChartView.SizeBasedOn.Height);
            } else {
                MarginLayoutParams params = (MarginLayoutParams) pieChartView.getLayoutParams();
                params.leftMargin = params.rightMargin = getResources().getDimensionPixelSize(R.dimen.space_xlarge);
                pieChartView.setSizeBasedOn(PieChartView.SizeBasedOn.Width);
            }

            final int textColor = ThemeUtils.getColor(getContext(), viewBackgroundTheme == ViewBackgroundTheme.Light ? android.R.attr.textColorPrimary : android.R.attr.textColorPrimaryInverse);
            totalBudgetTextView.setTextColor(textColor);
            totalExpenseTextView.setTextColor(textColor);
        } finally {
            a.recycle();
        }
    }
}
