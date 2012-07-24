package com.lvls.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class HelloWebViewActivity extends Activity {

    WebView mWebView;
    final Activity activity = this;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final String url = "http://www.google.com";
//        final String url = "http://www.jenandjane.com";
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new HelloWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.loadUrl(url);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    // --------------------------- Embedded classes ------------------------------------

    private class HelloWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            Toast.makeText(activity, "Oh, no! " + description, Toast.LENGTH_SHORT);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}