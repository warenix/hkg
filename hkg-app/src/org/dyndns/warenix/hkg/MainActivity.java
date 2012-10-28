package org.dyndns.warenix.hkg;

import java.util.ArrayList;

import org.dyndns.warenix.abs.activity.SlidingActionBarActivity;
import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.HKGForumFragment.HKGForumListener;
import org.dyndns.warenix.hkg.HKGThread.HKGForum;
import org.dyndns.warenix.hkg.HKGTopicFragment2.HKGThreadListener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class MainActivity extends SlidingActionBarActivity implements
		HKGListener, HKGThreadListener, HKGForumListener {

	public MainActivity() {
		super(R.string.app_name);
	}

	static final String TAG = "HKGMain";

	enum FragmentTag {
		STATIC, TOPIC, THREAD
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// always enable home button interaction
		getSupportActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
			// use a fragment to hold data across orientatoin change
			getSupportFragmentManager().beginTransaction()
					.add(new StaticFragment(), FragmentTag.STATIC.toString())
					.commit();
			getSupportFragmentManager().executePendingTransactions();

			// provide default forum
			HKGForum forum = new HKGForum("吹水台", "BW");
			getStaticFragment().saveCurrentForum(forum);

			updateTitle();

			showTopic(forum.mType, getStaticFragment().getCurrentTopicPageNo());
			loadTopic(forum.mType, getStaticFragment().getCurrentTopicPageNo());
		} else {
			HKGForum forum = getStaticFragment().getCurrentForum();
			showTopic(forum.mType, getStaticFragment().getCurrentTopicPageNo());

			// restore state
			StaticFragment sf = getStaticFragment();
			onTopicLoaded(sf.mType, sf.mPageNo, sf.mThreadList);

			updateTitle();

			// restore thread is loaded
			HKGThreadFragment tf = (HKGThreadFragment) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.THREAD.toString());
			if (tf != null) {

				setPageSwitcher(getStaticFragment().mThread);
				// switchThreadPage(getStaticFragment().mThread);
			}
		}

	}

	protected StaticFragment getStaticFragment() {
		return (StaticFragment) getSupportFragmentManager().findFragmentByTag(
				FragmentTag.STATIC.toString());
	}

	@Override
	public boolean onNavigationItemChanged(int itemPosition, long itemId) {
		int newPage = itemPosition + 1;
		Log.d(TAG, String.format("onNavigationItemChanged(), switch page[%d]",
				newPage));

		Toast.makeText(getApplicationContext(), "new page" + newPage,
				Toast.LENGTH_SHORT).show();

		// TODO fix switch page. temporarily comment out to test thread fragment
		// orientation change.

		// Fragment f = getSupportFragmentManager().findFragmentById(
		// R.id.container);
		// if (f instanceof HKGThreadFragment) {
		// ((HKGThreadFragment) f).switchPage(mThread.mThreadId,
		// itemPosition + 1);
		// }
		HKGThread thread = getStaticFragment().mThread;
		loadThread(thread, newPage);
		setSwitchThreadPageAdapter(thread.mTitle, thread.mPageCount,
				itemPosition);

		return true;
	}

	void switchThreadPage(HKGThread thread) {
		HKGThreadFragment tf = (HKGThreadFragment) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.THREAD.toString());
		tf.onThreadLoaded(thread);
	}

	void showTopic(final String type, final int pageNo) {
		HKGTopicFragment2 cf = (HKGTopicFragment2) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.TOPIC.toString());
		if (cf != null) {
			((HKGTopicFragment2) cf).setHKGThreadListener(this);
			return;
		}

		// display UI for thread list
		HKGTopicFragment2 f = HKGTopicFragment2.newInstance(type, pageNo);
		f.setHKGThreadListener(this);

		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
				R.anim.slide_in_left, R.anim.slide_out_right);

		ft.replace(R.id.container, f, FragmentTag.TOPIC.toString());
		ft.commit();
	}

	void loadTopic(final String type, final int pageNo) {
		// load content
		new Thread() {
			public void run() {
				Log.d(TAG, String.format("Network call, readTopicByPage()"));
				HKGController controller = HKGController.getController();
				controller.setHKGListener(MainActivity.this);
				controller.readTopicByPage(type, pageNo);
			}
		}.start();

		Toast.makeText(MainActivity.this,
				"Page " + getStaticFragment().getCurrentTopicPageNo(),
				Toast.LENGTH_SHORT).show();
	}

	void loadThread(final HKGThread thread, final int pageNo) {
		Log.d(TAG, String.format("loadThread pageNo[%d]", pageNo));
		new Thread() {
			public void run() {
				Log.d(TAG, String.format("Network call, readThreadByPage()"));
				HKGController mController = new HKGController();
				mController.setHKGListener(MainActivity.this);
				mController.readThreadByPage(thread, pageNo);
			}
		}.start();

	}

	void setPageSwitcher(final HKGThread thread) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				Log.d(TAG, "setPageSwitcher(), update navigation list item");
				setSwitchThreadPageAdapter(thread.mTitle, thread.mPageCount,
						thread.mSelectedPage - 1);
			}
		});
	}

	@Override
	public void onHKGThreadSelected(HKGThread thread) {
		Log.d(TAG, String.format("showing thread %s", thread.mThreadId));

		if (thread.mPageCount > 0) {
			// display UI
			HKGThreadFragment f = HKGThreadFragment.newInstance(thread, 1);
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.setCustomAnimations(R.anim.slide_in_left,
					R.anim.slide_out_right, R.anim.slide_in_left,
					R.anim.slide_out_right);
			ft.add(R.id.container, f, FragmentTag.THREAD.toString());
			ft.addToBackStack(null);
			ft.commit();

			// loadThread(thread, 1);
			getStaticFragment().saveThread(thread);
			setPageSwitcher(thread);
		} else {
			Toast.makeText(MainActivity.this,
					"This thread contains no page, probably being deleted",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem refresh = menu.add("Refresh");
		refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (getStaticFragment().mThread == null) {
					clearTopicList();

					// showTopic("BW", 1);
					int newPageNo = 1;
					getStaticFragment().saveCurrentTopicPageNo(newPageNo);
					// loadTopic("BW", newPageNo);
					HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
							.findFragmentByTag(FragmentTag.TOPIC.toString());
					if (f != null) {
						HKGForum forum = getStaticFragment().getCurrentForum();
						Bundle bundle = HKGTopicFragment2.getShowTopicBundle(
								forum.mType, 1);
						f.refreshTopic(bundle);

						updateTitle();
					}
				}
				return true;
			}
		});

		MenuItem more = menu.add("More");
		more.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		more.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				clearTopicList();

				HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
						.findFragmentByTag(FragmentTag.TOPIC.toString());
				if (f != null) {
					int newPageNo = getStaticFragment().getCurrentTopicPageNo() + 1;
					getStaticFragment().saveCurrentTopicPageNo(newPageNo);
					// loadTopic("BW", newPageNo);
					HKGForum forum = getStaticFragment().getCurrentForum();
					Bundle bundle = HKGTopicFragment2.getShowTopicBundle(
							forum.mType, newPageNo);
					f.refreshTopic(bundle);

					updateTitle();
				}

				return true;
			}
		});
		return true;
	}

	public void onBackPressed() {
		super.onBackPressed();

		if (getStaticFragment().mThread != null) {
			getStaticFragment().saveThread(null);
			getStaticFragment().saveCurrentTopicPageNo(1);
			setActionBarList(null, -1);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (getStaticFragment().mThread != null) {
				this.onBackPressed();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTopicLoaded(String type, int pageNo,
			ArrayList<HKGThread> threadList) {
		Log.d(TAG, String.format("onTopicLoaded pageNo[%d]", pageNo));

		if (threadList.size() > 0) {
			// save it so later we can resue it
			getStaticFragment().saveTopic(type, pageNo, threadList);

			HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.TOPIC.toString());
			if (f != null) {
				// f.onTopicLoaded(type, pageNo, threadList);
			}
		} else {
			// TODO find a better way to display this
			MainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(MainActivity.this, "No thread in this page",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public void onThreadLoaded(HKGThread thread) {
		Log.d(TAG, String.format("onThreadLoaded selectedPage[%s]",
				thread.mSelectedPage));
		// save it so later we can resue it
		getStaticFragment().saveThread(thread);

		HKGThreadFragment f = (HKGThreadFragment) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.THREAD.toString());
		if (f != null) {
			f.onThreadLoaded(thread);
		}
	}

	private void clearTopicList() {
		HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.TOPIC.toString());
		if (f != null) {
			// f.clear();
		}
	}

	public static class StaticFragment extends Fragment {
		// current forum
		private HKGForum mForum;

		// topic fragment
		private String mType;
		private int mPageNo;
		private ArrayList<HKGThread> mThreadList;

		// thread fragment
		private HKGThread mThread;

		// topic pageNo
		int mCurrentTopicPage = 1;

		public StaticFragment() {
			setRetainInstance(true);
		}

		public void saveThread(HKGThread thread) {
			mThread = thread;
		}

		public void saveTopic(String type, int pageNo,
				ArrayList<HKGThread> threadList) {
			mType = type;
			mPageNo = pageNo;
			mThreadList = threadList;
		}

		public void saveCurrentTopicPageNo(int pageNo) {
			mCurrentTopicPage = pageNo;
		}

		public int getCurrentTopicPageNo() {
			return mCurrentTopicPage;
		}

		public void saveCurrentForum(HKGForum forum) {
			mForum = forum;
		}

		public HKGForum getCurrentForum() {
			return mForum;
		}
	}

	@Override
	public Fragment getBehindFragment() {
		HKGForumFragment f = HKGForumFragment.newInstance();
		f.setHKGForumListener(this);
		return f;
	}

	@Override
	public void onHKGForumSelected(HKGForum forum) {
		// UI work
		// 1. remove thread fragment if any
		HKGThreadFragment tf = (HKGThreadFragment) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.THREAD.toString());
		if (tf != null) {
			getSupportFragmentManager().popBackStackImmediate();
			// reset actionbar list adpater
			getStaticFragment().saveThread(null);
			getStaticFragment().saveCurrentTopicPageNo(1);
			setActionBarList(null, -1);
		}
		// 2. collapse hidden fragment
		toggle();

		getStaticFragment().saveCurrentForum(forum);

		int newPageNo = 1;
		getStaticFragment().saveCurrentTopicPageNo(newPageNo);
		HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.TOPIC.toString());
		if (f != null) {
			Bundle bundle = HKGTopicFragment2
					.getShowTopicBundle(forum.mType, 1);
			f.refreshTopic(bundle);
		}

		updateTitle();
	}

	public void updateTitle() {
		String appName = getResources().getString(R.string.app_name);
		HKGForum forum = getStaticFragment().getCurrentForum();
		this.setTitle(String.format("%s@%s - %d", appName, forum.mName,
				getStaticFragment().getCurrentTopicPageNo()));
	}
}
