package com.code44.finance.data.providers;

import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;

import com.code44.finance.data.db.Column;
import com.code44.finance.data.db.Tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetsProvider extends ModelProvider {
    public static final String URI_PARAM_JOIN_TABLE = "join_table";
    public static final String URI_VALUE_JOIN_TABLE_CATEGORIES = "categories";
    public static final String URI_VALUE_JOIN_TABLE_TAGS = "tags";

    public static Uri uriBudgets() {
        return uriModels(BudgetsProvider.class, Tables.Budgets.TABLE_NAME);
    }

    public static Uri uriBudget(String budgetServerId) {
        return uriModel(BudgetsProvider.class, Tables.Budgets.TABLE_NAME, budgetServerId);
    }

    @Override protected String getModelTable() {
        return Tables.Budgets.TABLE_NAME;
    }

    @Override protected String getQueryTables(Uri uri) {
        final List<String> joinTables = new ArrayList<>();
        if (uri.getQueryParameterNames().contains(URI_PARAM_JOIN_TABLE)) {
            // Join specific tables
            joinTables.addAll(uri.getQueryParameters(URI_PARAM_JOIN_TABLE));
        } else {
            // Join all the things!
            joinTables.add(URI_VALUE_JOIN_TABLE_CATEGORIES);
            joinTables.add(URI_VALUE_JOIN_TABLE_TAGS);
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(getModelTable());

        if (joinTables.contains(URI_VALUE_JOIN_TABLE_CATEGORIES)) {
            sb.append(" left join ").append(Tables.Categories.TABLE_NAME)
                    .append(" on ").append(Tables.Categories.ID.getNameWithTable()).append("=").append(Tables.Budgets.CATEGORY_ID);
        }

        if (joinTables.contains(URI_VALUE_JOIN_TABLE_TAGS)) {
            sb.append(" left join ").append(Tables.BudgetTags.TABLE_NAME)
                    .append(" on ").append(Tables.BudgetTags.BUDGET_ID).append("=").append(Tables.Budgets.ID.getNameWithTable());
            sb.append(" left join ").append(Tables.Tags.TABLE_NAME)
                    .append(" on ").append(Tables.Tags.ID.getNameWithTable()).append("=").append(Tables.BudgetTags.TAG_ID);
        }

        return sb.toString();
    }

    @Override protected Column getIdColumn() {
        return Tables.Budgets.ID;
    }

    @Override protected void onBeforeInsertItem(Uri uri, ContentValues values, Map<String, Object> outExtras) {
        super.onBeforeInsertItem(uri, values, outExtras);
        updateBudgetTags(values);
    }

    @Override protected void onBeforeUpdateItems(Uri uri, ContentValues values, String selection, String[] selectionArgs, Map<String, Object> outExtras) {
        throw new IllegalArgumentException("Update is not supported.");
    }

    @Override protected void onBeforeDeleteItems(Uri uri, String selection, String[] selectionArgs, Map<String, Object> outExtras) {
        super.onBeforeDeleteItems(uri, selection, selectionArgs, outExtras);
        putColumnToExtras(outExtras, getIdColumn(), selection, selectionArgs);
    }

    @Override protected void onBeforeBulkInsertIteration(Uri uri, ContentValues values, Map<String, Object> extras) {
        super.onBeforeBulkInsertIteration(uri, values, extras);
        updateBudgetTags(values);
    }

    private void updateBudgetTags(ContentValues values) {
        // Remove current tags
        final String budgetId = values.getAsString(Tables.Budgets.ID.getName());
        getDatabase().delete(Tables.BudgetTags.TABLE_NAME, Tables.BudgetTags.BUDGET_ID + "=?", new String[]{budgetId});

        // Add new tags
        if (values.containsKey(Tables.Tags.ID.getName())) {
            final String[] tagIds = TextUtils.split(values.getAsString(Tables.Tags.ID.getName()), Tables.CONCAT_SEPARATOR);
            values.remove(Tables.Tags.ID.getName());
            if (tagIds != null) {
                for (String tagId : tagIds) {
                    final ContentValues tagValues = new ContentValues();
                    tagValues.put(Tables.BudgetTags.BUDGET_ID.getName(), budgetId);
                    tagValues.put(Tables.BudgetTags.TAG_ID.getName(), tagId);
                    getDatabase().insert(Tables.BudgetTags.TABLE_NAME, null, tagValues);
                }
            }
        }
    }
}
