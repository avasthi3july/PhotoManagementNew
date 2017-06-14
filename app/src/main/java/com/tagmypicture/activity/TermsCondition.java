package com.tagmypicture.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import com.tagmypicture.R;


/**
 * Created by kavasthi on 9/15/2016.
 */

public class TermsCondition extends Activity implements View.OnClickListener {
    private ImageView imageViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms);
        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        WebView webView = (WebView) findViewById(R.id.webView1);
        imageViewBack.setOnClickListener(this);
        webView.getSettings().setJavaScriptEnabled(true);
        // webView.loadUrl("http://synapse.asia/bridge8618/terms/terms_condition");
        webView.loadUrl("http://www.tagmypicture.com/pdf/Terms.pdf");
    }

    @Override
    public void onClick(View v) {
        super.onBackPressed();
        finish();
    }
}

