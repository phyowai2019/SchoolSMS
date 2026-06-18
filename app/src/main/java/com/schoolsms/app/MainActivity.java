package com.schoolsms.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.content.Context;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout offlineLayout;
    private ValueCallback<Uri[]> filePathCallback;

    private static final String SMS_URL =
        "https://script.google.com/macros/s/AKfycbzfZENGzuFlqfq3_RDpwRESZMpWn_TshFlGu6I3AX6S3CbRlXbjmgqgJsiz5lrOTDxePA/exec";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen — no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Root layout
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#1e3a8a"));

        // WebView
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Progress bar
        progressBar = new ProgressBar(this, null,
            android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressDrawable(
            getResources().getDrawable(android.R.drawable.progress_horizontal));
        FrameLayout.LayoutParams pbParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 8);
        root.addView(progressBar, pbParams);

        // Offline layout
        offlineLayout = buildOfflineLayout();
        offlineLayout.setVisibility(View.GONE);
        root.addView(offlineLayout, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        setContentView(root);

        setupWebView();

        if (isOnline()) {
            webView.loadUrl(SMS_URL);
        } else {
            showOffline();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString()
            + " SchoolSMSApp/1.0");

        // Enable cookies
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.addJavascriptInterface(new JSBridge(), "AndroidApp");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                                                    WebResourceRequest req) {
                String url = req.getUrl().toString();
                // Keep Google/Script URLs inside app
                if (url.contains("google.com") ||
                    url.contains("script.google.com") ||
                    url.contains("accounts.google.com")) {
                    view.loadUrl(url);
                    return true;
                }
                // Open external links in browser
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url)));
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                offlineLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                // Inject mobile CSS
                injectMobileStyles();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                if (!isOnline()) showOffline();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            // File chooser for photo upload
            @Override
            public boolean onShowFileChooser(WebView webView,
                ValueCallback<Uri[]> callback,
                FileChooserParams params) {
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,
                    "ဓာတ်ပုံရွေးချယ်ပါ"), 100);
                return true;
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition,
                                     mimeType, contentLength) -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
    }

    private void injectMobileStyles() {
        String css = "javascript:(function(){" +
            "var s=document.createElement('style');" +
            "s.innerHTML='" +
            "#sb{display:none!important;}"+
            "#mn{margin-left:0!important;}"+
            "#tb{padding:0 10px!important;height:44px!important;}"+
            "#tb h1{font-size:13px!important;}"+
            ".btn{padding:5px 9px!important;font-size:11px!important;}"+
            ".fi{font-size:14px!important;}"+  // prevent iOS zoom on input
            "table{font-size:11px!important;}"+
            ".cd{padding:12px!important;}"+
            ".sgrid{grid-template-columns:repeat(2,1fr)!important;}"+
            // Bottom nav for mobile
            "#mobile-nav{display:flex!important;}"+
            "';" +
            "document.head.appendChild(s);" +
            // Add hamburger menu button
            "if(!document.getElementById('ham-btn')){" +
            "var btn=document.createElement('button');" +
            "btn.id='ham-btn';" +
            "btn.innerHTML='☰';" +
            "btn.style.cssText='position:fixed;top:8px;left:8px;z-index:999;" +
            "background:#1e40af;color:#fff;border:none;border-radius:6px;" +
            "padding:6px 10px;font-size:18px;cursor:pointer;';" +
            "btn.onclick=function(){" +
            "var sb=document.getElementById(\\'sb\\');" +
            "if(sb){sb.style.display=sb.style.display===\\'block\\'?\\'none\\':\\'block\\';" +
            "sb.style.position=\\'fixed\\';sb.style.top=\\'0\\';sb.style.left=\\'0\\';" +
            "sb.style.height=\\'100vh\\';sb.style.zIndex=\\'500\\';sb.style.width=\\'240px\\';}};" +
            "document.body.appendChild(btn);" +
            "}" +
            "})();";
        webView.loadUrl(css);
    }

    private LinearLayout buildOfflineLayout() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(Color.parseColor("#1e3a8a"));
        ll.setGravity(android.view.Gravity.CENTER);

        TextView icon = new TextView(this);
        icon.setText("📡");
        icon.setTextSize(60);
        icon.setGravity(android.view.Gravity.CENTER);

        TextView msg = new TextView(this);
        msg.setText("အင်တာနက် ချိတ်ဆက်မှု မရှိပါ\nInternet connection required");
        msg.setTextColor(Color.WHITE);
        msg.setTextSize(16);
        msg.setGravity(android.view.Gravity.CENTER);
        msg.setPadding(0, 20, 0, 30);

        android.widget.Button retryBtn = new android.widget.Button(this);
        retryBtn.setText("🔄 ပြန်ကြိုးစားမည်");
        retryBtn.setBackgroundColor(Color.parseColor("#f59e0b"));
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setPadding(40, 16, 40, 16);
        retryBtn.setOnClickListener(v -> {
            if (isOnline()) {
                offlineLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(SMS_URL);
            } else {
                Toast.makeText(this, "အင်တာနက် မရှိသေးပါ", Toast.LENGTH_SHORT).show();
            }
        });

        ll.addView(icon);
        ll.addView(msg);
        ll.addView(retryBtn);
        return ll;
    }

    private void showOffline() {
        webView.setVisibility(View.GONE);
        offlineLayout.setVisibility(View.VISIBLE);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)
            getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    // JS Bridge - callable from web page JS
    public class JSBridge {
        @JavascriptInterface
        public String getDeviceInfo() {
            return android.os.Build.MODEL + " | Android " + android.os.Build.VERSION.RELEASE;
        }
        @JavascriptInterface
        public void showToast(String msg) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == 100) {
            if (filePathCallback != null) {
                Uri[] results = null;
                if (resultCode == Activity.RESULT_OK && data != null) {
                    results = new Uri[]{data.getData()};
                }
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
