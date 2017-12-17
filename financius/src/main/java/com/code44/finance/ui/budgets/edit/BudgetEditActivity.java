package com.code44.finance.ui.budgets.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.utils.analytics.Analytics;
import com.code44.finance.utils.preferences.GeneralPrefs;

import javax.inject.Inject;

public class BudgetEditActivity extends BaseActivity {
    @Inject AmountFormatter amountFormatter;
    @Inject CurrenciesManager currenciesManager;
    @Inject GeneralPrefs generalPrefs;

    public static void start(Context context, String budgetId) {
        final Intent intent = makeIntentForActivity(context, BudgetEditActivity.class);
        BudgetEditActivityPresenter.addExtras(intent, budgetId);
        startActivity(context, intent);
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_budget_edit);
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
        return new BudgetEditActivityPresenter(getEventBus(), this, getSupportFragmentManager(), amountFormatter, currenciesManager, generalPrefs);
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.BudgetEdit;
    }
}
