package com.surcumference.fingerprint.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.surcumference.fingerprint.Constant;
import com.surcumference.fingerprint.Lang;
import com.surcumference.fingerprint.R;
import com.surcumference.fingerprint.util.DpUtils;
import com.surcumference.fingerprint.util.Task;
import com.surcumference.fingerprint.util.UrlUtils;
import com.surcumference.fingerprint.util.log.L;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jason on 2017/11/18.
 */

public class LicenseView extends DialogFrameLayout {

    private ProgressBar mProgressBar;
    private boolean mLoadSuccess = false;

    public LicenseView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public LicenseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LicenseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        try {
            mProgressBar = initProgressBar(new ContextThemeWrapper(context, android.R.style.Theme_Material_NoActionBar_Fullscreen));
            WebView webView = initWebView(context);
            webView.loadUrl(Constant.HELP_URL_LICENSE);
            this.setMinimumHeight(DpUtils.dip2px(context, 200));
            this.addView(webView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            this.addView(mProgressBar, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DpUtils.dip2px(context, 4)));
        } catch (Exception e) {
            L.e(e);
        }
        withNegativeButtonText(Lang.getString(R.id.disagree));
        withPositiveButtonText(Lang.getString(R.id.agree));
    }

    private void loadFallbackLicense(WebView webView) {
        Task.onBackground(() -> {
            for (String fallbackUrl : Constant.HELP_URL_LICENSE_FALLBACKS) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(fallbackUrl).openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    if (conn.getResponseCode() != 200) {
                        conn.disconnect();
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    conn.disconnect();
                    String md = sb.toString();
                    if (md.length() > 0) {
                        String html = markdownToSimpleHtml(md);
                        Task.onMain(() -> webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null));
                        return;
                    }
                } catch (Exception e) {
                    L.d("Fallback failed: " + fallbackUrl, e);
                }
            }
        });
    }

    private static String markdownToSimpleHtml(String md) {
        String escaped = md.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String[] lines = escaped.split("\n");
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>");
        html.append("<style>body{font-family:sans-serif;padding:8px;line-height:1.6;color:#333}h3,h4,h5{margin:12px 0 6px}</style>");
        html.append("</head><body>");
        for (String line : lines) {
            if (line.startsWith("##### ")) {
                html.append("<h5>").append(line.substring(6)).append("</h5>");
            } else if (line.startsWith("#### ")) {
                html.append("<h4>").append(line.substring(5)).append("</h4>");
            } else if (line.startsWith("### ")) {
                html.append("<h3>").append(line.substring(4)).append("</h3>");
            } else if (line.startsWith("- ")) {
                String content = line.substring(2).replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
                html.append("<p style='margin:2px 0 2px 16px'>• ").append(content).append("</p>");
            } else if (line.trim().isEmpty()) {
                html.append("<br>");
            } else {
                String content = line.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
                html.append("<p>").append(content).append("</p>");
            }
        }
        html.append("</body></html>");
        return html.toString();
    }

    private ProgressBar initProgressBar(Context context) {
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.MULTIPLY);
        progressBar.setBackgroundColor(0x20009688);
        return progressBar;
    }

    private WebView initWebView(Context context) throws Exception {
        WebView webView = new WebView(context);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (TextUtils.isEmpty(url)) {
                    return super.shouldOverrideUrlLoading(view, url);
                }
                String lurl = url.toLowerCase();
                if (lurl.startsWith("http://") || lurl.startsWith("https://")) {
                    if (lurl.endsWith(".apk") || lurl.endsWith(".zip") || lurl.endsWith(".tar.gz") || lurl.contains("pan.baidu.com/s/")) {
                        UrlUtils.openUrl(context, url);
                        return true;
                    }
                    view.loadUrl(url);
                    return true;
                }
                UrlUtils.openUrl(context, url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadSuccess = true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (!mLoadSuccess && request.isForMainFrame()) {
                    loadFallbackLicense(view);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (!mLoadSuccess) {
                    loadFallbackLicense(view);
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                handleProgressChanged(newProgress);
            }
        });
        return webView;
    }

    private  void handleProgressChanged(int progress) {
        ProgressBar progressBar = mProgressBar;
        if (progress >= 100) {
            Task.onMain(1000, () -> {
                if (progressBar.getVisibility() != View.GONE) {
                    progressBar.setVisibility(View.GONE);
                }
                progressBar.setProgress(0);
            });
        } else {
            if (progressBar.getVisibility() != View.VISIBLE) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // will update the "progress" propriety of seekbar until it reaches progress
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", progress);
            animation.setDuration(600);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        } else {
            progressBar.setProgress(progress);
        }
    }

    @Override
    public String getDialogTitle() {
        return Lang.getString(R.id.settings_title_license);
    }
}
