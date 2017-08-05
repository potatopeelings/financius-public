package com.code44.finance.ui.transactions.filter.presenters;

import android.view.View;
import android.widget.Button;

import com.code44.finance.R;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.Presenter;

public class AmountPresenter extends Presenter {
    private final CurrenciesManager currenciesManager;
    private final AmountFormatter amountFormatter;
    private final Button amountFromButton;
    private final Button amountToButton;

    public AmountPresenter(BaseActivity activity, View.OnClickListener clickListener, View.OnLongClickListener longClickListener, CurrenciesManager currenciesManager, AmountFormatter amountFormatter) {
        this.currenciesManager = currenciesManager;
        this.amountFormatter = amountFormatter;

        amountFromButton = findView(activity, R.id.amountFromButton);
        amountToButton = findView(activity, R.id.amountToButton);

        amountFromButton.setOnClickListener(clickListener);
        amountFromButton.setOnLongClickListener(longClickListener);
        amountToButton.setOnClickListener(clickListener);
        amountToButton.setOnLongClickListener(longClickListener);
    }

    public void setAmountFrom(Long amountFrom) {
        setAmount(amountFromButton, amountFrom);
    }

    public void setAmountTo(Long amountTo) {
        setAmount(amountToButton, amountTo);
    }

    private void setAmount(Button amountButton, Long amount) {
        if (amount == null) {
            amountButton.setText("");
        }
        else {
            amountButton.setText(amountFormatter.format(getAmountCurrency(), amount));
        }
    }

    private String getAmountCurrency() {
        return currenciesManager.getMainCurrencyCode();
    }
}
