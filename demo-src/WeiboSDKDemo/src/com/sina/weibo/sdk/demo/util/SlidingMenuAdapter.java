package com.sina.weibo.sdk.demo.util;

import java.util.List;

import com.sina.weibo.sdk.demo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SlidingMenuAdapter extends ArrayAdapter<SlidingMenuItem> {
	private int resourceId;

	public SlidingMenuAdapter(Context context, int resource,
			List<SlidingMenuItem> objects) {
		super(context, resource, objects);
		resourceId = resource;
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SlidingMenuItem mSlidingMenuItem = getItem(position);
		View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
		ImageView menuImage = (ImageView) view.findViewById(R.id.menu_image);
		TextView menuTextView = (TextView) view.findViewById(R.id.menu_name);
		menuImage.setImageResource(mSlidingMenuItem.getImageId());
		menuTextView.setText(mSlidingMenuItem.getName());
		return view;

	}

}
