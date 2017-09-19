package com.referendum.uoctubre.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.main.Constants;

import im.delight.android.webview.AdvancedWebView;

public class WebFragment extends BaseFragment implements AdvancedWebView.Listener {

    public static final String TAG = "web_fragment";

    public static WebFragment newInstance() {
        Bundle args = new Bundle();
        WebFragment fragment = new WebFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private AdvancedWebView mWebView;
    private View loadingLayout;
    private View errorLayout;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_web, container, false);

        mWebView = view.findViewById(R.id.webview);
        loadingLayout = view.findViewById(R.id.loading_layout);
        errorLayout = view.findViewById(R.id.error_layout);

        errorLayout.findViewById(R.id.error_retry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadUrl("about:blank");

                //Allow to change URL via Firebase
                FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
                String webUrl = firebaseRemoteConfig.getString(Constants.FIREBASE_CONFIG_WEB_URL);

                mWebView.loadUrl(webUrl);
            }
        });

        mWebView.setListener(getActivity(), this);
        mWebView.setGeolocationEnabled(false);
        mWebView.setMixedContentAllowed(true);
        mWebView.setCookiesEnabled(true);
        mWebView.setThirdPartyCookiesEnabled(true);

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (view.getUrl()==null || view.getUrl().startsWith("https://")) {
                    handler.proceed();
                }
                else{
                    handler.cancel();
                }
            }
        });

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //Allow to change URL via Firebase
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        String webUrl = firebaseRemoteConfig.getString(Constants.FIREBASE_CONFIG_WEB_URL);

        mWebView.loadUrl(webUrl);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mWebView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        loadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        loadingLayout.setVisibility(View.GONE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        errorLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public void onExternalPageRequest(String url) {
        //Nothing
    }

    public boolean onBackPressed() {
        return !mWebView.onBackPressed();
    }
}
