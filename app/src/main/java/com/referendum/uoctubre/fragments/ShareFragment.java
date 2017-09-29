package com.referendum.uoctubre.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.adapters.ImageAdapter;
import com.referendum.uoctubre.main.Constants;
import com.referendum.uoctubre.main.ImageActivity;
import com.referendum.uoctubre.model.Image;
import com.referendum.uoctubre.utils.GridDividerItemDecoration;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShareFragment extends BaseFragment {

    public static final String TAG = "share_fragment";

    public static ShareFragment newInstance() {

        Bundle args = new Bundle();

        ShareFragment fragment = new ShareFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private View loadingLayout;
    private View errorLayout;
    private RecyclerView imageRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_share, container, false);
        loadingLayout = view.findViewById(R.id.loading_layout);
        errorLayout = view.findViewById(R.id.error_layout);
        imageRecyclerView = view.findViewById(R.id.images_recyclerview);

        errorLayout.findViewById(R.id.error_retry_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImages();
            }
        });

        loadImages();

        return view;
    }

    private void loadImages() {
        loadingLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        imageRecyclerView.setVisibility(View.GONE);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imatges.json");
        storageRef.getBytes(Constants.MAX_FIREBASE_DOWNLOAD_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (isFragmentSafe()) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Image>>() {
                    }.getType();
                    List<Image> images = gson.fromJson(new String(bytes), listType);

                    Collections.sort(images, new Comparator<Image>() {
                        @Override
                        public int compare(Image o1, Image o2) {
                            return o1.getOrder() - o2.getOrder();
                        }
                    });

                    loadingLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);
                    imageRecyclerView.setVisibility(View.VISIBLE);

                    imageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    imageRecyclerView.addItemDecoration(new GridDividerItemDecoration(
                            getResources().getDimensionPixelSize(R.dimen.grid_divider_width), 2));
                    imageRecyclerView.setAdapter(new ImageAdapter(images, new ImageAdapter.OnImageClickedListener() {
                        @Override
                        public void onImageClicked(Image image, View view) {
                            Intent intent = new Intent(getContext(), ImageActivity.class);
                            intent.putExtra(ImageActivity.EXTRA_IMAGE, image);
                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(getActivity(), view, "image");
                            startActivity(intent, options.toBundle());
                        }
                    }));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (isFragmentSafe()) {
                    loadingLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                    imageRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }
}
