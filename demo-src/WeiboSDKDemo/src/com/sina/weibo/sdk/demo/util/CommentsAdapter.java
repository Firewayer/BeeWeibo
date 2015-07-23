package com.sina.weibo.sdk.demo.util;

import java.util.ArrayList;
import java.util.List;

import com.sina.weibo.sdk.demo.R;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentsAdapter extends ArrayAdapter<Comment> {

	private int resourceId;

	private List<Comment> mComments;

	public CommentsAdapter(Context context, int textResource,
			List<Comment> comments) {
		super(context, textResource, comments);
		resourceId = textResource;
		mComments = comments;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Comment comment = mComments.get(position);
		View view;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
		} else {
			view = convertView;
		}
		ImageView mImageViewUserHead = (ImageView) view
				.findViewById(R.id.img_comment_item_head);
		TextView textContent = (TextView) view
				.findViewById(R.id.txt_comment_item_content);
		TextView textTime = (TextView) view
				.findViewById(R.id.txt_comment_item_day);
		TextView textName = (TextView) view
				.findViewById(R.id.txt_comment_item_uname);
		textContent.setText(comment.text);
		textTime.setText(comment.created_at);
		textName.setText(comment.user.name);
		Picasso.with(getContext()).load(comment.user.profile_image_url)
				.into(mImageViewUserHead);
		return view;
	}

}
