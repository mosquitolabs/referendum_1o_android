package com.referendum.uoctubre.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.referendum.uoctubre.R;
import com.referendum.uoctubre.utils.StringsManager;

public class LocalStringTextInputEditText extends TextInputEditText {

    public LocalStringTextInputEditText(Context context) {
        super(context);
        init(null, context);
    }

    public LocalStringTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public LocalStringTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    public void init(AttributeSet attrs, final Context context) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StringAttributes);
        final String stringId = a.getString(R.styleable.StringAttributes_string_id);
        final String hintStringId = a.getString(R.styleable.StringAttributes_hint_string_id);
        setString(stringId);
        setHintString(hintStringId);
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

    private void setHintString(String hintStringId) {
        if (TextUtils.isEmpty(hintStringId)) {
            return;
        }

        if (this.isInEditMode()) {
            setHint(hintStringId);
        } else {
            setHint(StringsManager.getString(hintStringId));
        }
    }

}