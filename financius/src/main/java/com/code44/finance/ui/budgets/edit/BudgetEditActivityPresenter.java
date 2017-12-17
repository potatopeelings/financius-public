package com.code44.finance.ui.budgets.edit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.view.View;
import android.widget.Button;

import com.code44.finance.R;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.DataStore;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Budget;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Tag;
import com.code44.finance.data.providers.BudgetsProvider;
import com.code44.finance.money.AmountFormatter;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.ui.CalculatorActivity;
import com.code44.finance.ui.budgets.edit.presenters.AmountPresenter;
import com.code44.finance.ui.budgets.edit.presenters.BudgetEditData;
import com.code44.finance.ui.budgets.edit.presenters.CategoryPresenter;
import com.code44.finance.ui.budgets.edit.presenters.RecurrencePresenter;
import com.code44.finance.ui.categories.list.CategoriesActivity;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.ModelEditActivityPresenter;
import com.code44.finance.ui.common.presenters.ModelsActivityPresenter;
import com.code44.finance.ui.budgets.edit.presenters.MultipleTagsPresenter;
import com.code44.finance.ui.dialogs.recurrencepicker.RecurrencePickerDialogFragment;
import com.code44.finance.ui.tags.list.TagsActivity;
import com.code44.finance.utils.EventBus;
import com.code44.finance.utils.ThemeUtils;
import com.code44.finance.utils.preferences.GeneralPrefs;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class BudgetEditActivityPresenter extends ModelEditActivityPresenter<Budget> implements View.OnClickListener, View.OnLongClickListener, RecurrencePickerDialogFragment.OnRecurrenceSetListener {
    private static final int REQUEST_AMOUNT = 1;
    private static final int REQUEST_RECURRENCE = 2;
    private static final int REQUEST_CATEGORY = 3;
    private static final int REQUEST_TAGS = 4;

    private static final String STATE_BUDGET_EDIT_DATA = "STATE_BUDGET_EDIT_DATA";

    private final Activity activity;
    private final FragmentManager fragmentManager;
    private final AmountFormatter amountFormatter;
    private final CurrenciesManager currenciesManager;
    private final GeneralPrefs generalPrefs;

    private AmountPresenter amountViewController;
    private RecurrencePresenter recurrenceViewController;
    private CategoryPresenter categoryViewController;
    private MultipleTagsPresenter tagsViewController;

    private Button categoryButton;

    private BudgetEditData budgetEditData;
    private boolean isResumed = false;

    public BudgetEditActivityPresenter(EventBus eventBus, Activity activity, FragmentManager fragmentManager, AmountFormatter amountFormatter, CurrenciesManager currenciesManager, GeneralPrefs generalPrefs) {
        super(eventBus);
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.amountFormatter = amountFormatter;
        this.currenciesManager = currenciesManager;
        this.generalPrefs = generalPrefs;
    }

    @Override public void onCreate(BaseActivity activity, Bundle savedInstanceState) {
        super.onCreate(activity, savedInstanceState);

        categoryButton = findView(activity, R.id.categoryButton);

        amountViewController = new AmountPresenter(activity, this, this, currenciesManager, amountFormatter);
        recurrenceViewController = new RecurrencePresenter(activity, this, this);
        categoryViewController = new CategoryPresenter(activity, this, this);
        tagsViewController = new MultipleTagsPresenter(activity, this, this);

        if (savedInstanceState == null) {
            budgetEditData = new BudgetEditData();
            update();
        } else {
            budgetEditData = savedInstanceState.getParcelable(STATE_BUDGET_EDIT_DATA);
        }
    }

    @Override public void onResume(BaseActivity activity) {
        super.onResume(activity);
        isResumed = true;
        getEventBus().register(this);
        update();
    }

    @Override public void onPause(BaseActivity activity) {
        super.onPause(activity);
        isResumed = false;
        getEventBus().unregister(this);
    }

    @Override protected void onDataChanged(Budget storedModel) {
        budgetEditData.setStoredBudget(storedModel);

        updateAmount(budgetEditData.getAmount());
        updateRecurrence(budgetEditData.getDateStart(), budgetEditData.getRecurrence());
        updateCategory(budgetEditData.getCategory());
        updateTags(budgetEditData.getTags());
    }

    @Override protected boolean onSave() {
        boolean canSave = true;
        Budget budget = budgetEditData.getModel();

        if (budget.getCategory() == null) {
            canSave = false;
            categoryButton.setHintTextColor(ThemeUtils.getColor(categoryButton.getContext(), R.attr.colorError));
        }

        if (canSave) {
            DataStore.insert().values(budgetEditData.getModel().asContentValues()).into(getActivity(), BudgetsProvider.uriBudgets());
        }

        return canSave;
    }

    @Override protected CursorLoader getModelCursorLoader(Context context, String modelId) {
        return Tables.Budgets.getQuery().asCursorLoader(context, BudgetsProvider.uriBudget(modelId));
    }

    @Override protected Budget getModelFrom(Cursor cursor) {
        return Budget.from(cursor);
    }

    @Override public void onActivityResult(BaseActivity activity, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(activity, requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_AMOUNT:
                    budgetEditData.setAmount(data.getLongExtra(CalculatorActivity.RESULT_EXTRA_RESULT, 0));
                    break;
                case REQUEST_CATEGORY:
                    budgetEditData.setCategory(ModelsActivityPresenter.<Category>getModelExtra(data));
                    break;
                case REQUEST_TAGS:
                    budgetEditData.setTags(ModelsActivityPresenter.<Tag>getModelsExtra(data));
                    break;
            }
        }
    }

    @Override public void onRecurrenceSet(long dateStart, String rrule) {
        budgetEditData.setDateStart(dateStart);
        budgetEditData.setRecurrence(rrule);
        update();
    }

    @Override public void onSaveInstanceState(BaseActivity activity, Bundle outState) {
        super.onSaveInstanceState(activity, outState);
        outState.putParcelable(STATE_BUDGET_EDIT_DATA, budgetEditData);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amountButton:
                CalculatorActivity.start(getActivity(), REQUEST_AMOUNT, budgetEditData.getAmount());
                break;
            case R.id.recurrenceButton:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                RecurrencePickerDialogFragment rpd = new RecurrencePickerDialogFragment();
                rpd.setEventBus(getEventBus());
                rpd.setOnRecurrenceSetListener(this);
                Bundle bundle = new Bundle();
                bundle.putLong(RecurrencePickerDialogFragment.BUNDLE_START_TIME_MILLIS, budgetEditData.getDateStart());
                bundle.putString(RecurrencePickerDialogFragment.BUNDLE_RRULE, budgetEditData.getRecurrence());
                bundle.putBoolean(RecurrencePickerDialogFragment.BUNDLE_HIDE_SWITCH_BUTTON, true);
                rpd.setArguments(bundle);
                rpd.show(fm, "FRAG_TAG_RECUR_PICKER");
                break;
            case R.id.categoryButton:
                CategoriesActivity.startSelect(getActivity(), REQUEST_CATEGORY, TransactionType.Expense);
                break;
            case R.id.tagsButton:
                TagsActivity.startMultiSelect(getActivity(), REQUEST_TAGS, budgetEditData.getTags() != null ? budgetEditData.getTags() : Collections.<Tag>emptyList());
                break;
        }
    }

    @Override public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.amountButton:
                budgetEditData.setAmount(0L);
                break;
            case R.id.recurrenceButton:
                budgetEditData.setRecurrence(null);
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);
                budgetEditData.setDateStart(now.getTimeInMillis());
                break;
            case R.id.categoryButton:
                budgetEditData.setCategory(null);
                break;
            case R.id.tagsButton:
                budgetEditData.setTags(null);
                return true;
            default:
                return false;
        }
        update();
        return true;
    }

    private void update() {
        if (!isResumed) {
            return;
        }

        onDataChanged(getStoredModel());
    }

    private void updateAmount(long amount) {
        amountViewController.setAmount(amount);
    }

    private void updateRecurrence(long dateStart, String rrule) {
        recurrenceViewController.setRecurrence(dateStart, rrule);
    }

    private void updateCategory(Category category) {
        categoryViewController.setCategory(category);
    }

    private void updateTags(List<Tag> tags) {
        tagsViewController.setTags(tags);
    }
}
