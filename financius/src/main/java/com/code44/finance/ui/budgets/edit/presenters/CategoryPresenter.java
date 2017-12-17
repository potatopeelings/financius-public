package com.code44.finance.ui.budgets.edit.presenters;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.code44.finance.R;
import com.code44.finance.data.model.Category;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.Presenter;
import com.code44.finance.utils.ThemeUtils;

public class CategoryPresenter extends Presenter {
    private final ImageView colorImageView;
    private final Button categoryButton;

    public CategoryPresenter(BaseActivity activity, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        colorImageView = findView(activity, R.id.colorImageView);
        categoryButton = findView(activity, R.id.categoryButton);

        categoryButton.setOnClickListener(clickListener);
        categoryButton.setOnLongClickListener(longClickListener);
    }

    public void setCategory(Category category) {
        colorImageView.setColorFilter(getCategoryColor(category));
        categoryButton.setText(category == null ? null : category.getTitle());
    }

    private int getCategoryColor(Category category) {
        if (category == null) {
            return ThemeUtils.getColor(categoryButton.getContext(), R.attr.textColorTransparent);
        } else {
            return category.getColor();
        }
    }
}
