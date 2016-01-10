package app.ssm.duck.duckapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {
    // Webview 선언
    private WebView mWebView;
    private String memo_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Intent successIntent = getIntent();

        if (successIntent.hasExtra("memo_id")) {
            memo_id = successIntent.getStringExtra("memo_id");
        }

        mWebView = (WebView) findViewById(R.id.WebView);

        // Javascript 사용가능
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.loadUrl("http://www.broduck.com/memo/readPage?id=" + memo_id);
        mWebView.setWebViewClient(new SSMemoWebViewClient());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private class SSMemoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
