package com.sina.weibo.sdk.demo.project;

import java.util.ArrayList;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.util.AccessTokenKeeper;
import com.sina.weibo.sdk.demo.util.Constants;
import com.sina.weibo.sdk.demo.util.ReFlashListView;
import com.sina.weibo.sdk.demo.util.SlidingMenuAdapter;
import com.sina.weibo.sdk.demo.util.SlidingMenuItem;
import com.sina.weibo.sdk.demo.util.StatusAdapter;
import com.sina.weibo.sdk.demo.util.ReFlashListView.ILoadMoreListener;
import com.sina.weibo.sdk.demo.util.ReFlashListView.IReflashListener;
import com.sina.weibo.sdk.exception.WeiboException;

import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.sina.weibo.sdk.openapi.UsersAPI;

import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener,
		IReflashListener, ILoadMoreListener, OnLongClickListener {

	// SlidingMenu
	private SlidingMenu mSlidingMenu;
	private boolean flag = true;
	private List<SlidingMenuItem> menuList = new ArrayList<SlidingMenuItem>();
	private ListView mMenuListView;

	private ArrayList<Status> mStatusList;
	private long loadMoreId;
	/** 当前 Token 信息 */
	private Oauth2AccessToken mAccessToken;
	/** 用于获取微博信息流等操作的API */
	private StatusesAPI mStatusesAPI;
	/** 用于显示微博Item */
	// private ListView mListViewStatus ;
	/** 用于存放要显示的Item */
	private StatusList mStatuses;

	StatusAdapter mStatusAdapter;

	// ReFlashListView
	private ReFlashListView mListViewStatus;
	/** 用于存放要显示的Item */

	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_main);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setBackgroundDrawable(
				this.getBaseContext().getResources()
						.getDrawable(R.drawable.background));
		init();
	}

	private void init() {

		// 获取当前已保存过的 Token
		mAccessToken = AccessTokenKeeper.readAccessToken(this);
		// 对statusAPI实例化
		mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);

		// 微博ReFlashListView部分
		mListViewStatus = (ReFlashListView) findViewById(R.id.id_listview_status);
		mListViewStatus.setRefreshInterface(this);
		mListViewStatus.setLoadMoreInterface(this);
		mListViewStatus.setOnItemClickListener(this);
		mListViewStatus.setOnLongClickListener(this);
		if (mAccessToken != null && mAccessToken.isSessionValid()) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在获取用户信息，请稍候...");
			progressDialog.show();
			mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false,
					mListener);
		}

		// SlidingMenu部分
		// SlidingMenu
		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setMode(SlidingMenu.LEFT);
		mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_offset);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mSlidingMenu.setFadeEnabled(true);
		mSlidingMenu.setFadeDegree(0.60f);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mSlidingMenu.setMenu(R.layout.slidingmenu);
		ImageButton btnMyInfo = (ImageButton) findViewById(R.id.id_btn_myinfo);
		btnMyInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UsersAPI mUsersAPI = new UsersAPI(MainActivity.this,
						Constants.APP_KEY, mAccessToken);
				mUsersAPI.show(Long.parseLong(mAccessToken.getUid()),
						new RequestListener() {

							@Override
							public void onWeiboException(WeiboException arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onComplete(String reponse) {
								// TODO Auto-generated method stub
								if (!TextUtils.isEmpty(reponse)) {
									User mUser = User.parse(reponse);
									UserActivity.actionStart(MainActivity.this,
											mUser.idstr,
											mUser.profile_image_url,
											mUser.name, mUser.description,
											mUser.followers_count,
											mUser.friends_count,
											mUser.statuses_count);
								}
							}
						});
			}
		});
		mMenuListView = (ListView) findViewById(R.id.menulistview);
		mMenuListView.setOnItemClickListener(this);
		menuList = new ArrayList<SlidingMenuItem>();
		initMenuList();
		SlidingMenuAdapter menuAdapter = new SlidingMenuAdapter(
				MainActivity.this, R.layout.menuitem, menuList);
		mMenuListView.setAdapter(menuAdapter);
		mSlidingMenu.setOnOpenedListener(new OnOpenedListener() {
			@Override
			public void onOpened() {
				flag = false;
			}
		});
		mSlidingMenu.setOnCloseListener(new OnCloseListener() {
			@Override
			public void onClose() {
				// TODO Auto-generated method stub
				flag = true;
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (flag) {
			Status status = mStatusList.get(position - 1);
			if (status.pic_urls != null) {
				CommentsActivity.actionStart(MainActivity.this, status.idstr,
						status.user.name, status.created_at, status.text,
						status.user.profile_image_url, status.bmiddle_pic,
						String.valueOf(status.attitudes_count),
						String.valueOf(status.comments_count),
						String.valueOf(status.reposts_count));
			} else {
				CommentsActivity.actionStart(MainActivity.this, status.idstr,
						status.user.name, status.created_at, status.text,
						status.user.profile_image_url, null,
						String.valueOf(status.attitudes_count),
						String.valueOf(status.comments_count),
						String.valueOf(status.reposts_count));
			}

		} else {
			switch (position) {
			case 0: // 首页
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("正在加载首页，请稍候...");
				progressDialog.show();
				mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false,
						mListener);
				mSlidingMenu.toggle();
				break;
			case 1:// 热门微博
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("正在获取热门微博，请稍候...");
				progressDialog.show();
				mStatusesAPI.publicTimeline(30, 1, false,
						new RequestListener() {

							@Override
							public void onWeiboException(WeiboException arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onComplete(String response) {
								if (!TextUtils.isEmpty(response)) {
									LogUtil.i("TAG", response);
									if (response.startsWith("{\"statuses\"")) {
										// 调用 StatusList#parse 解析字符串成微博列表对象
										StatusList statuses = StatusList
												.parse(response);
										mStatusList = statuses.statusList;
										mStatusAdapter = new StatusAdapter(
												MainActivity.this,
												R.layout.project_weibo_item,
												mStatusList);
										mListViewStatus
												.setAdapter(mStatusAdapter);
										mStatusAdapter.notifyDataSetChanged();
										progressDialog.dismiss();

									} else {
										Toast.makeText(MainActivity.this,
												response, Toast.LENGTH_LONG)
												.show();
									}
								}
							}
						});
				mSlidingMenu.toggle();
				break;
			case 2:// 提及
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("正在加载@，请稍候...");
				progressDialog.show();
				mStatusesAPI.mentions(0L, 0L, 10, 1,
						StatusesAPI.AUTHOR_FILTER_ALL,
						StatusesAPI.SRC_FILTER_ALL, 0, false,
						new RequestListener() {

							@Override
							public void onWeiboException(WeiboException arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onComplete(String response) {
								if (!TextUtils.isEmpty(response)) {
									LogUtil.i("TAG", response);
									if (response.startsWith("{\"statuses\"")) {
										// 调用 StatusList#parse 解析字符串成微博列表对象
										StatusList statuses = StatusList
												.parse(response);
										mStatusList = statuses.statusList;
										mStatusAdapter = new StatusAdapter(
												MainActivity.this,
												R.layout.project_weibo_item,
												mStatusList);
										mListViewStatus
												.setAdapter(mStatusAdapter);
										mStatusAdapter.notifyDataSetChanged();
										progressDialog.dismiss();

									} else {
										Toast.makeText(MainActivity.this,
												response, Toast.LENGTH_LONG)
												.show();
									}
								}
							}
						});
				mSlidingMenu.toggle();
				break;
			case 3:// 注销
				AccessTokenKeeper.clear(MainActivity.this);
				Intent intent = new Intent(MainActivity.this,
						WelcomeActivity.class);
				startActivity(intent);
				finish();
				mSlidingMenu.toggle();
				break;
			case 4: // 退出
				finish();
			default:
				break;
			}
		}
	}

	private RequestListener mListener = new RequestListener() {
		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				LogUtil.i("TAG", response);
				if (response.startsWith("{\"statuses\"")) {
					// 调用 StatusList#parse 解析字符串成微博列表对象
					StatusList statuses = StatusList.parse(response);
					mStatusList = statuses.statusList;
					mStatusAdapter = new StatusAdapter(MainActivity.this,
							R.layout.project_weibo_item, mStatusList);
					mListViewStatus.setAdapter(mStatusAdapter);
					mStatusAdapter.notifyDataSetChanged();
					progressDialog.dismiss();
				} else {
					Toast.makeText(MainActivity.this, response,
							Toast.LENGTH_LONG).show();
				}
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub

		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.id_menu_logout:
			// 获取当前已保存过的 Token
			AccessTokenKeeper.clear(MainActivity.this);

			// new LogoutAPI(MainActivity.this, Constants.APP_KEY,
			// mAccessToken).logout(new LogOutRequestListener());
			Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.id_menu_send:
			Intent sendStatusIntent = new Intent(MainActivity.this,
					SendStatusActivity.class);
			startActivity(sendStatusIntent);
			break;
		case R.id.id_menu_exit:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private RequestListener mRequestListener = new RequestListener() {

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onComplete(String arg0) {
			Toast.makeText(MainActivity.this, "微博已发出", Toast.LENGTH_SHORT)
					.show();

		}
	};

	public void initMenuList() {

		SlidingMenuItem oneMenu = new SlidingMenuItem("首页",
				R.drawable.ic_menu_shouye);
		menuList.add(oneMenu);
		SlidingMenuItem twoMenu = new SlidingMenuItem("热门",
				R.drawable.ic_menu_remen);
		menuList.add(twoMenu);
		SlidingMenuItem threeMenu = new SlidingMenuItem("提及",
				R.drawable.ic_menu_sixin);
		menuList.add(threeMenu);
		SlidingMenuItem fourMenu = new SlidingMenuItem("注销",
				R.drawable.ic_menu_pinglun);
		menuList.add(fourMenu);
		SlidingMenuItem fiveMenu = new SlidingMenuItem("退出",
				R.drawable.ic_menu_exit);
		menuList.add(fiveMenu);
	}
	
	@Override
	public boolean onLongClick(View v) {
		Toast.makeText(MainActivity.this, "长按事件触发", Toast.LENGTH_SHORT).show();
		return true;
	}

	private void setReflashData() {
		if (mAccessToken != null && mAccessToken.isSessionValid()) {
			mStatusesAPI.friendsTimeline(0L, 0L, 30, 1, false, 0, false,
					mListener);
		}
	}

	@Override
	public void onReflash() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				setReflashData();
				mListViewStatus.reflashComplete();
			}
		}, 1000);

	}



	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		// 加载更多数据
		Long maxId = Long
				.parseLong(mStatusList.get(mStatusList.size() - 1).idstr);

		mStatusesAPI.friendsTimeline(0L, maxId - 1L, 20, 1, false, 0, false,
				new RequestListener() {

					@Override
					public void onWeiboException(WeiboException arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onComplete(String response) {
						if (!TextUtils.isEmpty(response)) {
							LogUtil.i("TAG", response);
							if (response.startsWith("{\"statuses\"")) {
								// 调用 StatusList#parse 解析字符串成微博列表对象
								StatusList statuses = StatusList
										.parse(response);
								mStatusList.addAll(statuses.statusList);
								mStatusAdapter.notifyDataSetChanged();
								mListViewStatus.loadMoreComplete();
							}
						}
					}
				});

	}

}
