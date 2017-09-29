package com.referendum.uoctubre.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.main.Constants;
import com.referendum.uoctubre.model.Results;
import com.referendum.uoctubre.utils.StringsManager;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ResultsFragment extends BaseFragment {

    public static final String TAG = "results_fragment";

    public static ResultsFragment newInstance() {

        Bundle args = new Bundle();

        ResultsFragment fragment = new ResultsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private View loadingLayout;
    private View errorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PieChart pieChart;
    private TextView emptyChart;
    private TextView participation;
    private TextView counted;
    private ProgressBar participationBar;
    private ProgressBar countedBar;
    private TextView message;

    private Results results;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_results, container, false);

        loadingLayout = view.findViewById(R.id.loading_layout);
        errorLayout = view.findViewById(R.id.error_layout);
        pieChart = view.findViewById(R.id.chart);
        emptyChart = view.findViewById(R.id.chart_empty);
        participation = view.findViewById(R.id.participation);
        counted = view.findViewById(R.id.counted);
        participationBar = view.findViewById(R.id.participation_bar);
        countedBar = view.findViewById(R.id.counted_bar);
        message = view.findViewById(R.id.message);

        errorLayout.findViewById(R.id.error_retry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadResults(true);
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swiperefresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadResults(false);
            }
        });

        loadResults(true);

        return view;
    }

    public void loadResults(boolean showLoading) {
        if (showLoading) {
            loadingLayout.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("results.json");
        storageRef.getBytes(Constants.MAX_FIREBASE_DOWNLOAD_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (isFragmentSafe()) {
                    results = new Gson().fromJson(new String(bytes), Results.class);

                    prepareGraphLayout();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (isFragmentSafe()) {
                    loadingLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void prepareGraphLayout() {
        loadingLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setRefreshing(false);

        if (results.getCounted() > 0f) {
            pieChart.setVisibility(View.VISIBLE);
            emptyChart.setVisibility(View.GONE);

            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(results.getInvalid(), StringsManager.getString("graph_invalid")));
            entries.add(new PieEntry(results.getBlank(), StringsManager.getString("graph_blank")));
            entries.add(new PieEntry(results.getNo(), StringsManager.getString("graph_no")));
            entries.add(new PieEntry(results.getYes(), StringsManager.getString("graph_yes")));
            PieDataSet set = new PieDataSet(entries, "");
            set.setColors(Color.parseColor("#43474e"), Color.parseColor("#7f7f7f"), Color.parseColor("#fc543d"), Color.parseColor("#65c258"));
//            set.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//            set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//            set.setSliceSpace(2f);
            set.setValueTextSize(16f);
            set.setValueTextColor(Color.WHITE);
            PieData data = new PieData(set);
            data.setValueFormatter(new IValueFormatter() {

                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    // write your logic here
                    return new DecimalFormat("##0.0").format(value) + "%"; // e.g. append a dollar-sign
                }
            });
            pieChart.setData(data);
            pieChart.getLegend().setEnabled(false);
            pieChart.setUsePercentValues(true);
            pieChart.setEntryLabelColor(Color.WHITE);
            pieChart.setEntryLabelTextSize(16f);
            pieChart.setRotationEnabled(false);
            Description description = new Description();
            description.setText("");
            pieChart.setDescription(description);

            pieChart.invalidate();
        } else {
            pieChart.setVisibility(View.GONE);
            emptyChart.setVisibility(View.VISIBLE);
        }

        counted.setText(new DecimalFormat("##0.0").format(results.getCounted()) + "%");
        countedBar.setProgress((int) (results.getCounted() * 10));
        participation.setText(new DecimalFormat("##0.0").format(results.getParticipation()) + "%");
        participationBar.setProgress((int) (results.getParticipation() * 10));

        countedBar.getProgressDrawable().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.grey_dark, getContext().getTheme()), android.graphics.PorterDuff.Mode.SRC_IN);
        participationBar.getProgressDrawable().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.red, getContext().getTheme()), android.graphics.PorterDuff.Mode.SRC_IN);

        if (TextUtils.isEmpty(results.getMessage())) {
            message.setVisibility(View.GONE);
        } else {
            message.setVisibility(View.VISIBLE);
            message.setText(results.getMessage());
        }
    }
}
