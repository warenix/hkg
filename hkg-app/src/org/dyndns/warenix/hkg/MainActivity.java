package org.dyndns.warenix.hkg;

import org.dyndns.warenix.hkg.HKGTopicFragment.HKGThreadListener;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements HKGThreadListener {

	int mCurrentTopicPage = 1;
	int mCurrentThreadPage = 1;
	String mCurrentTopicThreadId;
	String mCurrentTopicTitle;
	boolean isThreadMode = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		showTopic("BW", 1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			// go to first page
			if (isThreadMode) {
				if (mCurrentThreadPage > 1) {
					mCurrentThreadPage = 1;
					showThread(mCurrentTopicThreadId, mCurrentThreadPage);
				}
			} else {
				mCurrentTopicPage = 1;
				mCurrentThreadPage = 1;

				showTopic("BW", 1);
			}

			break;
		case R.id.menu_more:
			if (isThreadMode) {
				showThread(mCurrentTopicThreadId, ++mCurrentThreadPage);
			} else {
				showTopic("BW", ++mCurrentThreadPage);
			}
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	void showTopic(String type, int pageNo) {
		updateTitle(getString(R.string.title_activity_main), pageNo);

		HKGTopicFragment f = HKGTopicFragment.newInstance(type, pageNo);
		f.setHKGThreadListener(this);
		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
				R.anim.slide_in_left, R.anim.slide_out_right);

		ft.replace(R.id.container, f);
		ft.commit();

	}

	void showThread(String threadId, int pageNo) {
		updateTitle(mCurrentTopicTitle, pageNo);

		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
				R.anim.slide_in_left, R.anim.slide_out_right);

		ft.add(R.id.container, HKGThreadFragment.newInstance(threadId, pageNo));
		ft.addToBackStack(null);
		ft.commit();

	}

	@Override
	public void onHKGThreadSelected(HKGThread topic) {
		isThreadMode = true;
		mCurrentTopicTitle = topic.mTitle;
		mCurrentTopicThreadId = topic.mThreadId;
		showThread(topic.mThreadId, 1);
	}

	public void onBackPressed() {
		super.onBackPressed();
		if (isThreadMode) {
			mCurrentThreadPage -= 1;

			if (mCurrentThreadPage == 0) {
				// back to topic list
				mCurrentThreadPage = 1;
				isThreadMode = false;
				updateTitle(getString(R.string.title_activity_main),
						mCurrentThreadPage);
			} else {
				updateTitle(mCurrentTopicTitle, mCurrentThreadPage);
			}
		} else {
			if (mCurrentTopicPage > 1) {
				mCurrentTopicPage -= 1;
			}
			updateTitle(getString(R.string.title_activity_main),
					mCurrentTopicPage);
		}

	}

	private void updateTitle(String title, int pageNo) {
		setTitle(String.format("%d>%s", pageNo, title));
	}
}
