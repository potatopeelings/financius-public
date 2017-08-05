package com.code44.finance.ui.tags.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.data.model.Tag;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.utils.analytics.Analytics;

import java.util.List;

public class TagsActivity extends BaseActivity {
    private static final String READ_ONLY = "READ_ONLY";

    public static void start(Context context) {
        final Intent intent = makeIntentForActivity(context, TagsActivity.class);
        TagsActivityPresenter.addViewExtras(intent);
        startActivity(context, intent);
    }

    public static void startMultiSelect(Activity activity, int requestCode, List<Tag> selectedTags) {
        startMultiSelect(activity, requestCode, selectedTags, false);
    }

    public static void startMultiSelect(Activity activity, int requestCode, List<Tag> selectedTags, boolean isReadOnly) {
        final Intent intent = makeIntentForActivity(activity, TagsActivity.class);
        intent.putExtra(READ_ONLY, isReadOnly);
        TagsActivityPresenter.addMultiSelectExtras(intent, selectedTags);
        startActivityForResult(activity, intent, requestCode);
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_tags);
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
        return new TagsActivityPresenter(getIntent().getExtras().getBoolean(READ_ONLY));
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.TagList;
    }
}
