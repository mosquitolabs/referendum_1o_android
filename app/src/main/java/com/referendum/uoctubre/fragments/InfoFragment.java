package com.referendum.uoctubre.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.adapters.HashtagAdapter;
import com.referendum.uoctubre.main.Constants;
import com.referendum.uoctubre.model.Hashtag;
import com.referendum.uoctubre.utils.SharedPreferencesUtils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class InfoFragment extends BaseFragment {

    public static final String TAG = "info_fragment";

    public static InfoFragment newInstance() {

        Bundle args = new Bundle();

        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    View loginLayout;
    View loadingLayout;
    View errorLayout;
    View normalLayout;
    TwitterLoginButton loginButton;
    private List<String> remoteHashtags;

    final int TWEETS_PER_PAGE = 50;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_info, container, false);

        loginLayout = view.findViewById(R.id.twitter_login_layout);
        loadingLayout = view.findViewById(R.id.twitter_loading_layout);
        errorLayout = view.findViewById(R.id.twitter_error_layout);
        normalLayout = view.findViewById(R.id.twitter_normal_layout);

        errorLayout.findViewById(R.id.error_retry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadHashtags(view);
            }
        });

        loginButton = view.findViewById(R.id.twitter_login_button);
        loginButton.setText(R.string.twitter_login_button);

        boolean hasTwitterApp = true;
        try {
            getContext().getPackageManager().getApplicationInfo("com.twitter.android", 0);
        } catch (PackageManager.NameNotFoundException e) {
            hasTwitterApp = false;
        }

        //Allow to enable user login via Firebase config, but first make all users be guests until we reach certain amount of users
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        boolean isTwitterLoginRequired = firebaseRemoteConfig.getBoolean(Constants.FIREBASE_CONFIG_TWITTER_LOGIN_REQUIRED);

        if (isTwitterLoginRequired && hasTwitterApp && TwitterCore.getInstance().getSessionManager().getActiveSession() == null) {
            //Not logged in and appears to have Twitter: ask for login
            loginLayout.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);
            normalLayout.setVisibility(View.GONE);
            loginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    loadHashtags(view);
                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(getContext(), R.string.twitter_login_error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadHashtags(view);
        }

        view.findViewById(R.id.twitter_add_hashtag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_hashtag, null);
                final EditText editText = dialogView.findViewById(R.id.hashtag_name);

                new AlertDialog.Builder(getContext()).setTitle(R.string.twitter_add_hashtag)
                        .setView(dialogView)
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                Set<String> hashtags = SharedPreferencesUtils.getStringSet(SharedPreferencesUtils.USER_HASHTAGS);
                                hashtags.add(editText.getText().toString().replaceAll("\"",""));
                                SharedPreferencesUtils.setStringSet(SharedPreferencesUtils.USER_HASHTAGS, hashtags);
                                prepareTweetsLayout(getView());
                            }
                        })
                        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        return view;
    }

    private void loadHashtags(final View view) {
        loginLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        normalLayout.setVisibility(View.GONE);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("hashtags.json");
        storageRef.getBytes(Constants.MAX_FIREBASE_DOWNLOAD_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>() {
                }.getType();
                List<String> hashtags = gson.fromJson(new String(bytes), listType);
                Collections.sort(hashtags);

                remoteHashtags = hashtags;
                prepareTweetsLayout(view);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                loginLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
                normalLayout.setVisibility(View.GONE);
            }
        });
    }

    private void prepareTweetsLayout(View view) {
        loginLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        normalLayout.setVisibility(View.VISIBLE);

        List<Hashtag> hashtagsList = new ArrayList<>();
        for (String hashtag : remoteHashtags) {
            hashtagsList.add(new Hashtag(hashtag, false));
        }

        Set<String> userHashtags = SharedPreferencesUtils.getStringSet(SharedPreferencesUtils.USER_HASHTAGS);

        for (String hashtag : userHashtags) {
            hashtagsList.add(new Hashtag(hashtag, true));
        }

        final RecyclerView hashtagsRecyclerView = view.findViewById(R.id.twitter_hashtags_recyclerview);
        hashtagsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        hashtagsRecyclerView.setAdapter(new HashtagAdapter(hashtagsList, new HashtagAdapter.OnHashtagRemovedListener() {
            @Override
            public void onHashTagRemoved(Hashtag hashtag) {
                Set<String> userHashtags = SharedPreferencesUtils.getStringSet(SharedPreferencesUtils.USER_HASHTAGS);
                userHashtags.remove(hashtag.getHashtag());
                prepareTweetsLayout(getView());
            }
        }));

        StringBuilder hashtagsStringBuilder = new StringBuilder();
        boolean first = true;
        for (Hashtag hashtag : hashtagsList) {
            if (!first) {
                hashtagsStringBuilder.append(" OR ");
            } else {
                first = false;
            }
            hashtagsStringBuilder.append("\"");
            hashtagsStringBuilder.append(hashtag.getHashtag());
            hashtagsStringBuilder.append("\"");
        }

        final SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query(hashtagsStringBuilder.toString())
                .resultType(SearchTimeline.ResultType.RECENT)
                .maxItemsPerRequest(TWEETS_PER_PAGE)
                .build();

        final TweetTimelineRecyclerViewAdapter adapter =
                new TweetTimelineRecyclerViewAdapter.Builder(getContext())
                        .setTimeline(searchTimeline)
                        .setViewStyle(R.style.tw__TweetLightStyle)
                        .build();

        final RecyclerView tweetsRecyclerView = view.findViewById(R.id.twitter_tweets_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        tweetsRecyclerView.setLayoutManager(layoutManager);
        tweetsRecyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(tweetsRecyclerView.getContext(),
                layoutManager.getOrientation());
        tweetsRecyclerView.addItemDecoration(dividerItemDecoration);

        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.twitter_swiperefresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.refresh(new Callback<TimelineResult<Tweet>>() {
                    @Override
                    public void success(Result<TimelineResult<Tweet>> result) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), R.string.load_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
