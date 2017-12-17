package com.code44.finance.ui.budgets.edit.presenters;

import android.os.Parcel;
import android.os.Parcelable;

import com.code44.finance.data.model.Budget;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Tag;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BudgetEditData implements Parcelable {
    public static final Creator<BudgetEditData> CREATOR = new Creator<BudgetEditData>() {
        public BudgetEditData createFromParcel(Parcel in) {
            return new BudgetEditData(in);
        }

        public BudgetEditData[] newArray(int size) {
            return new BudgetEditData[size];
        }
    };

    private Budget storedBudget;

    private Long amount;
    private long dateStart;
    private String recurrence;
    private Category category;
    private List<Tag> tags;

    private boolean isAmountSet = false;
    private boolean isDateStartSet = false;
    private boolean isRecurrenceSet = false;
    private boolean isCategorySet = false;
    private boolean isTagsSet = false;

    public BudgetEditData() {
    }

    private BudgetEditData(Parcel in) {
        amount = (Long) in.readValue(Long.class.getClassLoader());
        storedBudget = in.readParcelable(Budget.class.getClassLoader());
        dateStart = (Long) in.readValue(Long.class.getClassLoader());
        recurrence = in.readString();
        category = in.readParcelable(Category.class.getClassLoader());
        final boolean hasTags = in.readInt() != 0;
        if (hasTags) {
            tags = new ArrayList<>();
            in.readTypedList(tags, Tag.CREATOR);
        }
        isAmountSet = in.readInt() == 1;
        isDateStartSet = in.readInt() == 1;
        isRecurrenceSet = in.readInt() == 1;
        isCategorySet = in.readInt() == 1;
        isTagsSet = in.readInt() == 1;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(amount);
        dest.writeValue(dateStart);
        dest.writeString(recurrence);
        dest.writeParcelable(category, flags);
        final boolean hasTags = tags != null;
        dest.writeInt(hasTags ? 1 : 0);
        if (hasTags) {
            dest.writeTypedList(tags);
        }
        dest.writeInt(isAmountSet ? 1 : 0);
        dest.writeInt(isDateStartSet ? 1 : 0);
        dest.writeInt(isRecurrenceSet ? 1 : 0);
        dest.writeInt(isCategorySet ? 1 : 0);
        dest.writeInt(isTagsSet ? 1 : 0);
    }

    public Budget getStoredBudget() {
        return storedBudget;
    }

    public void setStoredBudget(Budget storedBudget) {
        this.storedBudget = storedBudget;
    }

    public long getAmount() {
        if (amount != null) {
            return amount;
        }

        if (storedBudget != null) {
            return storedBudget.getAmount();
        }

        return 0;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
        isAmountSet = amount != null;
    }

    public long getDateStart() {
        if (isDateStartSet) {
            return dateStart;
        }

        if (storedBudget != null) {
            return storedBudget.getDateStart();
        }

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        return now.getTimeInMillis();
    }

    public void setDateStart(Long dateStart) {
        this.dateStart = dateStart;
        isDateStartSet = true;
    }

    public String getRecurrence() {
        if (isRecurrenceSet || recurrence != null) {
            return recurrence;
        }

        if (storedBudget != null && storedBudget.getRecurrence() != null) {
            return storedBudget.getRecurrence();
        }

        return null;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
        isRecurrenceSet = true;
    }

    public Category getCategory() {
        if (isCategorySet || category != null) {
            return category;
        }

        if (storedBudget != null && storedBudget.getCategory() != null) {
            return storedBudget.getCategory();
        }

        return null;
    }

    public void setCategory(Category category) {
        this.category = category;
        isCategorySet = true;
    }

    public List<Tag> getTags() {
        if (isTagsSet || tags != null) {
            return tags;
        }

        if (storedBudget != null && storedBudget.getTags() != null) {
            return storedBudget.getTags();
        }

        return null;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
        isTagsSet = true;
    }

    public boolean isAmountSet() {
        return isAmountSet || storedBudget != null;
    }

    public boolean isDateStartSet() {
        return isDateStartSet || storedBudget != null;
    }

    public boolean isRecurrenceSet() {
        return isRecurrenceSet || storedBudget != null;
    }

    public boolean isCategorySet() {
        return isCategorySet || storedBudget != null;
    }

    public boolean isTagsSet() {
        return isTagsSet || storedBudget != null;
    }

    public Budget getModel() {
        final Budget budget = new Budget();
        if (storedBudget != null) {
            budget.setId(storedBudget.getId());
        }
        budget.setAmount(getAmount());
        budget.setDateStart(getDateStart());
        budget.setRecurrence(getRecurrence());
        budget.setCategory(getCategory());
        budget.setTags(getTags());
        return budget;
    }
}
