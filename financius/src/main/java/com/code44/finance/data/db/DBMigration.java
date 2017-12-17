package com.code44.finance.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.code44.finance.common.model.DecimalSeparator;
import com.code44.finance.common.model.GroupSeparator;
import com.code44.finance.common.model.ModelState;
import com.code44.finance.common.model.SymbolPosition;
import com.code44.finance.common.model.TransactionState;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.Query;
import com.code44.finance.data.model.Account;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.CurrencyFormat;
import com.code44.finance.data.model.SyncState;
import com.code44.finance.data.model.Tag;
import com.code44.finance.data.model.Transaction;
import com.code44.finance.data.providers.TransactionsProvider;
import com.code44.finance.money.CurrenciesManager;
import com.code44.finance.utils.IOUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DBMigration {
    private DBMigration() {
    }

    /**
     * 46 - v0.10.0
     */
    public static void upgradeV19(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(Tables.Tags.createScript());
            DBHelper.createIndex(db, Tables.Tags.ID);
            db.execSQL(Tables.TransactionTags.createScript());

            v19EnsureIds(db, "currencies");
            v19EnsureIds(db, "accounts");
            v19EnsureIds(db, "categories");
            v19EnsureIds(db, "transactions");

            final String tempCurrenciesTable = v19MigrateCurrencies(db);
            final String tempAccountsTable = v19MigrateAccounts(db, tempCurrenciesTable);
            final String tempCategoriesTable = v19MigrateCategories(db);
            final String tempTransactionsTable = v19MigrateTransactions(db, tempAccountsTable, tempCategoriesTable);

            db.execSQL("drop table " + tempCurrenciesTable);
            db.execSQL("drop table " + tempAccountsTable);
            db.execSQL("drop table " + tempCategoriesTable);
            db.execSQL("drop table " + tempTransactionsTable);

            TransactionsProvider.updateAllAccountsBalances(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 57 - v0.14.0
     */
    public static void upgradeV20(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            v20FixCategoriesIds(db);
            v20FixAccounts(db);
            v20FixTransactions(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 64 - v0.14.7
     */
    public static void upgradeV21(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            final ContentValues values = new ContentValues();
            values.put(Tables.Transactions.ACCOUNT_TO_ID.getName(), (String) null);
            values.put(Tables.Transactions.STATE.getName(), TransactionState.Pending.asInt());
            db.update(Tables.Transactions.TABLE_NAME, values, Tables.Transactions.ACCOUNT_TO_ID + "=2", null);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 65 - v0.14.8
     */
    public static void upgradeV22(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            fixTransactionsWithNotExistingAccounts(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 78 - v0.18.0
     */
    public static void upgradeV23(SQLiteDatabase db, CurrenciesManager currenciesManager) {
        try {
            db.beginTransaction();
            db.execSQL(Tables.ExchangeRates.createScript());
            db.execSQL("alter table " + Tables.Accounts.TABLE_NAME + " add column " + Tables.Accounts.CURRENCY_CODE.getName() + " " + Column.DataType.TEXT);

            final Map<String, String> currencyFormats = new HashMap<>();
            Cursor cursor = Tables.CurrencyFormats.getQuery().projection("currencies_is_default").from(db, Tables.CurrencyFormats.TABLE_NAME).execute();
            if (cursor != null && cursor.moveToFirst()) {
                final int iIsDefault = cursor.getColumnIndex("currencies_is_default");
                do {
                    final CurrencyFormat currencyFormat = CurrencyFormat.from(cursor);
                    currencyFormats.put(currencyFormat.getId(), currencyFormat.getCode());
                    if (cursor.getInt(iIsDefault) != 0) {
                        currenciesManager.setMainCurrencyCode(currencyFormat.getCode());
                    }
                } while (cursor.moveToNext());
            }
            IOUtils.closeQuietly(cursor);

            cursor = Tables.Accounts.getQuery().clearSelection().clearArgs().clearSort().clearSort().projection("accounts_currency_id").from(db, Tables.Accounts.TABLE_NAME).execute();
            if (cursor != null && cursor.moveToFirst()) {
                final int iLocalId = cursor.getColumnIndex(Tables.Accounts.LOCAL_ID.getName());
                final int iCurrencyId = cursor.getColumnIndex("accounts_currency_id");
                final ContentValues values = new ContentValues();
                do {
                    values.put(Tables.Accounts.CURRENCY_CODE.getName(), currencyFormats.get(cursor.getString(iCurrencyId)));
                    db.update(Tables.Accounts.TABLE_NAME, values, Tables.Accounts.LOCAL_ID + "=?", new String[]{String.valueOf(cursor.getLong(iLocalId))});
                } while (cursor.moveToNext());
            }
            IOUtils.closeQuietly(cursor);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 82 - v0.18.3-debug-2
     */
    public static void upgradeV24(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            db.execSQL(Tables.Budgets.createScript());
            db.execSQL(Tables.BudgetTags.createScript());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void fixTransactionsWithNotExistingAccounts(SQLiteDatabase db) {
        final String tables = Tables.Transactions.TABLE_NAME
                + " left join " + Tables.Accounts.TABLE_NAME + " as " + Tables.Accounts.TEMP_TABLE_NAME_FROM_ACCOUNT
                + " on " + Tables.Accounts.ID.getNameWithTable(Tables.Accounts.TEMP_TABLE_NAME_FROM_ACCOUNT)
                + "=" + Tables.Transactions.ACCOUNT_FROM_ID;

        final Cursor cursor = Query.create()
                .projectionLocalId(Tables.Transactions.LOCAL_ID)
                .selection("(" + Tables.Transactions.TYPE + "=" + TransactionType.Expense.asInt())
                .selection("or " + Tables.Transactions.TYPE + "=" + TransactionType.Transfer.asInt() + ")")
                .selection(" and " + Tables.Accounts.ID.getNameWithTable(Tables.Accounts.TEMP_TABLE_NAME_FROM_ACCOUNT) + " is null")
                .from(db, tables)
                .execute();
        if (cursor.moveToFirst()) {
            do {
                final ContentValues values = new ContentValues();
                values.put(Tables.Transactions.ACCOUNT_FROM_ID.getName(), (String) null);
                values.put(Tables.Transactions.STATE.getName(), TransactionState.Pending.asInt());
                db.update(Tables.Transactions.TABLE_NAME, values, Tables.Transactions.LOCAL_ID + "=?", new String[]{String.valueOf(cursor.getLong(0))});
            } while (cursor.moveToNext());

            TransactionsProvider.updateAllAccountsBalances(db);
        }
        IOUtils.closeQuietly(cursor);
    }

    private static void v19EnsureIds(SQLiteDatabase db, String tableName) {
        final String serverIdName = tableName + "_server_id";
        final Cursor cursor = db.query(tableName, new String[]{BaseColumns._ID}, null, null, null, null, null);
        final ContentValues values = new ContentValues();
        if (cursor != null && cursor.moveToFirst()) {
            values.put(serverIdName, UUID.randomUUID().toString());
            db.update(tableName, values, BaseColumns._ID + "=?", new String[]{String.valueOf(cursor.getLong(0))});
        }
    }

    private static String v19MigrateCurrencies(SQLiteDatabase db) {
        final String oldTableName = "currencies";
        final String tempTableName = "temp_" + oldTableName;
        db.execSQL("alter table " + oldTableName + " rename to " + tempTableName);
        db.execSQL(Tables.CurrencyFormats.createScript());
        DBHelper.createIndex(db, Tables.CurrencyFormats.ID);

        final String[] projection = {oldTableName + "_server_id", oldTableName + "_code",
                oldTableName + "_symbol", oldTableName + "_decimals",
                oldTableName + "_decimal_separator", oldTableName + "_group_separator",
                oldTableName + "_symbol_format", oldTableName + "_is_default",
                oldTableName + "_exchange_rate"};
        final String selection = oldTableName + "_delete_state = 0";
        final Cursor cursor = db.query(tempTableName, projection, selection, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final CurrencyFormat currencyFormat = new CurrencyFormat();
            currencyFormat.setModelState(ModelState.Normal);
            currencyFormat.setSyncState(SyncState.None);
            do {
                currencyFormat.setId(cursor.getString(0));
                currencyFormat.setCode(cursor.getString(1));
                currencyFormat.setSymbol(cursor.getString(2));
                final String symbolPositionOld = cursor.getString(6);
                final SymbolPosition symbolPosition = "LF".equals(symbolPositionOld) ? SymbolPosition.FarLeft :
                        "LC".equals(symbolPositionOld) ? SymbolPosition.CloseLeft :
                                "RC".equals(symbolPositionOld) ? SymbolPosition.CloseRight : SymbolPosition.FarRight;
                currencyFormat.setSymbolPosition(symbolPosition);
                currencyFormat.setDecimalSeparator(DecimalSeparator.fromSymbol(cursor.getString(4)));
                currencyFormat.setGroupSeparator(GroupSeparator.fromSymbol(cursor.getString(5)));
                currencyFormat.setDecimalCount(cursor.getInt(3));
                db.insert(Tables.CurrencyFormats.TABLE_NAME, null, currencyFormat.asContentValues());
            } while (cursor.moveToNext());
        }

        return tempTableName;
    }

    private static String v19MigrateAccounts(SQLiteDatabase db, String tempCurrenciesTable) {
        final String oldTableName = "accounts";
        final String tempTableName = "temp_" + oldTableName;
        db.execSQL("alter table " + oldTableName + " rename to " + tempTableName);
        db.execSQL(Tables.Accounts.createScript());
        DBHelper.createIndex(db, Tables.Accounts.ID);

        final String[] projection = {oldTableName + "_server_id", "currencies_server_id",
                oldTableName + "_title", oldTableName + "_note", oldTableName + "_show_in_totals"};
        final String selection = oldTableName + "_delete_state = 0 and " + oldTableName + "_origin = 1";
        final String tables = tempTableName + " inner join " + tempCurrenciesTable + " on " +
                tempCurrenciesTable + "._id=" + oldTableName + "_currency_id";
        final Cursor cursor = db.query(tables, projection, selection, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final CurrencyFormat currencyFormat = new CurrencyFormat();
            final Account account = new Account();
            account.setModelState(ModelState.Normal);
            account.setSyncState(SyncState.None);
            account.setCurrencyCode(currencyFormat.getCode());
            do {
                account.setId(cursor.getString(0));
                currencyFormat.setId(cursor.getString(1));
                account.setTitle(cursor.getString(2));
                account.setNote(cursor.getString(3));
                account.setIncludeInTotals(cursor.getInt(4) != 0);
                db.insert(Tables.Accounts.TABLE_NAME, null, account.asContentValues());
            } while (cursor.moveToNext());
        }

        return tempTableName;
    }

    private static String v19MigrateCategories(SQLiteDatabase db) {
        final String oldTableName = "categories";
        final String tempTableName = "temp_" + oldTableName;
        db.execSQL("alter table " + oldTableName + " rename to " + tempTableName);
        db.execSQL(Tables.Categories.createScript());
        DBHelper.createIndex(db, Tables.Categories.ID);

        final String[] projection = {oldTableName + "_server_id", oldTableName + "_title",
                oldTableName + "_color", oldTableName + "_type", oldTableName + "_order",
                oldTableName + "_level"};
        final String selection = oldTableName + "_delete_state = 0 and " + oldTableName + "_origin = 1";
        final Cursor cursor = db.query(tempTableName, projection, selection, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final Category category = new Category();
            category.setModelState(ModelState.Normal);
            category.setSyncState(SyncState.None);
            final Tag tag = new Tag();
            tag.setModelState(ModelState.Normal);
            tag.setSyncState(SyncState.None);
            do {
                final int level = cursor.getInt(5);
                if (level == 1) {
                    category.setId(cursor.getString(0));
                    category.setTitle(cursor.getString(1));
                    category.setColor(cursor.getInt(2));
                    category.setSortOrder(cursor.getInt(4));
                    final int categoryType = cursor.getInt(3);
                    category.setTransactionType(categoryType == 0 ? TransactionType.Income : categoryType == 1 ? TransactionType.Expense : TransactionType.Transfer);
                    db.insert(Tables.Categories.TABLE_NAME, null, category.asContentValues());
                } else {
                    tag.setId(cursor.getString(0));
                    tag.setTitle(cursor.getString(1));
                    db.insert(Tables.Tags.TABLE_NAME, null, tag.asContentValues());
                }
            } while (cursor.moveToNext());
        }

        return tempTableName;
    }

    private static String v19MigrateTransactions(SQLiteDatabase db, String tempAccountsTable, String tempCategoriesTable) {
        final String oldTableName = "transactions";
        final String tempTableName = "temp_" + oldTableName;
        db.execSQL("alter table " + oldTableName + " rename to " + tempTableName);
        db.execSQL(Tables.Transactions.createScript());
        DBHelper.createIndex(db, Tables.Transactions.ID);

        final String tempFromAccountsTable = tempAccountsTable + "_from";
        final String tempToAccountsTable = tempAccountsTable + "_to";
        final String tempChildCategoriesTable = tempCategoriesTable + "_child";
        final String tempParentCategoriesTable = tempCategoriesTable + "_parent";
        final String tables = tempTableName
                + " inner join " + tempAccountsTable + " as " + tempFromAccountsTable + " on " + tempFromAccountsTable + "._id=" + oldTableName + "_account_from_id"
                + " inner join " + tempAccountsTable + " as " + tempToAccountsTable + " on " + tempToAccountsTable + "._id=" + oldTableName + "_account_to_id"
                + " inner join " + tempCategoriesTable + " as " + tempChildCategoriesTable + " on " + tempChildCategoriesTable + "._id=" + oldTableName + "_category_id"
                + " left join " + tempCategoriesTable + " as " + tempParentCategoriesTable + " on " + tempParentCategoriesTable + "._id=" + tempChildCategoriesTable + ".categories_parent_id";
        final String[] projection = {oldTableName + "_server_id", oldTableName + "_date",
                oldTableName + "_amount", oldTableName + "_exchange_rate", oldTableName + "_note",
                oldTableName + "_state", oldTableName + "_show_in_totals",
                tempFromAccountsTable + ".accounts_server_id", tempToAccountsTable + ".accounts_server_id",
                tempChildCategoriesTable + "._id", tempChildCategoriesTable + ".categories_server_id",
                tempChildCategoriesTable + ".categories_level", tempChildCategoriesTable + ".categories_type",
                tempParentCategoriesTable + ".categories_server_id"};
        final String selection = oldTableName + "_delete_state = 0";

        final Cursor cursor = db.query(tables, projection, selection, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final ContentValues values = new ContentValues();
            final Category category = new Category();
            final Account accountFrom = new Account();
            final Account accountTo = new Account();
            final Transaction transaction = new Transaction();
            transaction.setModelState(ModelState.Normal);
            transaction.setSyncState(SyncState.None);

            do {
                transaction.setId(cursor.getString(0));
                transaction.setDate(cursor.getLong(1));
                transaction.setAmount(Math.round(cursor.getDouble(2) * 100));
                transaction.setExchangeRate(cursor.getDouble(3));
                transaction.setNote(cursor.getString(4));
                transaction.setTransactionState(TransactionState.fromInt(cursor.getInt(5) + 1));
                transaction.setIncludeInReports(cursor.getInt(6) != 0);

                final int categoryType = cursor.getInt(12);
                transaction.setTransactionType(categoryType == 0 ? TransactionType.Income : categoryType == 1 ? TransactionType.Expense : TransactionType.Transfer);

                final long categoryId = cursor.getLong(9);
                if (categoryId > 3) {
                    final int level = cursor.getInt(11);
                    if (level == 1) {
                        category.setId(cursor.getString(10));
                    } else {
                        category.setId(cursor.getString(13));
                        values.clear();
                        values.put(Tables.TransactionTags.TRANSACTION_ID.getName(), transaction.getId());
                        values.put(Tables.TransactionTags.TAG_ID.getName(), cursor.getString(10));
                        db.insert(Tables.TransactionTags.TABLE_NAME, null, values);
                    }
                    transaction.setCategory(category);
                } else {
                    transaction.setCategory(null);
                }

                switch (transaction.getTransactionType()) {
                    case Expense:
                        accountFrom.setId(cursor.getString(7));
                        transaction.setAccountFrom(accountFrom);
                        transaction.setAccountTo(null);
                        break;
                    case Income:
                        transaction.setAccountFrom(null);
                        accountTo.setId(cursor.getString(8));
                        transaction.setAccountTo(accountTo);
                        break;
                    case Transfer:
                        accountFrom.setId(cursor.getString(7));
                        transaction.setAccountFrom(accountFrom);
                        accountTo.setId(cursor.getString(8));
                        transaction.setAccountTo(accountTo);
                        break;
                }

                final ContentValues transactionValues = transaction.asContentValues();
                transactionValues.remove(Tables.Tags.ID.getName());
                db.insert(Tables.Transactions.TABLE_NAME, null, transactionValues);
            } while (cursor.moveToNext());
        }

        return tempTableName;
    }

    private static void v20FixCategoriesIds(SQLiteDatabase db) {
        final Cursor cursor = Query.create()
                .projection(Tables.Categories.ID.getName())
                .from(db, Tables.Categories.TABLE_NAME)
                .execute();

        if (cursor != null && cursor.moveToFirst()) {
            final ContentValues values = new ContentValues();
            final String[] args = new String[1];
            do {
                final String oldId = cursor.getString(0);
                final String newId = UUID.randomUUID().toString();

                args[0] = oldId;

                values.clear();
                values.put(Tables.Categories.ID.getName(), newId);
                db.update(Tables.Categories.TABLE_NAME, values, Tables.Categories.ID + "=?", args);

                values.clear();
                values.put(Tables.Transactions.CATEGORY_ID.getName(), newId);
                db.update(Tables.Transactions.TABLE_NAME, values, Tables.Transactions.CATEGORY_ID + "=?", args);
            } while (cursor.moveToNext());
        }

        IOUtils.closeQuietly(cursor);
    }

    private static void v20FixAccounts(SQLiteDatabase db) {
        final ContentValues values = new ContentValues();
        values.put(Tables.Accounts.NOTE.getName(), "");
        db.update(Tables.Accounts.TABLE_NAME, values, Tables.Accounts.NOTE + " is null", null);
    }

    private static void v20FixTransactions(SQLiteDatabase db) {
        final ContentValues values = new ContentValues();
        values.put(Tables.Transactions.NOTE.getName(), "");
        db.update(Tables.Transactions.TABLE_NAME, values, Tables.Transactions.NOTE + " is null", null);

        final String[] args = new String[1];
        args[0] = TransactionType.Transfer.asString();
        values.clear();
        values.put(Tables.Transactions.CATEGORY_ID.getName(), "");
        db.update(Tables.Transactions.TABLE_NAME, values, Tables.Transactions.TYPE + "=?", args);

        args[0] = TransactionType.Expense.asString();
        values.clear();
        values.put(Tables.Transactions.ACCOUNT_TO_ID.getName(), "");
        db.update(Tables.Transactions.TABLE_NAME, values, Tables.Transactions.TYPE + "=?", args);

        args[0] = TransactionType.Income.asString();
        values.clear();
        values.put(Tables.Transactions.ACCOUNT_FROM_ID.getName(), "");
        db.update(Tables.Transactions.TABLE_NAME, values, Tables.Transactions.TYPE + "=?", args);
    }
}
