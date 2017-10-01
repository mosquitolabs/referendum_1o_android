package com.referendum.uoctubre.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;

import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.Marker;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.referendum.uoctubre.R;

import java.util.List;

public class MapClusterOptionsProvider implements ClusterOptionsProvider {

    private Bitmap baseBitmap;
    private LruCache<Integer, BitmapDescriptor> cache = new LruCache<>(128);

    private Paint bigFontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint littleFontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect bounds = new Rect();

    private ClusterOptions clusterOptions = new ClusterOptions().anchor(0.5f, 0.5f);

    public MapClusterOptionsProvider(Resources resources) {
        int poiSize = resources.getDimensionPixelSize(R.dimen.map_poi_size);

        Drawable d = ResourcesCompat.getDrawable(resources, R.drawable.ic_wrapper_poi_cluster, null);
        d.setBounds(0, 0, poiSize, poiSize);
        Bitmap bitmap = Bitmap.createBitmap(poiSize, poiSize, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        d.draw(canvas);
        baseBitmap = bitmap;

        littleFontPaint.setColor(Color.BLACK);
        littleFontPaint.setTextAlign(Align.CENTER);
        littleFontPaint.setFakeBoldText(true);
        littleFontPaint.setTextSize(resources.getDimension(R.dimen.map_marker_cluster_text_size_small));
        bigFontPaint.setColor(Color.BLACK);
        bigFontPaint.setTextAlign(Align.CENTER);
        bigFontPaint.setFakeBoldText(true);
        bigFontPaint.setTextSize(resources.getDimension(R.dimen.map_marker_cluster_text_size_big));
    }

    @Override
    public ClusterOptions getClusterOptions(List<Marker> markers) {
        int markersCount = markers.size();

        if (markersCount > 10 && markersCount < 25) {
            markersCount = 11;
        } else if (markersCount >= 25 && markersCount < 50) {
            markersCount = 26;
        } else if (markersCount >= 50 && markersCount < 100) {
            markersCount = 51;
        } else if (markersCount >= 100 && markersCount < 250) {
            markersCount = 101;
        } else if (markersCount >= 250 && markersCount < 500) {
            markersCount = 251;
        } else if (markersCount >= 500 && markersCount < 1000) {
            markersCount = 501;
        } else if (markersCount >= 1000 && markersCount < 2000) {
            markersCount = 1001;
        } else if (markersCount >= 2000 && markersCount < 3000) {
            markersCount = 2001;
        } else if (markersCount >= 3000 && markersCount < 4000) {
            markersCount = 3001;
        } else if (markersCount >= 4000 && markersCount < 5000) {
            markersCount = 4001;
        } else if (markersCount >= 5000) {
            markersCount = 5001;
        }

        BitmapDescriptor cachedIcon = cache.get(markersCount);
        if (cachedIcon != null) {
            return clusterOptions.icon(cachedIcon);
        }
        Bitmap bitmap = baseBitmap.copy(Config.ARGB_8888, true);

        String text;

        if (markersCount > 10) {
            text = String.valueOf(markersCount - 1) + "+";
        } else {
            text = String.valueOf(markersCount);
        }
        if (markersCount > 100) {
            littleFontPaint.getTextBounds(text, 0, text.length(), bounds);
            float x = bitmap.getWidth() / 2.0f;
            float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;

            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(text, x, y, littleFontPaint);
        } else {
            bigFontPaint.getTextBounds(text, 0, text.length(), bounds);
            float x = bitmap.getWidth() / 2.0f;
            float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;

            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(text, x, y, bigFontPaint);
        }

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        cache.put(markersCount, icon);
        return clusterOptions.icon(icon);
    }
}
