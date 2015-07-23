package com.sina.weibo.sdk.demo.project;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.util.AccessTokenKeeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class LoginActivity extends Activity {

	private WebView webView;

	private Button offBtn;
	String url = "http://m.weibo.cn/reg/index?vt=4&res=wel&wm=3349&backURL=http%3A%2F%2Fm.weibo.cn%2Findex%2Frouter%3Fcookie%3D2%26jumpfrom%3Dwapv4%26tip%3D1";
	String name = "注册";
	private Oauth2AccessToken mAccessToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_login);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setBackgroundDrawable(
				this.getBaseContext().getResources()
						.getDrawable(R.drawable.background));

		webView = (WebView) findViewById(R.id.web_view);
		webView.getSettings().setJavaScriptEnabled(true);
		offBtn = (Button) findViewById(R.id.id_off);
		Intent intent = getIntent();
		String initUrl = url;
		String offText = "关闭" + name + "  直接登录";
		offBtn.setText(offText);
		Log.d("WebActivity", "onCreate");

		offBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				webView.reload();
				Intent intent = new Intent(LoginActivity.this,
						WelcomeActivity.class);
				startActivity(intent);
				finish();
			}
		});

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

		});
		webView.loadUrl(initUrl);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d("WebActivity", "onRestart");
	}

	@Override
	protected void onDestroy() {
		webView.reload();
		super.onDestroy();

		Log.d("WebActivity", "onDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
