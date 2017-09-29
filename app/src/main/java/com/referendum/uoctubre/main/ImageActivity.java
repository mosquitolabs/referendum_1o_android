package com.referendum.uoctubre.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.model.Image;
import com.referendum.uoctubre.utils.ImageFileProvider;
import com.referendum.uoctubre.utils.StringsManager;
import com.referendum.uoctubre.utils.TouchImageView;

import java.io.File;

public class ImageActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE = "image";

    private Image image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        image = (Image) getIntent().getSerializableExtra(EXTRA_IMAGE);

        if (image == null) {
            finish();
        } else {
            TouchImageView imageView = findViewById(R.id.activity_image_image);
            Glide.with(this)
                    .load(image.getUrl())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(imageView);
            setTitle(image.getName());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_share:
                new ShareTask(this).execute(image.getUrl());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            switch (menuItem.getItemId()) {
                case R.id.action_share:
                    menuItem.setTitle(StringsManager.getString("menu_share"));
                    break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    static class ShareTask extends AsyncTask<String, Void, File> {
        private final Context context;

        public ShareTask(Context context) {
            this.context = context;
        }

        @Override
        protected File doInBackground(String... params) {
            String url = params[0];
            try {
                return Glide
                        .with(context)
                        .downloadOnly()
                        .load(url)
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                Toast.makeText(context, StringsManager.getString("image_share_error"), Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = ImageFileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", result);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(intent, StringsManager.getString("image_share_title")));
        }
    }
}
