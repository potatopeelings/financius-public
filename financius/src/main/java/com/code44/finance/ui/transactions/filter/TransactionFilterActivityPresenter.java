package com.code44.finance.ui.transactions.filter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.code44.finance.R;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.model.Account;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Tag;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.ui.CalculatorActivity;
import com.code44.finance.ui.accounts.list.AccountsActivity;
import com.code44.finance.ui.categories.list.CategoriesActivity;
import com.code44.finance.ui.common.ModelListActivity;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.ui.common.presenters.ModelsActivityPresenter;
import com.code44.finance.ui.dialogs.DatePickerDialog;
import com.code44.finance.ui.tags.list.TagsActivity;
import com.code44.finance.ui.transactions.edit.presenters.AccountsPresenter;
import com.code44.finance.ui.transactions.edit.presenters.CategoryPresenter;
import com.code44.finance.ui.transactions.edit.presenters.MultipleTagsPresenter;
import com.code44.finance.ui.transactions.edit.presenters.NotePresenter;
import com.code44.finance.ui.transactions.edit.presenters.TransactionTypePresenter;
import com.code44.finance.ui.transactions.filter.presenters.AmountPresenter;
import com.code44.finance.ui.transactions.filter.presenters.DatePresenter;
import com.code44.finance.utils.EventBus;
import com.code44.finance.utils.filter.TransactionFilter;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.Collections;

class TransactionFilterActivityPresenter extends ActivityPresenter implements NotePresenter.Callbacks, View.OnClickListener, View.OnLongClickListener {
    private static final int REQUEST_AMOUNT_FROM = 1;
    private static final int REQUEST_AMOUNT_TO = 2;
    private static final int REQUEST_DATE_FROM = 3;
    private static final int REQUEST_DATE_TO = 4;
    private static final int REQUEST_ACCOUNT_FROM = 5;
    private static final int REQUEST_ACCOUNT_TO = 6;
    private static final int REQUEST_CATEGORY = 7;
    private static final int REQUEST_TAGS = 8;

    private final EventBus eventBus;
    private final CurrenciesManager currenciesManager;
    private final AmountFormatter amountFormatter;

    private TransactionTypePresenter transactionTypeViewController;
    private AmountPresenter amountViewController;
    private DatePresenter dateViewController;
    private AccountsPresenter accountsViewController;
    private CategoryPresenter categoryViewController;
    private MultipleTagsPresenter tagsViewController;
    private NotePresenter noteViewController;

    private TransactionFilter transactionFilter;

    public TransactionFilterActivityPresenter(EventBus eventBus, CurrenciesManager currenciesManager, AmountFormatter amountFormatter, TransactionFilter transactionFilter) {
        this.eventBus = eventBus;
        this.currenciesManager = currenciesManager;
        this.amountFormatter = amountFormatter;
        this.transactionFilter = transactionFilter;
    }

    @Override public void onCreate(BaseActivity activity, Bundle savedInstanceState) {
        super.onCreate(activity, savedInstanceState);

        transactionTypeViewController = new TransactionTypePresenter(activity, this);
        amountViewController = new AmountPresenter(activity, this, this, currenciesManager, amountFormatter);
        dateViewController = new DatePresenter(activity, this, this);
        accountsViewController = new AccountsPresenter(activity, this, this);
        categoryViewController = new CategoryPresenter(activity, this, this);
        tagsViewController = new MultipleTagsPresenter(activity, this, this);
        noteViewController = new NotePresenter(activity, this, this);

        final Button cancelButton = findView(activity, R.id.cancelButton);
        final Button saveButton = findView(activity, R.id.saveButton);
        cancelButton.setText(R.string.clear);
        saveButton.setText(R.string.filter);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        update();
    }

    @Override public void onResume(BaseActivity activity) {
        super.onResume(activity);
        getEventBus().register(this);
    }

