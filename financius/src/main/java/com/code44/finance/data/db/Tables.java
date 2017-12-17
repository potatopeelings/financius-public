package com.code44.finance.data.db;

import android.provider.BaseColumns;

import com.code44.finance.common.model.ModelState;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.Query;
import com.code44.finance.data.model.SyncState;

import java.util.ArrayList;
import java.util.List;

public final class Tables {
    public static final String SUFFIX_ID = "id";
    public static final String SUFFIX_MODEL_STATE = "model_state";
    public static final String SUFFIX_SYNC_STATE = "sync_state";
    public static final String CONCAT_SEPARATOR = ";";

    private Tables() {
    }

    private static Column getLocalIdColumn(String tableName) {
        return new Column(tableName, BaseColumns._ID, Column.DataType.INTEGER_PRIMARY_KEY, null, false);
    }

    private static Column getIdColumn(String tableName) {
        return new Column(tableName, SUFFIX_ID, Column.DataType.TEXT, null);
    }

    private static Column getModelStateColumn(String tableName) {
        return new Column(tableName, SUFFIX_MODEL_STATE, Column.DataType.INTEGER, String.valueOf(ModelState.Normal.asInt()));
    }

    private static Column getSyncStateColumn(String tableName) {
        return new Column(tableName, SUFFIX_SYNC_STATE, Column.DataType.INTEGER, String.valueOf(SyncState.None.asInt()));
    }

    private static String makeCreateScript(String table, Column... columns) {
        final StringBuilder sb = new StringBuilder("create table ");
        sb.append(table);
        sb.append(" (");

        if (columns != null) {
            for (int i = 0, size = columns.length; i < size; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(columns[i].getCreateScript());
            }
        }

        sb.append(");");

        return sb.toString();
    }

    private static String makeRelationshipCreateScript(String table, Column firstIdColumn, Column secondIdColumn, Column... columns) {
        final StringBuilder sb = new StringBuilder("create table ");
        sb.append(table);
        sb.append(" (");

        List<Column> allColumns = new ArrayList<>();
        allColumns.add(firstIdColumn);
        allColumns.add(secondIdColumn);
        java.util.Collections.addAll(allColumns, columns);

        for (int i = 0, size = allColumns.size(); i < size; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(allColumns.get(i).getCreateScript());
        }

        sb.append(",").append(" primary key (").append(firstIdColumn.getName()).append(", ").append(secondIdColumn.getName()).append(")");

        sb.append(");");

        return sb.toString();
    }

    public static final class ExchangeRates {
        public static final String TABLE_NAME = "exchange_rates";

        public static final Column LOCAL_ID = getLocalIdColumn(TABLE_NAME);
        public static final Column CURRENCY_CODE_FROM = new Column(TABLE_NAME, "currency_code_from", Column.DataType.TEXT);
        public static final Column CURRENCY_CODE_TO = new Column(TABLE_NAME, "currency_code_to", Column.DataType.TEXT);
        public static final Column RATE = new Column(TABLE_NAME, "rate", Column.DataType.REAL);

        public static final String[] PROJECTION = {CURRENCY_CODE_FROM.getName(), CURRENCY_CODE_TO.getName(), RATE.getName()};

