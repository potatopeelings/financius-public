package com.code44.finance.ui.transactions.filter.presenters;

import android.view.View;
import android.widget.Button;

import com.code44.finance.R;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.Presenter;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;

public class DatePresenter extends Presenter {
    private final Button dateFromButton;
    private final Button dateToButton;

    public DatePresenter(BaseActivity activity, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        dateFromButton = findView(activity, R.id.dateFromButton);
        dateToButton = findView(activity, R.id.dateToButton);

        dateFromButton.setOnClickListener(clickListener);
        dateFromButton.setOnLongClickListener(longClickListener);
        dateToButton.setOnClickListener(clickListener);
        dateToButton.setOnLongClickListener(longClickListener);
    }

    public void setDateFrom(Long dateFrom) {
        setDate(dateFromButton, dateFrom);
    }

    public void setDateTo(Long dateTo) {
        setDate(dateToButton, dateTo);
    }

    private  void setDate(Button dateButton, Long date) {
        if (date == null) {
            dateButton.setText("");
        }
        else {
            DateTime dateTime = new DateTime(date);
            int dateFormat = DateUtils.FORMAT_SHOW_DATE;
            if (dateTime.getYear() != DateTime.now().getYear()) {
                dateFormat |= DateUtils.FORMAT_SHOW_YEAR;
            }
            dateButton.setText(DateUtils.formatDateTime(dateButton.getContext(), dateTime, dateFormat));
        }
    }
}
