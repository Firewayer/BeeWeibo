package com.sina.weibo.sdk.demo.project;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.util.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.util.Constants;
import com.sina.weibo.sdk.demo.util.StatusAdapter;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.FriendshipsAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UserActivity extends Activity {

	private static final int UPDATE_HEAD = 1;

	private TextView text_user_name;
	private TextView text_user_mention;
	private TextView text_user_follower;
	private TextView text_user_status;
	private TextView text_user_description;
	ImageView image_head;
	private Button btnGuanzhu;
	private ListView mListView;
	private String userHeadUri;
	private String statusUserUId;
	String name;
	// 获取ListView微博所需信息
	private ArrayList<Status> mStatusList;

	/** 当前 Token 信息 */
	private Oauth2AccessToken mAccessToken;
	/** 用于获取微博信息流等操作的API */
	private StatusesAPI mStatusesAPI;
	/** 用于显示微博Item */
	// private ListView mListViewStatus ;
	/** 用于存放要显示的Item */
	private StatusList mStatuses;

	StatusAdapter mStatusAdapter;

	public static void actionStart(Context context, String statusUserUID,
			String profile_image_url, String name, String description,
			int followers_count, int friends_count, int statuses_count) {
		Intent intent = new Intent(context, UserActivity.class);
		intent.putExtra("followers_count", followers_count);
		intent.putExtra("friends_count", friends_count);
		intent.putExtra("statuses_count", statuses_count);
		intent.putExtra("name", name);
		intent.putExtra("description", description);
		intent.putExtra("profile_image_url", profile_image_url);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_user);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setBackgroundDrawable(
				this.getBaseContext().getResources()
						.getDrawable(R.drawable.background));
		init();
	}

	private void init() {
		// 初始化数据
		// 获取当前已保存过的 Token
		mAccessToken = AccessTokenKeeper.readAccessToken(this);
		// 对statusAPI实例化
		mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);

		Intent intent = getIntent();
		name = intent.getStringExtra("name");
		String description = intent.getStringExtra("description");
		String userHeadUri = intent.getStringExtra("profile_image_url");
		int followers_count = intent.getIntExtra("followers_count", 0);
		int friends_count = intent.getIntExtra("friends_count", 0);
		int statuses_count = intent.getIntExtra("statuses_count", 0);

		// 初始化控件
		mListView = (ListView) findViewById(R.id.listview_statuses);
		text_user_description = (TextView) findViewById(R.id.txt_perinfo_describtion);
		text_user_name = (TextView) findViewById(R.id.txt_perinfo_uname);
		text_user_mention = (TextView) findViewById(R.id.txt_perinfo_guanzhu);
		text_user_follower = (TextView) findViewById(R.id.txt_perinfo_fens);
		text_user_status = (TextView) findViewById(R.id.id_txt_userstatuses);
		btnGuanzhu = (Button) findViewById(R.id.id_btn_guanzhu);
		/*
		 * btnGuanzhu.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { FriendshipsAPI
		 * mFriendshipsAPI = new FriendshipsAPI(UserActivity.this,
		 * Constants.APP_KEY, mAccessToken); mFriendshipsAPI.create(uid,
		 * screen_name, listener)(name, new RequestListener() {
		 * 
		 * @Override public void onWeiboException(WeiboException arg0) { // TODO
		 * Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void onComplete(String arg0) {
		 * btnGuanzhu.setText("")
		 * 
		 * } }); } });
		 */
		image_head = (ImageView) findViewById(R.id.img_perinfo_head);

		text_user_description.setText(description);
		text_user_name.setText(name);
		text_user_mention.setText("关注 " + friends_count);
		text_user_follower.setText("粉丝 " + followers_count);
		text_user_status.setText("微博 " + statuses_count);

		// 网络操作
		Picasso.with(UserActivity.this).load(userHeadUri).into(image_head);

		// 关注按钮
		btnGuanzhu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(UserActivity.this, "按钮生效", Toast.LENGTH_SHORT)
						.show();
			}
		});

		if (mAccessToken != null && mAccessToken.isSessionValid()) {
			mStatusesAPI.userTimeline(name, 0L, 0L, 15, 1, false, 0, false,
					new RequestListener() {
						@Override
						public void onWeiboException(WeiboException arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onComplete(String response) {
							Toast.makeText(UserActivity.this, "个人信息加载完成",
									Toast.LENGTH_SHORT).show();
							// TODO Auto-generated method stub
							if (!TextUtils.isEmpty(response)) {
								LogUtil.i("TAG", response);
								if (true) {
									// 调用 StatusList#parse 解析字符串成微博列表对象
									StatusList statuses = StatusList
											.parse(response);
									mStatusList = statuses.statusList;
									mStatusAdapter = new StatusAdapter(
											UserActivity.this,
											R.layout.project_weibo_item,
											mStatusList);
									mListView.setAdapter(mStatusAdapter);
									mStatusAdapter.notifyDataSetChanged();

								} else {
									Toast.makeText(UserActivity.this, response,
											Toast.LENGTH_LONG).show();
								}
							}
						}
					});

		}
	}

}
