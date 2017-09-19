package com.referendum.uoctubre.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.model.Image;
import com.referendum.uoctubre.utils.GenericRecyclerViewViewHolder;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<GenericRecyclerViewViewHolder> {
    private OnImageClickedListener onImageClickedListener;
    private List<Image> imageList;

    public ImageAdapter(List<Image> imageList, OnImageClickedListener onImageClickedListener) {
        this.imageList = imageList;
        this.onImageClickedListener = onImageClickedListener;
    }

    @Override
    public GenericRecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        GenericRecyclerViewViewHolder viewHolder = new GenericRecyclerViewViewHolder(itemView);
        viewHolder.setView("imageName", itemView.findViewById(R.id.image_name));
        viewHolder.setView("imageView", itemView.findViewById(R.id.image_view));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GenericRecyclerViewViewHolder holder, int position) {
        final Image image = imageList.get(position);

        Glide.with(holder.getView("imageView").getContext())
                .load(image.getUrl())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.getView("imageView", ImageView.class));

        holder.getView("imageName", TextView.class).setText(image.getName());

        holder.getView("imageView").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageClickedListener.onImageClicked(image, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public interface OnImageClickedListener {
        void onImageClicked(Image image, View view);
    }
}
