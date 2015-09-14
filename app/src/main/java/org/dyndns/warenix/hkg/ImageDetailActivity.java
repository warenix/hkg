/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dyndns.warenix.hkg;

import java.util.ArrayList;

import org.dyndns.warenix.abs.activity.SimpleABSActionbarActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageFetcher;

public class ImageDetailActivity extends SimpleABSActionbarActivity implements
		OnClickListener {
	private static final String TAG = "ImageDetailActivity";

	private static final String IMAGE_CACHE_DIR = "images";
	public static final String EXTRA_IMAGE = "extra_image";

	private ImagePagerAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	private ViewPager mPager;
	private int total = 0;
	private int mCurrentImageIndex = 0;

	private ArrayList<String> mImageList;

	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mImageList = getIntent().getStringArrayListExtra(EXTRA_IMAGE);
		total = mImageList.size();
		if (total > 0) {
			updateTitle(0);
		}
		// Fetch screen height and width, to use as our max size when loading
		// images as this
		// activity runs full screen
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		// For this sample we'll use half of the longest width to resize our
		// images. As the
		// image scaling ensures the image is larger than this, we should be
		// left with a
		// resolution that is appropriate for both portrait and landscape. For
		// best image quality
		// we shouldn't divide by 2, but this will use more memory and require a
		// larger memory
		// cache.
		final int longest = (height > width ? height : width);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				this, IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory
		cacheParams.compressQuality = 100;

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(this, longest);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageFetcher.setImageFadeIn(true);
		mImageFetcher.setErrorImage(R.drawable.ic_launcher);

		// Set up ViewPager and backing adapter
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		/*
		 * mPager.setPageMargin((int) getResources().getDimension(
		 * R.dimen.image_detail_pager_margin));
		 */
		mPager.setPageMargin(2);
		mPager.setOffscreenPageLimit(2);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int page) {
				updateTitle(page);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		// Set up activity to go full screen
		// getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

		// Enable some additional newer visibility and ActionBar features to
		// create a more
		// immersive photo viewing experience
		// if (Utils.hasHoneycomb()) {
		// final ActionBar actionBar = getActionBar();
		//
		// // Hide title text and set home as up
		// actionBar.setDisplayShowTitleEnabled(false);
		// actionBar.setDisplayHomeAsUpEnabled(true);
		//
		// // Hide and show the ActionBar as the visibility changes
		// mPager.setOnSystemUiVisibilityChangeListener(new
		// View.OnSystemUiVisibilityChangeListener() {
		// @Override
		// public void onSystemUiVisibilityChange(int vis) {
		// if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
		// actionBar.hide();
		// } else {
		// actionBar.show();
		// }
		// }
		// });
		//
		// // Start low profile mode and hide ActionBar
		// mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		// actionBar.hide();
		// }

		// Set the current item based on the extra passed in to this activity
		/*
		 * final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE,
		 * -1); if (extraCurrentItem != -1) {
		 * mPager.setCurrentItem(extraCurrentItem); }
		 */

	}

	private void updateTitle(int imageIndex) {
		mCurrentImageIndex = imageIndex;
		setTitle(String.format("%s %d/%d",
				getResources().getString(R.string.app_name), imageIndex + 1,
				total));
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;
			/*
			 * case R.id.clear_cache: mImageFetcher.clearCache();
			 * Toast.makeText( this,
			 * R.string.clear_cache_complete_toast,Toast.LENGTH_SHORT).show();
			 * return true;
			 */
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageFetcher
	 */
	public ImageFetcher getImageFetcher() {
		return mImageFetcher;
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of
	 * FragmentStatePagerAdapter as there could be a large number of items in
	 * the ViewPager and we don't want to retain them all in memory at once but
	 * create/destroy them on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public ImagePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mImageList == null ? 0 : mImageList.size();
		}

		@Override
		public Fragment getItem(int position) {
			return ImageDetailFragment.newInstance(getImageUrl(position), "");
		}
	}

	protected String getImageUrl(int position) {
		String imageUrl = mImageList.get(position);
		return imageUrl;
	}

	/**
	 * Set on the ImageView in the ViewPager children fragments, to
	 * enable/disable low profile mode when the ImageView is touched.
	 */
	@TargetApi(11)
	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		// final int vis = mPager.getSystemUiVisibility();
		// if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
		// mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		// } else {
		// mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		// }
		/* mInfo.setVisibility(enableSwipe ? View.VISIBLE : View.INVISIBLE); */
		// open browser
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(getImageUrl(mCurrentImageIndex)));
		startActivity(i);
	}
}
