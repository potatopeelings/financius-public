package com.code44.finance.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Parcel;
import android.text.TextUtils;

import com.code44.finance.common.utils.Strings;
import com.code44.finance.data.db.Column;
import com.code44.finance.data.db.Tables;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Budget extends Model {
    public static final Creator<Budget> CREATOR = new Creator<Budget>() {
        public Budget createFromParcel(Parcel in) {
            return new Budget(in);
        }

        public Budget[] newArray(int size) {
            return new Budget[size];
        }
    };

    private long amount;
    private long dateStart;
    private String recurrence;
    private Category category;
    private List<Tag> tags;

    public Budget() {
        super();
        setAmount(0);
        setDateStart(0);
        setRecurrence(null);
        setCategory(null);
        setTags(null);
    }

    public Budget(Parcel parcel) {
        super(parcel);
        setAmount(parcel.readLong());
        setDateStart(parcel.readLong());
        setRecurrence(parcel.readString());
        setCategory((Category) parcel.readParcelable(Category.class.getClassLoader()));
        tags = new ArrayList<>();
        parcel.readTypedList(tags, Tag.CREATOR);
    }

    public static Budget from(Cursor cursor) {
        final Budget budget = new Budget();
        if (cursor.getCount() > 0) {
            try {
                budget.updateFromCursor(cursor, null);
            } catch (Exception e) {
                Crashlytics.log(DatabaseUtils.dumpCurrentRowToString(cursor));
                throw e;
            }
        }
        return budget;
    }

    @Override public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeLong(amount);
        parcel.writeLong(dateStart);
        parcel.writeString(recurrence);
        parcel.writeParcelable(category, 0);
        parcel.writeTypedList(tags);
    }

    @Override public ContentValues asContentValues() {
        final ContentValues values = super.asContentValues();
        values.put(Tables.Budgets.AMOUNT.getName(), amount);
        values.put(Tables.Budgets.DATE_START.getName(), dateStart);
        values.put(Tables.Budgets.RECURRENCE.getName(), recurrence);
        values.put(Tables.Budgets.CATEGORY_ID.getName(), category == null ? null : category.getId());
        final StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            if (sb.length() > 0) {
                sb.append(Tables.CONCAT_SEPARATOR);
            }
            sb.append(tag.getId());
        }
        values.put(Tables.Tags.ID.getName(), sb.toString());
        return values;
    }

    @Override public void prepareForContentValues() {
        super.prepareForContentValues();

        if (tags == null) {
            tags = Collections.emptyList();
        }
    }

    @Override public void validateForContentValues() throws IllegalStateException {
        super.validateForContentValues();
    }

    @Override public void updateFromCursor(Cursor cursor, String columnPrefixTable) {
        super.updateFromCursor(cursor, columnPrefixTable);
        int index;

        // Amount
        index = cursor.getColumnIndex(Tables.Budgets.AMOUNT.getName(columnPrefixTable));
        if (index >= 0) {
            setAmount(cursor.getLong(index));
        }

        // Date Start
        index = cursor.getColumnIndex(Tables.Budgets.DATE_START.getName(columnPrefixTable));
        if (index >= 0) {
            setDateStart(cursor.getLong(index));
        }

        // Recurrence
        index = cursor.getColumnIndex(Tables.Budgets.RECURRENCE.getName(columnPrefixTable));
        if (index >= 0) {
            setRecurrence(cursor.getString(index));
        }
        // Category
        index = cursor.getColumnIndex(Tables.Budgets.CATEGORY_ID.getName(columnPrefixTable));
        if (index >= 0 && !Strings.isEmpty(cursor.getString(index)) && !cursor.getString(index).equals("null")) {
            final Category category = Category.from(cursor);
            category.setId(cursor.getString(index));
            setCategory(category);
        } else {
            setCategory(null);
        }

        // Tags
        final String[] tagIds;
        final String[] tagTitles;

        index = cursor.getColumnIndex(Tables.Tags.ID.getName(columnPrefixTable));
        if (index >= 0) {
            final String str = cursor.getString(index);
            if (!Strings.isEmpty(str)) {
                tagIds = TextUtils.split(str, Tables.CONCAT_SEPARATOR);
            } else {
                tagIds = null;
            }
        } else {
            tagIds = null;
        }

        index = cursor.getColumnIndex(Tables.Tags.TITLE.getName(columnPrefixTable));
        if (index >= 0) {
            final String str = cursor.getString(index);
            if (!Strings.isEmpty(str)) {
                tagTitles = TextUtils.split(str, Tables.CONCAT_SEPARATOR);
            } else {
                tagTitles = null;
            }
        } else {
            tagTitles = null;
        }

        if (tagIds != null || tagTitles != null) {
            final List<Tag> tags = new ArrayList<>();
            final int count = tagIds != null ? tagIds.length : tagTitles.length;
            for (int i = 0; i < count; i++) {
                final Tag tag = new Tag();
                if (tagIds != null) {
                    tag.setId(tagIds[i]);
                }

                if (tagTitles != null) {
                    tag.setTitle(tagTitles[i]);
                }
                tags.add(tag);
            }
            setTags(tags);
        } else {
            setTags(null);
        }
    }

    @Override protected Column getLocalIdColumn() {
        return Tables.Budgets.LOCAL_ID;
    }

    @Override protected Column getIdColumn() {
        return Tables.Budgets.ID;
    }

    @Override protected Column getModelStateColumn() {
        return Tables.Budgets.MODEL_STATE;
    }

    @Override protected Column getSyncStateColumn() {
        return Tables.Budgets.SYNC_STATE;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getDateStart() {
        return dateStart;
    }

    public void setDateStart(long dateStart) {
        this.dateStart = dateStart;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags == null ? Collections.<Tag>emptyList() : tags;
    }
}
