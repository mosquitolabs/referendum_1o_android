package com.referendum.uoctubre.main;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import com.referendum.uoctubre.R;


public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mToolbar = findViewById(R.id.toolbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, color));
        }
    }

    protected boolean isActivitySafe() {
        return !isFinishing() && !isDestroyed();
    }
}
