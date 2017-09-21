package com.referendum.uoctubre.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.referendum.uoctubre.R;
import com.referendum.uoctubre.utils.StringsManager;

public class LocalStringImageView extends AppCompatImageView {

    public LocalStringImageView(Context context) {
        super(context);
        init(null, context);
    }

    public LocalStringImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public LocalStringImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    public void init(AttributeSet attrs, final Context context) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StringAttributes);
        final String contentDescriptionStringId = a.getString(R.styleable.StringAttributes_content_description_string_id);
        setContentDescriptionString(contentDescriptionStringId);
        a.recycle();
    }

    private void setContentDescriptionString(String contentDescriptionStringId) {
        if (TextUtils.isEmpty(contentDescriptionStringId)) {
            return;
        }

        if (this.isInEditMode()) {
            setContentDescription(contentDescriptionStringId);
        } else {
            setContentDescription(StringsManager.getString(contentDescriptionStringId));
        }
    }

}