package com.code44.finance.ui.accounts.list;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Account;
import com.code44.finance.data.providers.AccountsProvider;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.ui.accounts.detail.AccountActivity;
import com.code44.finance.ui.accounts.edit.AccountEditActivity;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.adapters.ModelsAdapter;
import com.code44.finance.ui.common.presenters.ModelsActivityPresenter;
import com.code44.finance.utils.preferences.GeneralPrefs;

class AccountsActivityPresenter extends ModelsActivityPresenter<Account> implements CompoundButton.OnCheckedChangeListener {
    private final GeneralPrefs generalPrefs;
    private final CurrenciesManager currenciesManager;
    private final AmountFormatter amountFormatter;
    private final boolean isConfigurable;

    private TextView balanceTextView;

    public AccountsActivityPresenter(GeneralPrefs generalPrefs, CurrenciesManager currenciesManager, AmountFormatter amountFormatter, boolean isReadOnly, boolean isConfigurable) {
        super(isReadOnly);
        this.generalPrefs = generalPrefs;
        this.currenciesManager = currenciesManager;
        this.amountFormatter = amountFormatter;
        this.isConfigurable = isConfigurable;
    }

    @Override public void onCreate(BaseActivity activity, Bundle savedInstanceState) {
        super.onCreate(activity, savedInstanceState);

        final View balanceContainerView = findView(activity, R.id.balanceContainerView);
        balanceTextView = findView(activity, R.id.balanceTextView);
        final SwitchCompat hideZeroBalanceAccountsSwitch = findView(activity, R.id.hideZeroBalanceAccountsSwitch);

        if (getMode() != Mode.View) {
            balanceContainerView.setVisibility(View.GONE);
        }
        hideZeroBalanceAccountsSwitch.setChecked(generalPrefs.getHideZeroBalanceAccounts());
        hideZeroBalanceAccountsSwitch.setOnCheckedChangeListener(this);

        if (!isConfigurable) {
            final View settingsContainerView = findView(activity, R.id.settingsContainerView);
            settingsContainerView.setVisibility(View.GONE);
        }
    }

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        generalPrefs.setHideZeroBalanceAccounts(isChecked);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_MODELS) {
            updateBalance(data);
        }
        super.onLoadFinished(loader, data);
    }

    @Override protected ModelsAdapter<Account> createAdapter(ModelsAdapter.OnModelClickListener<Account> defaultOnModelClickListener) {
        return new AccountsAdapter(defaultOnModelClickListener, currenciesManager, amountFormatter);
    }

    @Override protected CursorLoader getModelsCursorLoader(Context context) {
        return Tables.Accounts.getQuery().asCursorLoader(context, AccountsProvider.uriAccounts());
    }

    @Override protected void onModelClick(Context context, View view, Account model, Cursor cursor, int position) {
        AccountActivity.start(context, model.getId());
    }

    @Override protected void startModelEdit(Context context, String modelId) {
        AccountEditActivity.start(context, modelId);
    }

    private void updateBalance(Cursor cursor) {
        long balance = 0;
        if (cursor.moveToFirst()) {
            do {
                final Account account = Account.from(cursor);
                if (account.includeInTotals()) {
                    balance += account.getBalance() * currenciesManager.getExchangeRate(account.getCurrencyCode(), currenciesManager.getMainCurrencyCode());
                }
            } while (cursor.moveToNext());
        }
        balanceTextView.setText(amountFormatter.format(balance));
    }
}
