package com.code44.finance.utils.filter;

import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.model.Account;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Tag;
import com.code44.finance.utils.EventBus;

import java.util.Collections;
import java.util.List;

public class TransactionFilter {

    private TransactionType transactionType;
    private Long amountFrom;
    private Long amountTo;
    private Long dateFrom;
    private Long dateTo;
    private Account accountFrom;
    private Account accountTo;
    private Category category;
    private List<Tag> tags;
    private String note;

    public TransactionFilter() {
        reset();
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Long getAmountFrom() {
        return amountFrom;
    }

    public void setAmountFrom(Long amountFrom) {
        this.amountFrom = amountFrom;
    }

    public Long getAmountTo() {
        return amountTo;
    }

    public void setAmountTo(Long amountTo) {
        this.amountTo = amountTo;
    }

    public Long getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Long dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Long getDateTo() {
        return dateTo;
    }

    public void setDateTo(Long dateTo) {
        this.dateTo = dateTo;
    }

    public Account getAccountFrom() {
        return (transactionType == TransactionType.Income) ? null : accountFrom;
    }

    public void setAccountFrom(Account accountFrom) {
        this.accountFrom = accountFrom;
    }

    public Account getAccountTo() {
        return (transactionType == TransactionType.Expense) ? null : accountTo;
    }

    public void setAccountTo(Account accountTo) {
        this.accountTo = accountTo;
    }

    public Category getCategory() {
        return (transactionType == TransactionType.Transfer) ? null : category;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isNoteSet() {
        return this.note != null;
    }


    public boolean isApplied() {
        return (getTransactionType() != null) ||
                (getAmountFrom() != null) ||
                (getAmountTo() != null) ||
                (getDateFrom() != null) ||
                (getDateTo() != null) ||
                (getAccountFrom() != null) ||
                (getAccountTo() != null) ||
                (getCategory() != null) ||
                (getTags().size() != 0) ||
                (getNote() != null);
    }

    public void reset() {
        setTransactionType(null);
        setAmountFrom(null);
        setAmountTo(null);
        setDateFrom(null);
        setDateTo(null);
        setAccountFrom(null);
        setAccountTo(null);
        setCategory(null);
        setTags(null);
        setNote(null);
    }
}
