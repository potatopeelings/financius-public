package com.code44.finance.ui.budgets.edit.presenters;

import android.view.View;
import android.widget.Button;

import com.code44.finance.R;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.Presenter;
import com.code44.finance.utils.ThemeUtils;

public class AmountPresenter extends Presenter {
    private final CurrenciesManager currenciesManager;
    private final AmountFormatter amountFormatter;
    private final Button amountButton;

    private long amount = 0;

    public AmountPresenter(BaseActivity activity, View.OnClickListener clickListener, View.OnLongClickListener longClickListener, CurrenciesManager currenciesManager, AmountFormatter amountFormatter) {
        this.currenciesManager = currenciesManager;
        this.amountFormatter = amountFormatter;

        amountButton = findView(activity, R.id.amountButton);

        amountButton.setOnClickListener(clickListener);
        amountButton.setOnLongClickListener(longClickListener);
        amountButton.setOnLongClickListener(longClickListener);
    }

    public void showError() {
        amountButton.setTextColor(ThemeUtils.getColor(amountButton.getContext(), R.attr.colorError));
    }

    public void setAmount(long amount) {
        this.amount = amount;
        update();
    }

    private void update() {
        if (amount > 0) {
            amountButton.setTextColor(ThemeUtils.getColor(amountButton.getContext(), android.R.attr.textColorPrimaryInverse));
        }

        amountButton.setText(amountFormatter.format(getAmountCurrency(), amount));
    }

    private String getAmountCurrency() {
        return currenciesManager.getMainCurrencyCode();
    }
}
