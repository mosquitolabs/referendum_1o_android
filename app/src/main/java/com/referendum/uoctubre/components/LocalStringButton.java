package com.referendum.uoctubre.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.referendum.uoctubre.R;
import com.referendum.uoctubre.utils.StringsManager;

public class LocalStringButton extends AppCompatTextView {

    public LocalStringButton(Context context) {
        super(context);
        init(null, context);
    }

    public LocalStringButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public LocalStringButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    public void init(AttributeSet attrs, final Context context) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StringAttributes);
        final String stringId = a.getString(R.styleable.StringAttributes_string_id);
        setString(stringId);
        a.recycle();
    }

    private void setString(String stringId) {
        if (TextUtils.isEmpty(stringId)) {
            return;
        }

        if (this.isInEditMode()) {
            setText(stringId);
        } else {
            setText(StringsManager.getString(stringId));
        }
    }

}