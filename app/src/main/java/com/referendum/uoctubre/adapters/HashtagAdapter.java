package com.referendum.uoctubre.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.referendum.uoctubre.R;
import com.referendum.uoctubre.model.Hashtag;
import com.referendum.uoctubre.utils.GenericRecyclerViewViewHolder;

import java.util.List;

public class HashtagAdapter extends RecyclerView.Adapter<GenericRecyclerViewViewHolder> {
    private OnHashtagRemovedListener onHashtagRemovedListener;
    private List<Hashtag> hashtagsList;

    public HashtagAdapter(List<Hashtag> hashtagsList, OnHashtagRemovedListener onHashtagRemovedListener) {
        this.hashtagsList = hashtagsList;
        this.onHashtagRemovedListener = onHashtagRemovedListener;
    }

    @Override
    public GenericRecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hashtag, parent, false);
        GenericRecyclerViewViewHolder viewHolder = new GenericRecyclerViewViewHolder(itemView);
        viewHolder.setView("hashtagName", itemView.findViewById(R.id.hashtag_name));
        viewHolder.setView("deleteButton", itemView.findViewById(R.id.delete_button));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GenericRecyclerViewViewHolder holder, int position) {
        final Hashtag hashtag = hashtagsList.get(position);

        if (hashtag.isUserAdded()) {
            holder.getView("deleteButton").setVisibility(View.VISIBLE);
            holder.getView("deleteButton").setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onHashtagRemovedListener.onHashTagRemoved(hashtag);
                }
            });
        } else {
            holder.getView("deleteButton").setVisibility(View.GONE);
        }

        holder.getView("hashtagName", TextView.class).setText(hashtag.getHashtag());

    }

    @Override
    public int getItemCount() {
        return hashtagsList.size();
    }

    public interface OnHashtagRemovedListener {
        void onHashTagRemoved(Hashtag hashtag);
    }
}
