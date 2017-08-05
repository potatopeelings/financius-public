package com.code44.finance.ui.accounts.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.appcompat.R.bool;

import com.code44.finance.R;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.ui.common.activities.BaseDrawerActivity;
import com.code44.finance.ui.common.navigation.NavigationScreen;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.ui.common.presenters.ModelsActivityPresenter;
import com.code44.finance.utils.analytics.Analytics;
import com.code44.finance.utils.preferences.GeneralPrefs;

import javax.inject.Inject;

public class AccountsActivity extends BaseDrawerActivity {
    private static final String CONFIGURABLE = "CONFIGURABLE";
    private static final String READ_ONLY = "READ_ONLY";

    @Inject GeneralPrefs generalPrefs;
    @Inject CurrenciesManager currenciesManager;
    @Inject AmountFormatter amountFormatter;

    public static Intent makeViewIntent(Context context) {
        final Intent intent = makeIntentForActivity(context, AccountsActivity.class);
        // stand alone listing should have configuration
        intent.putExtra(CONFIGURABLE, true);
        AccountsActivityPresenter.addViewExtras(intent);
        return intent;
    }

    public static void startSelect(Activity activity, int requestCode) {
        startSelect(activity, requestCode, false);
    }

    public static void startSelect(Activity activity, int requestCode, boolean isReadOnly) {
        final Intent intent = makeIntentForActivity(activity, AccountsActivity.class);
        intent.putExtra(READ_ONLY, isReadOnly);
        // account selection view should not have configuration
        intent.putExtra(CONFIGURABLE, false);
        AccountsActivityPresenter.addSelectExtras(intent);
        startActivityForResult(activity, intent, requestCode);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        final ModelsActivityPresenter.Mode mode = (ModelsActivityPresenter.Mode) getIntent().getSerializableExtra(ModelsActivityPresenter.EXTRA_MODE);
        if (mode == ModelsActivityPresenter.Mode.View) {
            setShowDrawer(true);
            setShowDrawerToggle(true);
        }
        super.onCreate(savedInstanceState);
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_accounts);
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
        Bundle extras = getIntent().getExtras();
        boolean isReadOnly = extras.getBoolean(READ_ONLY);
        boolean isConfigurable =  extras.getBoolean(CONFIGURABLE);

        return new AccountsActivityPresenter(generalPrefs, currenciesManager, amountFormatter, isReadOnly, isConfigurable);
    }

    @Override protected NavigationScreen getNavigationScreen() {
        return NavigationScreen.Accounts;
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.AccountList;
    }
}