        private ExchangeRates() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, LOCAL_ID, CURRENCY_CODE_FROM, CURRENCY_CODE_TO, RATE);
        }

        public static Query getQuery() {
            return Query.create()
                    .projectionLocalId(ExchangeRates.LOCAL_ID)
                    .projection(ExchangeRates.PROJECTION);
        }
    }

    public static final class CurrencyFormats {
        public static final String TABLE_NAME = "currencies";

        public static final Column LOCAL_ID = getLocalIdColumn(TABLE_NAME);
        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column CODE = new Column(TABLE_NAME, "code", Column.DataType.TEXT);
        public static final Column SYMBOL = new Column(TABLE_NAME, "symbol", Column.DataType.TEXT);
        public static final Column SYMBOL_POSITION = new Column(TABLE_NAME, "symbol_position", Column.DataType.INTEGER);
        public static final Column DECIMAL_SEPARATOR = new Column(TABLE_NAME, "decimal_separator", Column.DataType.TEXT);
        public static final Column GROUP_SEPARATOR = new Column(TABLE_NAME, "group_separator", Column.DataType.TEXT);
        public static final Column DECIMAL_COUNT = new Column(TABLE_NAME, "decimal_count", Column.DataType.INTEGER);

        public static final String[] PROJECTION = {ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                CODE.getName(), SYMBOL.getName(), SYMBOL_POSITION.getName(), DECIMAL_SEPARATOR.getName(),
                GROUP_SEPARATOR.getName(), DECIMAL_COUNT.getName()};

        private CurrencyFormats() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, LOCAL_ID, ID, MODEL_STATE, SYNC_STATE, CODE, SYMBOL,
                    SYMBOL_POSITION, DECIMAL_SEPARATOR, GROUP_SEPARATOR, DECIMAL_COUNT);
        }

        public static Query getQuery() {
            return Query.create()
                    .projectionLocalId(CurrencyFormats.LOCAL_ID)
                    .projection(CurrencyFormats.PROJECTION)
                    .selection("(" + CurrencyFormats.MODEL_STATE + "=?", ModelState.Normal.asString())
                    .selection(" or " + CurrencyFormats.MODEL_STATE + "=?)", ModelState.DeletedUndo.asString())
                    .sortOrder(CurrencyFormats.CODE.getName());
        }
    }

    public static final class Accounts {
        public static final String TABLE_NAME = "accounts";
        public static final String TEMP_TABLE_NAME_FROM_ACCOUNT = "accounts_from";
        public static final String TEMP_TABLE_NAME_TO_ACCOUNT = "accounts_to";

        public static final Column LOCAL_ID = getLocalIdColumn(TABLE_NAME);
        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column CURRENCY_CODE = new Column(TABLE_NAME, "currency_code", Column.DataType.TEXT);
        public static final Column TITLE = new Column(TABLE_NAME, "title", Column.DataType.TEXT);
        public static final Column NOTE = new Column(TABLE_NAME, "note", Column.DataType.TEXT);
        public static final Column BALANCE = new Column(TABLE_NAME, "balance", Column.DataType.INTEGER, "0");
        public static final Column INCLUDE_IN_TOTALS = new Column(TABLE_NAME, "include_in_totals", Column.DataType.BOOLEAN, "1");

        public static final String[] PROJECTION = {ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                CURRENCY_CODE.getName(), TITLE.getName(), NOTE.getName(), BALANCE.getName(), INCLUDE_IN_TOTALS.getName()};

        public static final String[] PROJECTION_ACCOUNT_FROM = {ID.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT), MODEL_STATE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT),
                SYNC_STATE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT), CURRENCY_CODE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT),
                TITLE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT), NOTE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT),
                BALANCE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT), INCLUDE_IN_TOTALS.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT)};

        public static final String[] PROJECTION_ACCOUNT_TO = {ID.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT), MODEL_STATE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT),
                SYNC_STATE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT), CURRENCY_CODE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT),
                TITLE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT), NOTE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT),
                BALANCE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT), INCLUDE_IN_TOTALS.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT)};

        private Accounts() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, LOCAL_ID, ID, MODEL_STATE, SYNC_STATE, CURRENCY_CODE, TITLE, NOTE, BALANCE, INCLUDE_IN_TOTALS);
        }

        public static Query getQuery() {
            return Query.create()
                    .projectionLocalId(Accounts.LOCAL_ID)
                    .projection(Accounts.PROJECTION)
                    .selection("(" + Accounts.MODEL_STATE + "=?", ModelState.Normal.asString())
                    .selection(" or " + Accounts.MODEL_STATE + "=?)", ModelState.DeletedUndo.asString())
                    .sortOrder(Accounts.INCLUDE_IN_TOTALS.getName() + " desc")
                    .sortOrder(Accounts.TITLE.getName());
        }
    }

    public static final class Categories {
        public static final String TABLE_NAME = "categories";

        public static final Column LOCAL_ID = getLocalIdColumn(TABLE_NAME);
        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column TRANSACTION_TYPE = new Column(TABLE_NAME, "transaction_type", Column.DataType.INTEGER);
        public static final Column TITLE = new Column(TABLE_NAME, "title", Column.DataType.TEXT);
        public static final Column COLOR = new Column(TABLE_NAME, "color", Column.DataType.INTEGER);
        public static final Column SORT_ORDER = new Column(TABLE_NAME, "sort_order", Column.DataType.INTEGER);

        public static final String[] PROJECTION = {ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                TRANSACTION_TYPE.getName(), TITLE.getName(), COLOR.getName(), SORT_ORDER.getName()};

        private Categories() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, LOCAL_ID, ID, MODEL_STATE, SYNC_STATE, TRANSACTION_TYPE, TITLE, COLOR, SORT_ORDER);
        }

        public static Query getQuery(TransactionType transactionType) {
            final Query query = Query.create()
                    .projectionLocalId(Categories.LOCAL_ID)
                    .projection(Categories.PROJECTION)
                    .selection("(" + Categories.MODEL_STATE + "=?", ModelState.Normal.asString())
                    .selection(" or " + Categories.MODEL_STATE + "=?)", ModelState.DeletedUndo.asString());

            if (transactionType != null) {
                query.selection(" and " + Tables.Categories.TRANSACTION_TYPE + "=?", transactionType.asString());
            }

            return query;
        }
    }

    public static final class Tags {
        public static final String TABLE_NAME = "tags";

        public static final Column LOCAL_ID = getLocalIdColumn(TABLE_NAME);
        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column TITLE = new Column(TABLE_NAME, "title", Column.DataType.TEXT);

        public static final String[] PROJECTION = {ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                TITLE.getName()};

        private Tags() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, LOCAL_ID, ID, MODEL_STATE, SYNC_STATE, TITLE);
        }

        public static Query getQuery() {
            return Query.create()
                    .projectionLocalId(Tags.LOCAL_ID)
                    .projection(Tags.PROJECTION)
                    .selection("(" + Tags.MODEL_STATE + "=?", ModelState.Normal.asString())
                    .selection(" or " + Tags.MODEL_STATE + "=?)", ModelState.DeletedUndo.asString());
        }

        public static final String[] PROJECTION_TRANSACTION = {"group_concat(" + ID + ",'" + CONCAT_SEPARATOR + "') as " + ID.getName(),
                "group_concat(" + TITLE + ",'" + CONCAT_SEPARATOR + "') as " + TITLE.getName()};

        public static final String[] PROJECTION_BUDGET = {"group_concat(" + ID + ",'" + CONCAT_SEPARATOR + "') as " + ID.getName(),
                "group_concat(" + TITLE + ",'" + CONCAT_SEPARATOR + "') as " + TITLE.getName()};
    }

    public static final class Transactions {
        public static final String TABLE_NAME = "transactions";

        public static final Column LOCAL_ID = getLocalIdColumn(TABLE_NAME);
        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column ACCOUNT_FROM_ID = new Column(TABLE_NAME, "account_from_id", Column.DataType.TEXT);
        public static final Column ACCOUNT_TO_ID = new Column(TABLE_NAME, "account_to_id", Column.DataType.TEXT);
        public static final Column CATEGORY_ID = new Column(TABLE_NAME, "category_id", Column.DataType.TEXT);
        public static final Column DATE = new Column(TABLE_NAME, "date", Column.DataType.DATETIME);
        public static final Column AMOUNT = new Column(TABLE_NAME, "amount", Column.DataType.INTEGER);
        public static final Column EXCHANGE_RATE = new Column(TABLE_NAME, "exchange_rate", Column.DataType.REAL);
        public static final Column NOTE = new Column(TABLE_NAME, "note", Column.DataType.TEXT);
        public static final Column STATE = new Column(TABLE_NAME, "state", Column.DataType.INTEGER);
        public static final Column TYPE = new Column(TABLE_NAME, "type", Column.DataType.INTEGER);
        public static final Column INCLUDE_IN_REPORTS = new Column(TABLE_NAME, "include_in_reports", Column.DataType.BOOLEAN, "1");

        public static final String[] PROJECTION = {ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                ACCOUNT_FROM_ID.getName(), ACCOUNT_TO_ID.getName(), CATEGORY_ID.getName(),
                DATE.getName(), AMOUNT.getName(), EXCHANGE_RATE.getName(), NOTE.getName(), STATE.getName(),
                TYPE.getName(), INCLUDE_IN_REPORTS.getName()};

        private Transactions() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, LOCAL_ID, ID, MODEL_STATE, SYNC_STATE, ACCOUNT_FROM_ID,
                    ACCOUNT_TO_ID, CATEGORY_ID, DATE, AMOUNT, EXCHANGE_RATE, NOTE, STATE, TYPE, INCLUDE_IN_REPORTS);
        }

        public static Query getQuery() {
            return Query.create()
                    .projectionLocalId(Transactions.LOCAL_ID)
                    .projection(Transactions.PROJECTION)
                    .projection(Accounts.PROJECTION_ACCOUNT_FROM)
                    .projection(Accounts.PROJECTION_ACCOUNT_TO)
                    .projection(Categories.PROJECTION)
                    .projection(Tags.PROJECTION_TRANSACTION)
                    .selection("(" + Transactions.MODEL_STATE + "=?", ModelState.Normal.asString())
                    .selection(" or " + Transactions.MODEL_STATE + "=?)", ModelState.DeletedUndo.asString())
                    .groupBy(ID.getName())
                    .sortOrder(Transactions.STATE + " desc")
                    .sortOrder(Transactions.DATE + " desc");
        }
    }

    public static final class TransactionTags {
        public static final String TABLE_NAME = "transaction_tags";

        public static final Column TRANSACTION_ID = new Column(TABLE_NAME, "transaction_id", Column.DataType.TEXT);
        public static final Column TAG_ID = new Column(TABLE_NAME, "tag_id", Column.DataType.TEXT);

        public static final String[] PROJECTION = {TRANSACTION_ID.getName(), TAG_ID.getName()};

        private TransactionTags() {
        }

        public static String createScript() {
            return makeRelationshipCreateScript(TABLE_NAME, TRANSACTION_ID, TAG_ID);
        }
    }

    public static final class Budgets {
        public static final String TABLE_NAME = "budgets";

        public static final Column LOCAL_ID = getLocalIdColumn(TABLE_NAME);
        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column CATEGORY_ID = new Column(TABLE_NAME, "category_id", Column.DataType.TEXT);
        public static final Column AMOUNT = new Column(TABLE_NAME, "amount", Column.DataType.INTEGER);
        public static final Column DATE_START = new Column(TABLE_NAME, "date_start", Column.DataType.DATETIME);
        public static final Column RECURRENCE = new Column(TABLE_NAME, "recurrence", Column.DataType.TEXT);

        public static final String[] PROJECTION = {ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                AMOUNT.getName(), CATEGORY_ID.getName(), DATE_START.getName(), RECURRENCE.getName()};

        private Budgets() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, LOCAL_ID, ID, MODEL_STATE, SYNC_STATE, AMOUNT, CATEGORY_ID, DATE_START, RECURRENCE);
        }

        public static Query getQuery() {
            return Query.create()
                    .projectionLocalId(Budgets.LOCAL_ID)
                    .projection(Budgets.PROJECTION)
                    .projection(Categories.PROJECTION)
                    .projection(Tags.PROJECTION_BUDGET)
                    .selection("(" + Budgets.MODEL_STATE + "=?", ModelState.Normal.asString())
                    .selection(" or " + Budgets.MODEL_STATE + "=?)", ModelState.DeletedUndo.asString())
                    .groupBy(ID.getName())
                    .sortOrder(Categories.TITLE.getName())
                    .sortOrder(Budgets.CATEGORY_ID.getName());
        }
    }

    public static final class BudgetTags {
        public static final String TABLE_NAME = "budget_tags";

        public static final Column BUDGET_ID = new Column(TABLE_NAME, "budget_id", Column.DataType.TEXT);
        public static final Column TAG_ID = new Column(TABLE_NAME, "tag_id", Column.DataType.TEXT);

        public static final String[] PROJECTION = {BUDGET_ID.getName(), TAG_ID.getName()};

        private BudgetTags() {
        }

        public static String createScript() {
            return makeRelationshipCreateScript(TABLE_NAME, BUDGET_ID, TAG_ID);
        }
    }
}
