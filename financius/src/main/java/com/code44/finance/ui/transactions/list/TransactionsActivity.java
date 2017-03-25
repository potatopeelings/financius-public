package com.code44.finance.ui.transactions.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.code44.finance.R;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.ui.common.activities.BaseDrawerActivity;
import com.code44.finance.ui.common.navigation.NavigationScreen;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.ui.transactions.edit.TransactionEditActivity;
import com.code44.finance.utils.analytics.Analytics;
import com.code44.finance.utils.interval.CurrentInterval;
import com.code44.finance.views.FabImageButton;

import javax.inject.Inject;

public class TransactionsActivity extends BaseDrawerActivity implements View.OnClickListener {
    @Inject AmountFormatter amountFormatter;
    @Inject CurrentInterval currentInterval;

    public static Intent makeViewIntent(Context context) {
        final Intent intent = makeIntentForActivity(context, TransactionsActivity.class);
        TransactionsActivityPresenter.addViewExtras(intent);
        return intent;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        setShowDrawer(true);
        setShowDrawerToggle(true);
        super.onCreate(savedInstanceState);
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        final ImageButton addImageButton = (ImageButton) findViewById(R.id.addImageButton);
        if (addImageButton != null) {
            addImageButton.setVisibility(View.GONE);
        }

        final FabImageButton newTransactionView = (FabImageButton) findViewById(R.id.newTransaction);
        newTransactionView.setOnClickListener(this);
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
        return new TransactionsActivityPresenter(getEventBus(), amountFormatter, currentInterval);
    }

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newTransaction:
                TransactionEditActivity.start(this, null);
                break;
        }
    }

    @Override protected NavigationScreen getNavigationScreen() {
        return NavigationScreen.Transactions;
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.TransactionList;
    }
}
