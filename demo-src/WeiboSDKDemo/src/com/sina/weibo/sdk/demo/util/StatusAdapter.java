package com.sina.weibo.sdk.demo.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.demo.project.CommentsActivity;
import com.sina.weibo.sdk.demo.project.UserActivity;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.legacy.CommonAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;

import android.sax.StartElementListener;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StatusAdapter extends ArrayAdapter<Status> {

	private int resourceId;

	public StatusAdapter(Context context, int textViewResource,
			List<Status> statusList) {
		super(context, textViewResource, statusList);
		resourceId = textViewResource;
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Status status = getItem(position);
		View view;
		ViewHolder viewHolder;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder = new ViewHolder();

			// 获得控件实例
			viewHolder.subLayout = (LinearLayout) view
					.findViewById(R.id.lyt_wb_item_sublayout);

			viewHolder.textSubContent = (TextView) view
					.findViewById(R.id.txt_wb_item_subcontent);
			viewHolder.iv_subContent = (ImageView) view
					.findViewById(R.id.img_wb_item_content_subpic);

			viewHolder.btnUserHead = (ImageButton) view
					.findViewById(R.id.id_btn_userhead);

			viewHolder.iv_userhead = (ImageView) view
					.findViewById(R.id.img_wb_item_head);

			viewHolder.statusPic = (ImageView) view
					.findViewById(R.id.img_wb_item_content);

			ImageButton btnStatusImage = (ImageButton) view
					.findViewById(R.id.id_btn_statusImage);

			viewHolder.textUserName = (TextView) view
					.findViewById(R.id.txt_wb_item_uname);
			viewHolder.textContent = (TextView) view
					.findViewById(R.id.txt_wb_item_content);
			viewHolder.numberOfDianzan = (TextView) view
					.findViewById(R.id.txt_wb_item_unlike);
			viewHolder.numberOfpinglun = (TextView) view
					.findViewById(R.id.txt_wb_item_comment);
			viewHolder.numberOfzhuanfa = (TextView) view
					.findViewById(R.id.txt_wb_item_redirect);
			viewHolder.textResource = (TextView) view
					.findViewById(R.id.txt_wb_item_from);
			viewHolder.textTime = (TextView) view
					.findViewById(R.id.txt_wb_item_time);
			viewHolder.iv_isv = (ImageView) view
					.findViewById(R.id.img_wb_item_V);

			viewHolder.btnPinglun = (ImageButton) view
					.findViewById(R.id.txt_wb_item_comment_img);

			// TextView numberOfzhuanfa TextView textResource TextView textTime

			view.setTag(viewHolder);
		} else {
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}

		viewHolder.btnUserHead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UserActivity.actionStart(getContext(), status.user.idstr,
						status.user.profile_image_url, status.user.name,
						status.user.description, status.user.followers_count,
						status.user.friends_count, status.user.statuses_count);
			}
		});

		// 主微博
		Transformation userTransformation = new Transformation() {
			@Override
			public Bitmap transform(Bitmap bitmap) {
				Bitmap output = null;
				float roundPx = 15.8f;
				// 自定义变换

				output = Bitmap.createBitmap(bitmap.getWidth(), bitmap

				.getHeight(), Config.ARGB_8888);

				Canvas canvas = new Canvas(output);

				final int color = 0xff424242;

				final Paint paint = new Paint();

				final Rect rect = new Rect(0, 0, bitmap.getWidth(),
						bitmap.getHeight());

				final RectF rectF = new RectF(rect);

				paint.setAntiAlias(true);

				canvas.drawARGB(0, 0, 0, 0);

				paint.setColor(color);

				canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

				paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

				canvas.drawBitmap(bitmap, rect, rect, paint);
				if (bitmap != null && !bitmap.isRecycled()) { // 将旧图片回收
					bitmap.recycle();
				}
				return output;
			}

			@Override
			public String key() { // 将用作cache的key
				return "key";
			}
		};
		String uriUserHead = status.user.profile_image_url;
		Picasso.with(getContext()).load(uriUserHead)
				.transform(userTransformation).into(viewHolder.iv_userhead);

		Transformation transformation = new Transformation() {
			@Override
			public Bitmap transform(Bitmap bitmap) {
				Bitmap newBitmap = null;
				// 自定义变换
				if (bitmap.getHeight() > 700) {
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
					int newWidth = 300;
					int newHeight = 400;
					float scaleWidth = ((float) newWidth) / width;
					float scaleHeight = ((float) newHeight) / height;
					Matrix matrix = new Matrix();
					matrix.postScale(scaleWidth, scaleHeight);
					newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
							height, matrix, true);
					if (bitmap != null && !bitmap.isRecycled()) { // 将旧图片回收
						bitmap.recycle();
					}
				} else {
					newBitmap = bitmap;
				}

				return newBitmap;
			}

			@Override
			public String key() { // 将用作cache的key
				return "key";
			}
		};
		if (status.pic_urls != null) {
			viewHolder.statusPic.setVisibility(View.VISIBLE);
			Picasso.with(getContext()).load(status.bmiddle_pic)
					.transform(transformation).into(viewHolder.statusPic);
		} else {
			viewHolder.statusPic.setVisibility(View.GONE); // 一条属性，解决无中生有
		}

		viewHolder.btnPinglun.setOnClickListener(new OnClickListener() {
			// 评论方法示例
			@Override
			public void onClick(View v) {
				if (status.pic_urls != null) {
					CommentsActivity.actionStart(getContext(), status.idstr,
							status.user.name, status.created_at, status.text,
							status.user.profile_image_url, status.bmiddle_pic,
							String.valueOf(status.attitudes_count),
							String.valueOf(status.comments_count),
							String.valueOf(status.reposts_count));
				} else {
					CommentsActivity.actionStart(getContext(), status.idstr,
							status.user.name, status.created_at, status.text,
							status.user.profile_image_url, null,
							String.valueOf(status.attitudes_count),
							String.valueOf(status.comments_count),
							String.valueOf(status.reposts_count));
				}
			}
		});

		if (status.retweeted_status != null) { // Visibility属性设置，解决子微博内容偶尔消失的Bug
			viewHolder.subLayout.setVisibility(View.VISIBLE);
			viewHolder.textSubContent
					.setText(setTextColor(status.retweeted_status.text));
			if (status.retweeted_status.pic_urls != null) {
				String pic_url = status.retweeted_status.bmiddle_pic;
				Picasso.with(getContext()).load(pic_url).resize(200, 200)
						.centerCrop().into(viewHolder.iv_subContent);
				viewHolder.iv_subContent.setVisibility(View.VISIBLE);

			} else {
				viewHolder.iv_subContent.setVisibility(View.GONE);
			}
		} else {
			viewHolder.subLayout.setVisibility(View.GONE);
		}

		if (status.user.verified == true)
			viewHolder.iv_isv.setVisibility(View.VISIBLE);
		else
			viewHolder.iv_isv.setVisibility(View.GONE);

		viewHolder.textUserName.setText(status.user.name);
		viewHolder.textContent.setText(setTextColor(status.text));

		viewHolder.numberOfDianzan.setText(String
				.valueOf(status.attitudes_count));
		viewHolder.numberOfpinglun.setText(String
				.valueOf(status.comments_count));
		viewHolder.numberOfzhuanfa
				.setText(String.valueOf(status.reposts_count));
		viewHolder.textResource.setText(" " + Html.fromHtml(status.source));
		viewHolder.textTime.setText(dealTime(status.created_at));

		return view;

	}

	@SuppressWarnings("deprecation")
	public String dealTime(String time)// 用于处理微博的发布时间
	{
		Date now = new Date();
		long lnow = now.getTime() / 1000;

		long ldate = Date.parse(time) / 1000;
		Date date = new Date(ldate);

		if ((lnow - ldate) < 60)
			return (lnow - ldate) + "秒前";
		else if ((lnow - ldate) < 60 * 60)
			return ((lnow - ldate) / 60) + "分钟前";
		else
			return time.substring(4, 16);
	}

	public SpannableStringBuilder setTextColor(String str) {
		// 将用户名和话题名变成蓝色
		int bstart = 0;
		int bend = 0;
		int fstart = 0;
		int fend = 0;
		int a = 0;
		int b = 0;
		SpannableStringBuilder style = new SpannableStringBuilder(str);
		while (true) {
			bstart = str.indexOf("@", bend);
			a = str.indexOf(" ", bstart);
			if (bstart < 0) {
				break;
			} else {
				if (a < 0) {
					break;
				} else {
					bend = a;
				}
				style.setSpan(new ForegroundColorSpan(0xFF0099ff), bstart, a,
						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}
		}
		while (true) {
			fstart = str.indexOf("#", fend);
			b = str.indexOf("#", fstart + 1);
			if (fstart < 0) {
				break;
			} else {
				if (b < 0) {
					break;
				} else {
					fend = b + 1;
				}
				style.setSpan(new ForegroundColorSpan(0xFF0099ff), fstart,
						fend, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}
		}
		return style;
	}

	class ViewHolder {

		// 主微博
		ImageView iv_userhead;
		ImageView iv_isv;
		ImageButton btnUserHead;
		ImageView statusPic;
		ImageButton btnStatusImage;
		ImageButton btnPinglun;
		TextView textUserName;
		TextView textContent;
		TextView numberOfDianzan;
		TextView numberOfpinglun;
		TextView numberOfzhuanfa;
		TextView textResource;
		TextView textTime;

		// 子微博
		LinearLayout subLayout;
		ImageView iv_subContent;
		TextView textSubContent; // 子微博内容
	}

}
