package com.code44.finance.ui.budgets.edit.presenters;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.code44.finance.R;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.Presenter;
import com.code44.finance.ui.dialogs.recurrencepicker.EventRecurrence;
import com.code44.finance.ui.dialogs.recurrencepicker.EventRecurrenceFormatter;

public class RecurrencePresenter extends Presenter {
    private final Button recurrenceButton;

    private Activity activity;
    private long dateStart;
    private String rrule;
    private EventRecurrence eventRecurrence = new EventRecurrence();

    public RecurrencePresenter(BaseActivity activity, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        this.activity = activity;

        recurrenceButton = findView(activity, R.id.recurrenceButton);

        recurrenceButton.setOnClickListener(clickListener);
        recurrenceButton.setOnLongClickListener(longClickListener);
    }

    public void setRecurrence(long dateStart, String rrule) {
        this.dateStart = dateStart;
        this.rrule = rrule;
        eventRecurrence.parse(this.rrule);
        populateRepeats();
    }

    private void populateRepeats() {
        String repeatString = EventRecurrenceFormatter.getString(activity, activity.getResources(), this.dateStart, eventRecurrence, true);
        recurrenceButton.setText(repeatString);
    }
}
