package com.sina.weibo.sdk.demo.project;

import java.util.ArrayList;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.util.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.util.CommentsAdapter;
import com.sina.weibo.sdk.demo.util.Constants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.utils.LogUtil;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CommentsActivity extends Activity {
	private ArrayList<Comment> mComments;
	private CommentsAdapter mCommentsAdapter;
	private ListView mListView;
	private TextView mTextView;
	/** 当前 Token 信息 */
	private Oauth2AccessToken mAccessToken;
	/** 微博评论接口 */
	private CommentsAPI mCommentsAPI;

	private String statusId;
	String numberOfpinglun;
	int intNumberOfpinglun;

	private EditText writeComment;
	private TextView txt_zhuanfa;
	private TextView txt_pinglun;

	public static void actionStart(Context context, String statusId,
			String name, String time, String content, String uriHead,
			String uriContent, String numberOfdianzan, String numberOfpinglun,
			String numberOfzhuanfa) {
		Intent intent = new Intent(context, CommentsActivity.class);
		intent.putExtra("statusId", statusId);
		intent.putExtra("statusName", name);
		intent.putExtra("statusTime", time);
		intent.putExtra("statusContent", content);
		intent.putExtra("statusUriHead", uriHead);
		intent.putExtra("uriContent", uriContent);
		intent.putExtra("numberOfdianzan", numberOfdianzan);
		intent.putExtra("numberOfpinglun", numberOfpinglun);
		intent.putExtra("numberOfzhuanfa", numberOfzhuanfa);

		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_comments);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setBackgroundDrawable(
				this.getBaseContext().getResources()
						.getDrawable(R.drawable.background));
		init();
	}

	private void init() {
		mAccessToken = AccessTokenKeeper.readAccessToken(this);
		mCommentsAPI = new CommentsAPI(this, Constants.APP_KEY, mAccessToken);
		Intent intent = getIntent();
		final String statusId = intent.getStringExtra("statusId");
		String statusUriHead = intent.getStringExtra("statusUriHead");
		String statusUriContent = intent.getStringExtra("uriContent");
		String statusName = intent.getStringExtra("statusName");
		String statusTime = intent.getStringExtra("statusTime");
		String statusContent = intent.getStringExtra("statusContent");
		String numberOfdianzan = intent.getStringExtra("numberOfdianzan");
		numberOfpinglun = intent.getStringExtra("numberOfpinglun");
		String numberOfzhuanfa = intent.getStringExtra("numberOfzhuanfa");

		Log.d("TAG", statusId);
		// 原微博部分
		TextView name = (TextView) findViewById(R.id.txt_wb_item_uname);
		TextView time = (TextView) findViewById(R.id.txt_wb_item_time);
		TextView content = (TextView) findViewById(R.id.txt_wb_item_content);
		mListView = (ListView) findViewById(R.id.id_list_comments);
		name.setText(statusName);
		time.setText(statusTime);
		content.setText(statusContent);
		LinearLayout subStatusLayout = (LinearLayout) findViewById(R.id.lyt_wb_item_sublayout);
		subStatusLayout.setVisibility(View.GONE);

		// number
		TextView txt_dianzan = (TextView) findViewById(R.id.txt_wb_item_unlike);
		txt_pinglun = (TextView) findViewById(R.id.txt_wb_item_comment);
		txt_zhuanfa = (TextView) findViewById(R.id.txt_wb_item_redirect);
		txt_dianzan.setText(numberOfdianzan);
		txt_pinglun.setText(numberOfpinglun);
		txt_zhuanfa.setText(numberOfzhuanfa);

		ImageView imageHead = (ImageView) findViewById(R.id.img_wb_item_head);
		Picasso.with(CommentsActivity.this).load(statusUriHead).into(imageHead);

		// 评论列表
		if (Integer.valueOf(numberOfpinglun).intValue() != 0) {
			// 获取微博评论信息接口

			if (mAccessToken != null && mAccessToken.isSessionValid()) {
				mCommentsAPI.show(Long.parseLong(statusId), 0L, 0L, 15, 1,
						CommentsAPI.AUTHOR_FILTER_ALL, mListener);
			}
		}
		if (statusUriContent != null) {
			ImageView imageContent = (ImageView) findViewById(R.id.img_wb_item_content);
			imageContent.setVisibility(View.VISIBLE);
			Picasso.with(CommentsActivity.this).load(statusUriContent)
					.resize(300, 300).centerInside().into(imageContent);
		}

		// 写评论部分
		writeComment = (EditText) findViewById(R.id.id_edit_comment);
		Button sendComment = (Button) findViewById(R.id.id_btn_comment_send);
		sendComment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mCommentsAPI.create(writeComment.getText().toString(),
						Long.parseLong(statusId), false, new RequestListener() {

							@Override
							public void onWeiboException(WeiboException arg0) {
							}

							@Override
							public void onComplete(String arg0) {
								String statusId = getIntent().getStringExtra(
										"statusId");
								if (Integer.valueOf(numberOfpinglun).intValue() != 0) {
									intNumberOfpinglun = Integer
											.parseInt(getIntent()
													.getStringExtra(
															"numberOfpinglun")) + 1;

								} else {
									intNumberOfpinglun = 1;
								}
								writeComment.setText("");
								txt_pinglun.setText(String
										.valueOf(intNumberOfpinglun));
								mCommentsAPI.show(Long.parseLong(statusId), 0L,
										0L, 20, 1,
										CommentsAPI.AUTHOR_FILTER_ALL,
										mListener);
							}

						});
			}
		});

	}

	private RequestListener mListener = new RequestListener() {
		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				CommentList comments = CommentList.parse(response);
				mComments = comments.commentList;

				mCommentsAdapter = new CommentsAdapter(CommentsActivity.this,
						R.layout.project_comments_item, mComments);
				mListView.setAdapter(mCommentsAdapter);
				mCommentsAdapter.notifyDataSetChanged();

			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.comment, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.id_menu_zhuanfa:
			String statusId = getIntent().getStringExtra("statusId");
			final int numberOfzhuanfa = Integer.parseInt(getIntent()
					.getStringExtra("numberOfzhuanfa")) + 1;
			StatusesAPI mStatusesAPI = new StatusesAPI(CommentsActivity.this,
					Constants.APP_KEY, mAccessToken);
			mStatusesAPI.repost(Long.parseLong(statusId), null, 0,
					new RequestListener() {

						@Override
						public void onWeiboException(WeiboException arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onComplete(String arg0) {
							txt_zhuanfa.setText(String.valueOf(numberOfzhuanfa));
							Toast.makeText(CommentsActivity.this, "微博已转发",
									Toast.LENGTH_SHORT).show();
						}
					});
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
