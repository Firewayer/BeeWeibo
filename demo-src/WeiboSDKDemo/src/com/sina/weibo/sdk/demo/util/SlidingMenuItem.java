package com.sina.weibo.sdk.demo.util;

public class SlidingMenuItem {
	private String name;

	private int imageId;

	public SlidingMenuItem(String name, int imageId) {
		this.name = name;
		this.imageId = imageId;
	}

	public String getName() {
		return name;
	}

	public int getImageId() {
		return imageId;
	}
}