    @Override public void onPause(BaseActivity activity) {
        super.onPause(activity);
        getEventBus().unregister(this);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transactionTypeContainerView:
                toggleTransactionType();
                break;
            case R.id.amountFromButton:
                CalculatorActivity.start(getActivity(), REQUEST_AMOUNT_FROM, coalesce(transactionFilter.getAmountFrom(), 0));
                break;
            case R.id.amountToButton:
                CalculatorActivity.start(getActivity(), REQUEST_AMOUNT_TO, coalesce(transactionFilter.getAmountTo(), 0));
                break;
            case R.id.dateFromButton:
                DatePickerDialog.show(getActivity().getSupportFragmentManager(), REQUEST_DATE_FROM, coalesce(transactionFilter.getDateFrom(), System.currentTimeMillis()));
                break;
            case R.id.dateToButton:
                DatePickerDialog.show(getActivity().getSupportFragmentManager(), REQUEST_DATE_TO, coalesce(transactionFilter.getDateTo(), System.currentTimeMillis()));
                break;
            case R.id.accountFromButton:
                AccountsActivity.startSelect(getActivity(), REQUEST_ACCOUNT_FROM, true);
                break;
            case R.id.accountToButton:
                AccountsActivity.startSelect(getActivity(), REQUEST_ACCOUNT_TO, true);
                break;
            case R.id.categoryButton:
                CategoriesActivity.startSelect(getActivity(), REQUEST_CATEGORY, transactionFilter.getTransactionType(), true);
                break;
            case R.id.tagsButton:
                TagsActivity.startMultiSelect(getActivity(), REQUEST_TAGS, transactionFilter.getTags() != null ? transactionFilter.getTags() : Collections.<Tag>emptyList(), true);
                break;
        }
    }

    @Override public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.amountFromButton:
                transactionFilter.setAmountFrom(null);
                update();
                return true;
            case R.id.amountToButton:
                transactionFilter.setAmountTo(null);
                update();
                return true;
            case R.id.dateFromButton:
                transactionFilter.setDateFrom(null);
                update();
                return true;
            case R.id.dateToButton:
                transactionFilter.setDateTo(null);
                update();
                return true;
            case R.id.accountFromButton:
                transactionFilter.setAccountFrom(null);
                update();
                return true;
            case R.id.accountToButton:
                transactionFilter.setAccountTo(null);
                update();
                return true;
            case R.id.categoryButton:
                transactionFilter.setCategory(null);
                update();
                return true;
            case R.id.tagsButton:
                transactionFilter.setTags(null);
                update();
                return true;
        }
        return false;
    }

    @Override public void onActivityResult(BaseActivity activity, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(activity, requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_AMOUNT_FROM:
                    transactionFilter.setAmountFrom(data.getLongExtra(CalculatorActivity.RESULT_EXTRA_RESULT, 0));
                    update();
                    break;
                case REQUEST_AMOUNT_TO:
                    transactionFilter.setAmountTo(data.getLongExtra(CalculatorActivity.RESULT_EXTRA_RESULT, 0));
                    update();
                    break;
                case REQUEST_ACCOUNT_FROM:
                    transactionFilter.setAccountFrom(ModelListActivity.<Account>getModelExtra(data));
                    update();
                    break;
                case REQUEST_ACCOUNT_TO:
                    transactionFilter.setAccountTo(ModelListActivity.<Account>getModelExtra(data));
                    update();
                    break;
                case REQUEST_CATEGORY:
                    transactionFilter.setCategory(ModelsActivityPresenter.<Category>getModelExtra(data));
                    update();
                    break;
                case REQUEST_TAGS:
                    transactionFilter.setTags(ModelsActivityPresenter.<Tag>getModelsExtra(data));
                    update();
                    break;
            }
        }
    }

    @Subscribe public void onDateSet(DatePickerDialog.DateSelected dateSelected) {
        switch (dateSelected.getRequestCode()) {
            case REQUEST_DATE_FROM:
                long dateFrom = getUpdatedDate(dateSelected, transactionFilter.getDateFrom());
                transactionFilter.setDateFrom(dateFrom);
                update();
                break;
            case REQUEST_DATE_TO:
                long dateTo = getUpdatedDate(dateSelected, transactionFilter.getDateTo());
                transactionFilter.setDateTo(dateTo);
                update();
                break;
        }
    }

    @Override public void onNoteUpdated(String note) {
        transactionFilter.setNote(note);
    }

    @Override public void onNoteFocusGained() {
        if (!transactionFilter.isNoteSet()) {
            transactionFilter.setNote(null);
            update();
        }
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    private void toggleTransactionType() {
        TransactionType transactionType;

        if (transactionFilter.getTransactionType() == null) {
            transactionType = TransactionType.Transfer.Expense;
        } else {
            switch (transactionFilter.getTransactionType()) {
                case Expense:
                    transactionType = TransactionType.Income;
                    break;
                case Income:
                    transactionType = TransactionType.Transfer;
                    break;
                case Transfer:
                    transactionType = null;
                    break;
                default:
                    throw new IllegalArgumentException("TransactionType " + transactionFilter.getTransactionType() + " is not supported.");
            }
        }

        transactionFilter.setTransactionType(transactionType);
        update();
    }

    private Long getUpdatedDate(DatePickerDialog.DateSelected dateSelected, Long dateTime) {
        return new DateTime(dateTime)
                    .withYear(dateSelected.getYear())
                    .withMonthOfYear(dateSelected.getMonthOfYear())
                    .withDayOfMonth(dateSelected.getDayOfMonth())
                    .getMillis();
    }

    private void update() {
        transactionTypeViewController.setTransactionType(transactionFilter.getTransactionType());
        accountsViewController.setTransactionType(transactionFilter.getTransactionType());
        categoryViewController.setTransactionType(transactionFilter.getTransactionType());
        amountViewController.setAmountFrom(transactionFilter.getAmountFrom());
        amountViewController.setAmountTo(transactionFilter.getAmountTo());
        dateViewController.setDateFrom(transactionFilter.getDateFrom());
        dateViewController.setDateTo(transactionFilter.getDateTo());
        accountsViewController.setAccountFrom(transactionFilter.getAccountFrom());
        accountsViewController.setAccountTo(transactionFilter.getAccountTo());
        categoryViewController.setCategory(transactionFilter.getCategory());
        tagsViewController.setTags(transactionFilter.getTags());
        noteViewController.setNote(transactionFilter.getNote());
    }

    private boolean onSave() {
        // TODO - validate filter values (greater than, etc.)
        return true;
    }

    private void save() {
        if (onSave()) {
            eventBus.post(transactionFilter);
            getActivity().finish();
        }
    }

    private void cancel() {
        transactionFilter.reset();
        getEventBus().post(transactionFilter);
        getActivity().finish();
    }

    private long coalesce(Long nullableLong, long defaultValue) {
        return (nullableLong != null) ? nullableLong.longValue() : defaultValue;
    }
}
