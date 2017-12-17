package com.code44.finance.ui.budgets.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.ui.common.activities.BaseDrawerActivity;
import com.code44.finance.ui.common.navigation.NavigationScreen;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.utils.analytics.Analytics;
import com.code44.finance.utils.interval.CurrentInterval;

import javax.inject.Inject;

public class BudgetsActivity extends BaseDrawerActivity {
    @Inject AmountFormatter amountFormatter;
    @Inject CurrentInterval currentInterval;

    public static Intent makeViewIntent(Context context) {
        final Intent intent = makeIntentForActivity(context, BudgetsActivity.class);
        BudgetsActivityPresenter.addViewExtras(intent);
        return intent;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        setShowDrawer(true);
        setShowDrawerToggle(true);
        super.onCreate(savedInstanceState);
        getEventBus().register(this);
    }

    @Override protected void onDestroy() {
        getEventBus().unregister(this);
        super.onDestroy();
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_budgets);
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
        return new BudgetsActivityPresenter(getEventBus(), amountFormatter, currentInterval);
    }

    @Override protected NavigationScreen getNavigationScreen() {
        return NavigationScreen.Budgets;
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.BudgetList;
    }
}
