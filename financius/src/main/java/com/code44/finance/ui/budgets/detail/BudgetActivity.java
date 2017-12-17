package com.code44.finance.ui.budgets.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.utils.analytics.Analytics;

import javax.inject.Inject;

public class BudgetActivity extends BaseActivity {
    @Inject AmountFormatter amountFormatter;

    public static void start(Context context, String budgetId) {
        final Intent intent = makeIntentForActivity(context, BudgetActivity.class);
        BudgetActivityPresenter.addExtras(intent, budgetId);
        startActivity(context, intent);
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_budget);
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
        return new BudgetActivityPresenter(getEventBus(), amountFormatter);
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.Budget;
    }
}
