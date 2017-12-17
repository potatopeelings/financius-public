package com.code44.finance.ui.budgets.edit.presenters;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;

import com.code44.finance.R;
import com.code44.finance.data.model.Tag;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.Presenter;
import com.code44.finance.utils.TextBackgroundSpan;
import com.code44.finance.utils.ThemeUtils;

import java.util.Collections;
import java.util.List;

public class MultipleTagsPresenter extends Presenter {
    private final Button tagsButton;

    private final int tagBackgroundColor;
    private final float tagBackgroundRadius;

    public MultipleTagsPresenter(BaseActivity activity, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        tagsButton = findView(activity, R.id.tagsButton);

        tagBackgroundColor = ThemeUtils.getColor(tagsButton.getContext(), R.attr.backgroundColorSecondary);
        tagBackgroundRadius = tagsButton.getResources().getDimension(R.dimen.tag_radius);
        tagsButton.setOnClickListener(clickListener);
        tagsButton.setOnLongClickListener(longClickListener);
    }

    public void setTags(List<Tag> tags) {
        if (tags == null) {
            tags = Collections.emptyList();
        }

        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        for (Tag tag : tags) {
            ssb.append(tag.getTitle());
            ssb.setSpan(new TextBackgroundSpan(tagBackgroundColor, tagBackgroundRadius), ssb.length() - tag.getTitle().length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(" ");
        }
        tagsButton.setText(ssb);
    }
}
