package org.dyndns.warenix.hkg;

import java.util.ArrayList;

import org.dyndns.warenix.abs.activity.SlidingActionBarActivity;
import org.dyndns.warenix.hkg.HKGBookmarkFragment.HKGBookmarkListener;
import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.HKGForumFragment.HKGForumListener;
import org.dyndns.warenix.hkg.HKGSearchFragment.HKGSearchResultListener;
import org.dyndns.warenix.hkg.HKGThread.HKGForum;
import org.dyndns.warenix.hkg.HKGTopicFragment2.HKGThreadListener;
import org.dyndns.warenix.hkg.provider.HKGMetaData;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

public class MainActivity extends SlidingActionBarActivity implements
		HKGListener, HKGThreadListener, HKGForumListener, HKGBookmarkListener,
		HKGSearchResultListener {

	public MainActivity() {
		super(R.string.app_name);
	}

	static final String TAG = "HKGMain";

	enum FragmentTag {
		STATIC, HKG_TOPIC, HKG_THREAD, HKG_BOOKMARK, HKG_SEARCH
	}

	private String mDefaultTimeFilter = "m";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// always enable home button interaction
		getSupportActionBar().setHomeButtonEnabled(true);

		StaticFragment sf = getStaticFragment();

		if (savedInstanceState == null) {
			Log.d(TAG, "init with default forum");
			// provide default forum
			HKGForum forum = new HKGForum("吹水台", "BW");
			sf.saveCurrentForum(forum);
		}

		// HKGForum forum = sf.getCurrentForum();
		updateTitle();
		setupFragmentLayout();
		// ensureFragmentIsReady(forum.mType, sf.getCurrentTopicPageNo());

		// // setup listener
		// HKGBookmarkFragment f = (HKGBookmarkFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.HKG_BOOKMARK.toString());
		// if (f != null) {
		// f.setHKGBookmarkListener(this);
		// showHKGBookmarkFragment(f);
		// }

		// startLoadTopic(forum.mType, sf.getCurrentTopicPageNo());
		//
		// // 1. restore loweset level bookmark/ topic fragment
		// // if ("BM".equals(forum.mType)) {
		// // // restore bookmark
		// // onHKGForumSelected(forum);
		// // } else {
		// // onTopicLoaded(sf.mType, sf.mPageNo, sf.mThreadList);
		// // }
		//
		// onHKGForumSelected(forum);
		//
		// 2. restore thread if it has been opened
		// HKGThreadFragment tf = (HKGThreadFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.HKG_THREAD.toString());
		// if (tf != null) {
		// showHKGThreadFragment(tf);
		// setPageSwitcher(getStaticFragment().mThread);
		// // switchThreadPage(getStaticFragment().mThread);
		// }

	}

	public void onStart() {
		super.onStart();

		// restore thread fragment
		HKGThreadFragment tf = (HKGThreadFragment) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.HKG_THREAD.toString());
		if (tf != null) {
			setPageSwitcher(getStaticFragment().mThread);
		} else {
			final Uri data = getIntent().getData();
			if (data != null) {
				final String url = data.toString();
				String threadId = HKGMaster.extraceThreadIdFromURL(url);
				int pageNo = HKGMaster.extracePageNoFromURL(url);
				if (threadId != null) {
					showThreadById(threadId, pageNo);
				}
			}
		}
		if (getStaticFragment().getSearchQuery() != null) {
			startNewSearch();
		}

		// StaticFragment sf = getStaticFragment();
		// HKGForum forum = sf.getCurrentForum();
		//
		// int currentPageNo = sf.getCurrentTopicPageNo();
		// if (currentPageNo == sf.mPageNo) {
		// // this page has been loaded
		// } else {
		// startLoadTopic(forum.mType, sf.getCurrentTopicPageNo());
		// }
		//
		// // 1. restore loweset level bookmark/ topic fragment
		// if ("BM".equals(forum.mType)) {
		// // restore bookmark
		// HKGBookmarkFragment f = (HKGBookmarkFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.HKG_BOOKMARK.toString());
		// // if (f == null) {
		// // f = HKGBookmarkFragment.newInstance(forum.mType, 1);
		// // }
		// // f.setHKGBookmarkListener(this);
		// // showHKGBookmarkFragment(f);
		// } else {
		// onTopicLoaded(sf.mType, sf.mPageNo, sf.mThreadList);
		// }
		//
		// // 2. restore thread if it has been opened
		// HKGThreadFragment tf = (HKGThreadFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.HKG_THREAD.toString());
		// if (tf != null) {
		// setPageSwitcher(getStaticFragment().mThread);
		// }
	}

	protected StaticFragment getStaticFragment() {
		StaticFragment f = (StaticFragment) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.STATIC.toString());
		if (f == null) {
			f = new StaticFragment();
			getSupportFragmentManager().beginTransaction()
					.add(f, FragmentTag.STATIC.toString())
					.commitAllowingStateLoss();
			getSupportFragmentManager().executePendingTransactions();
		}
		return f;
	}

	@Override
	public boolean onNavigationItemChanged(int itemPosition, long itemId) {
		int newPage = itemPosition + 1;
		Log.d(TAG, String.format("onNavigationItemChanged(), switch page[%d]",
				newPage));

		Toast.makeText(getApplicationContext(),
				getString(R.string.toast_thread_page_loaded, newPage),
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
		startLoadThread(thread, newPage);
		setSwitchThreadPageAdapter(thread.mTitle, thread.mPageCount,
				itemPosition);

		return true;
	}

	void switchThreadPage(HKGThread thread) {
		HKGThreadFragment tf = (HKGThreadFragment) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.HKG_THREAD.toString());
		tf.onThreadLoaded(thread);
	}

	void ensureFragmentIsReady(final String type, final int pageNo) {
		HKGTopicFragment2 cf = (HKGTopicFragment2) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
		if (cf != null) {
			((HKGTopicFragment2) cf).setHKGThreadListener(this);
			return;
		}

		// display UI for thread list
		HKGTopicFragment2 f = HKGTopicFragment2.newInstance(type, pageNo);
		f.setHKGThreadListener(this);
		showHKGTopicFragment(f);
	}

	void setupFragmentLayout() {
		// final String type;
		// final int pageNo = 1;
		// HKGTopicFragment2 cf = (HKGTopicFragment2)
		// getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
		// if (cf != null) {
		// ((HKGTopicFragment2) cf).setHKGThreadListener(this);
		// return;
		// }
		//
		// // display UI for thread list
		// HKGTopicFragment2 f = HKGTopicFragment2.newInstance(type, pageNo);
		// f.setHKGThreadListener(this);
		// showHKGTopicFragment(f);

		StaticFragment sf = getStaticFragment();

		// 1. setup topic/bookmark fragment
		HKGForum forum = sf.getCurrentForum();
		if ("BM".equals(forum.mType)) {
			HKGBookmarkFragment f = (HKGBookmarkFragment) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_BOOKMARK.toString());
			if (f == null) {
				f = HKGBookmarkFragment.newInstance(forum.mType, 1);
				showHKGBookmarkFragment(f);
			}
			f.setHKGBookmarkListener(this);
		} else if ("WS".equals(forum.mType)) {
			HKGSearchFragment f = (HKGSearchFragment) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_SEARCH.toString());
			if (f == null) {
				f = HKGSearchFragment.newInstance(getStaticFragment()
						.getSearchQuery(), 0, mDefaultTimeFilter);
				showHKGSearchFragment(f);
			}

		} else {
			HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
			if (f == null) {
				f = HKGTopicFragment2.newInstance(forum.mType, 1);
				showHKGTopicFragment(f);

			}
			f.setHKGThreadListener(this);

		}

		// 2. setup thread fragment
		// HKGThreadFragment tf = (HKGThreadFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.HKG_THREAD.toString());
		// if (tf != null) {
		// showHKGThreadFragment(tf);
		// }
	}

	// /**
	// * staring point of loading topic from current forum. will make network
	// * call.
	// *
	// * @param type
	// * @param pageNo
	// */
	// void startLoadTopic(final String type, final int pageNo) {
	// // load content
	// new Thread() {
	// public void run() {
	// Log.d(TAG, String.format("Network call, readTopicByPage()"));
	// HKGController controller = HKGController.getController();
	// controller.setHKGListener(MainActivity.this);
	// controller.readTopicByPage(type, pageNo);
	// }
	// }.start();
	//
	// Toast.makeText(MainActivity.this,
	// "Topic Page " + getStaticFragment().getCurrentTopicPageNo(),
	// Toast.LENGTH_SHORT).show();
	// }

	/**
	 * starting point of loading thread content. will make network call.
	 * 
	 * @param thread
	 * @param pageNo
	 */
	void startLoadThread(final HKGThread thread, final int pageNo) {
		Log.d(TAG, String.format("loadThread pageNo[%d]", pageNo));
		new Thread() {
			public void run() {
				HKGController mController = new HKGController();
				mController.setHKGListener(MainActivity.this);
				mController.readThreadByPage(thread, pageNo);
			}
		}.start();

	}

	void setPageSwitcher(final HKGThread thread) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				if (getStaticFragment().mThread != null) {
					// make sure update the ui when current fragment is the
					// thread view
					Log.d(TAG, "setPageSwitcher(), update navigation list item");
					setSwitchThreadPageAdapter(thread.mTitle,
							thread.mPageCount, thread.mSelectedPage - 1);
				}
			}
		});
	}

	@Override
	public void onHKGThreadSelected(HKGThread thread) {
		Log.d(TAG, String.format("showing thread %s", thread.mThreadId));

		if (thread.mPageCount > 0) {
			int lastUnreadPageNo = getLastUnreadPageNo(thread);
			thread.mSelectedPage = lastUnreadPageNo;
			// display UI
			HKGThreadFragment f = HKGThreadFragment.newInstance(thread,
					lastUnreadPageNo);
			showHKGThreadFragment(f);

			// loadThread(thread, 1);
			getStaticFragment().saveThread(thread);
			setPageSwitcher(thread);
		} else {
			Toast.makeText(MainActivity.this,
					getString(R.string.thread_loading_error), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public boolean onHKGThreadRefreshClicked(MenuItem item) {
		if (getStaticFragment().mThread == null) {
			// showTopic("BW", 1);
			int newPageNo = 1;
			getStaticFragment().saveCurrentTopicPageNo(newPageNo);
			// loadTopic("BW", newPageNo);
			HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
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

	@Override
	public boolean onHKGThreadMoreClicked(MenuItem item) {
		HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
		if (f != null) {
			int newPageNo = getStaticFragment().getCurrentTopicPageNo() + 1;
			getStaticFragment().saveCurrentTopicPageNo(newPageNo);
			// loadTopic("BW", newPageNo);
			HKGForum forum = getStaticFragment().getCurrentForum();
			Bundle bundle = HKGTopicFragment2.getShowTopicBundle(forum.mType,
					newPageNo);
			f.refreshTopic(bundle);

			updateTitle();
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SearchView searchView = new SearchView(getSupportActionBar()
				.getThemedContext());
		searchView.setQueryHint(getString(R.string.search_view_hints));
		final MenuItem searchMenuItem = menu.add(Menu.NONE, Menu.NONE, 99,
				getString(R.string.menu_search));
		searchMenuItem.setActionView(searchView).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
						| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.d(TAG, String.format("query[%s]", query));
				searchMenuItem.collapseActionView();
				getStaticFragment().saveSearchQuery(query);

				startNewSearch();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return true;
			}
		});

		final MenuItem gotoMenu = menu.add(Menu.NONE, R.id.goto_menu, 10,
				getString(R.string.menu_goto));
		gotoMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		return true;
		// MenuItem refresh = menu.add("Refresh");
		// refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
		// | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		// refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		//
		// @Override
		// public boolean onMenuItemClick(MenuItem item) {
		// if (getStaticFragment().mThread == null) {
		// // showTopic("BW", 1);
		// int newPageNo = 1;
		// getStaticFragment().saveCurrentTopicPageNo(newPageNo);
		// // loadTopic("BW", newPageNo);
		// HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
		// if (f != null) {
		// HKGForum forum = getStaticFragment().getCurrentForum();
		// Bundle bundle = HKGTopicFragment2.getShowTopicBundle(
		// forum.mType, 1);
		// f.refreshTopic(bundle);
		//
		// updateTitle();
		// }
		// }
		// return true;
		// }
		// });
		//
		// MenuItem more = menu.add("More");
		// more.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
		// | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		// more.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		//
		// @Override
		// public boolean onMenuItemClick(MenuItem item) {
		// HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
		// if (f != null) {
		// int newPageNo = getStaticFragment().getCurrentTopicPageNo() + 1;
		// getStaticFragment().saveCurrentTopicPageNo(newPageNo);
		// // loadTopic("BW", newPageNo);
		// HKGForum forum = getStaticFragment().getCurrentForum();
		// Bundle bundle = HKGTopicFragment2.getShowTopicBundle(
		// forum.mType, newPageNo);
		// f.refreshTopic(bundle);
		//
		// updateTitle();
		// }
		//
		// return true;
		// }
		// });
		// return true;
	}

	public void onBackPressed() {
		super.onBackPressed();

		if (getStaticFragment().mThread != null) {
			getStaticFragment().saveThread(null);
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
			break;
		case R.id.goto_menu:
			gotoThredById();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void gotoThredById() {
		QuantityDialogFragment dialog = new QuantityDialogFragment();
		dialog.show(getSupportFragmentManager(), "Dialog");
	}

	@Override
	public void onTopicLoaded(String type, int pageNo,
			ArrayList<HKGThread> threadList) {
		Log.d(TAG, String.format("onTopicLoaded pageNo[%d]", pageNo));

		if (threadList != null && threadList.size() > 0) {
			// save it so later we can resue it
			getStaticFragment().saveTopic(type, pageNo, threadList);

			HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
			if (f != null) {
				// f.onTopicLoaded(type, pageNo, threadList);
			}
		} else {
			// TODO find a better way to display this
			MainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(MainActivity.this,
							getString(R.string.thread_loading_error),
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public void onThreadLoaded(HKGThread thread) {
		Log.d(TAG, String.format("onThreadLoaded selectedPage[%s]",
				thread.mSelectedPage));
		if (getStaticFragment().mThread != null) {
			// make sure update the ui when current fragment is the thread view
			saveLastUnreadPageNo(thread);

			// save it so later we can reuse it
			getStaticFragment().saveThread(thread);
			setPageSwitcher(thread);

			HKGThreadFragment f = (HKGThreadFragment) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_THREAD.toString());
			if (f != null) {
				f.onThreadLoaded(thread);
			}
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

		/**
		 * search query obtained from SearchView widge. Will be passed to
		 * HKGSearchFragment
		 */
		private String mSearchQuery;

		public StaticFragment() {
			setRetainInstance(true);
		}

		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (savedInstanceState != null) {
				mForum = (HKGForum) savedInstanceState.get("mForum");
				mThread = (HKGThread) savedInstanceState.get("mThread");
				mCurrentTopicPage = savedInstanceState
						.getInt("mCurrentTopicPage");
				mType = savedInstanceState.getString("mType");
				mPageNo = savedInstanceState.getInt("mPageNo");
				mThreadList = (ArrayList<HKGThread>) savedInstanceState
						.getSerializable("mThreadList");
			}
		}

		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
			super.onSaveInstanceState(savedInstanceState);

			savedInstanceState.putSerializable("mForum", mForum);
			savedInstanceState.putSerializable("mThread", mThread);
			savedInstanceState.putInt("mCurrentTopicPage", mCurrentTopicPage);
			savedInstanceState.putString("mType", mType);
			savedInstanceState.putInt("mPageNo", mPageNo);
			savedInstanceState.putSerializable("mThreadList", mThreadList);
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

		public void saveSearchQuery(String query) {
			mSearchQuery = query;
		}

		public String getSearchQuery() {
			return mSearchQuery;
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
				.findFragmentByTag(FragmentTag.HKG_THREAD.toString());
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

		// show forum on right fragment
		if ("BM".equals(forum.mType)) {
			HKGBookmarkFragment f = (HKGBookmarkFragment) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_BOOKMARK.toString());
			if (f == null) {
				f = HKGBookmarkFragment.newInstance(forum.mType, 1);
				f.setHKGBookmarkListener(this);
			}
			showHKGBookmarkFragment(f);
		} else if ("WS".equals(forum.mType)) {
			HKGSearchFragment f = (HKGSearchFragment) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_SEARCH.toString());
			if (f == null) {
				f = HKGSearchFragment.newInstance(getStaticFragment()
						.getSearchQuery(), 0, mDefaultTimeFilter);
			}
			showHKGSearchFragment(f);

		} else {
			HKGTopicFragment2 f = (HKGTopicFragment2) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_TOPIC.toString());
			if (f == null) {
				f = HKGTopicFragment2.newInstance(forum.mType, 1);
				f.setHKGThreadListener(this);

				showHKGTopicFragment(f);
			} else {
				Bundle bundle = HKGTopicFragment2.getShowTopicBundle(
						forum.mType, 1);
				f.refreshTopic(bundle);
			}

		}

		updateTitle();
	}

	/**
	 * update actionbar title to display current forum & page no
	 */
	public void updateTitle() {
		String appName = getResources().getString(R.string.app_name);
		HKGForum forum = getStaticFragment().getCurrentForum();
		this.setTitle(String.format("%s - %d @%s", forum.mName,
				getStaticFragment().getCurrentTopicPageNo(), appName));
	}

	/**
	 * update actionbar title to display current forum & page no
	 */
	public void updateTitleAsSearch() {
		String appName = getResources().getString(R.string.app_name);
		HKGForum forum = getStaticFragment().getCurrentForum();
		String query = getStaticFragment().getSearchQuery();
		this.setTitle(String.format("%s - %d @%s", query, getStaticFragment()
				.getCurrentTopicPageNo(), appName));
	}

	private void showHKGThreadFragment(HKGThreadFragment f) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
		// R.anim.slide_in_left, R.anim.slide_out_right);
		ft.replace(R.id.container, f, FragmentTag.HKG_THREAD.toString());
		ft.addToBackStack(null);
		ft.commit();
	}

	private void showHKGTopicFragment(HKGTopicFragment2 f) {
		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		// ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
		// R.anim.slide_in_left, R.anim.slide_out_right);

		ft.replace(R.id.container, f, FragmentTag.HKG_TOPIC.toString());
		ft.commit();
	}

	private void showHKGBookmarkFragment(HKGBookmarkFragment f) {
		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		// ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
		// R.anim.slide_in_left, R.anim.slide_out_right);

		ft.replace(R.id.container, f, FragmentTag.HKG_BOOKMARK.toString());
		ft.commit();
	}

	private void showHKGSearchFragment(HKGSearchFragment f) {
		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		// ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
		// R.anim.slide_in_left, R.anim.slide_out_right);

		ft.replace(R.id.container, f, FragmentTag.HKG_SEARCH.toString());
		ft.commit();
	}

	/**
	 * display search fragment for search result
	 */
	void startNewSearch() {
		String query = getStaticFragment().getSearchQuery();
		if (query != null && query.length() > 0) {
			HKGSearchFragment f = (HKGSearchFragment) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.HKG_SEARCH.toString());
			f = HKGSearchFragment.newInstance(query, 0, mDefaultTimeFilter);
			showHKGSearchFragment(f);

			updateTitleAsSearch();
		}
	}

	@Override
	public void onHKGBookmarkSelected(HKGBookmark bookmark) {
		onHKGThreadSelected(bookmark);
	}

	@Override
	public void onHKGSearchResultSelected(HKGSearchResult searchResult) {
		onHKGThreadSelected(searchResult);
	}

	/**
	 * Find the last unread page no for a particular thread
	 * 
	 * @param thread
	 * @return the last unread page no. default is the selected page.
	 */
	private int getLastUnreadPageNo(HKGThread thread) {
		int lastUnreadPageNo = thread.mSelectedPage;
		Uri uri = HKGMetaData
				.getUriShowLastVisitThreadPage(thread.mThreadId, 0);
		Cursor cursor = getApplication().getContentResolver().query(uri, null,
				null, null, null);
		if (cursor != null && cursor.moveToNext()) {
			lastUnreadPageNo = cursor
					.getInt(cursor
							.getColumnIndex(HKGMetaData.HKGThreadLastVisitColumns.pageNo));
		}
		return lastUnreadPageNo;
	}

	/**
	 * Save the selected page no of the thread as last unread
	 * 
	 * @param thread
	 */
	private void saveLastUnreadPageNo(HKGThread thread) {
		Uri uri = HKGMetaData
				.getUriShowLastVisitThreadPage(thread.mThreadId, 0);

		ContentValues values = new ContentValues();
		values.put(HKGMetaData.HKGThreadLastVisitColumns.threadId,
				thread.mThreadId);
		values.put(HKGMetaData.HKGThreadLastVisitColumns.pageNo,
				thread.mSelectedPage);

		Cursor c = getContentResolver().query(uri, null, null, null, null);
		if (c == null) {
			Uri insertUri = getContentResolver().insert(uri, values);
			Log.d(TAG, "inserted uri:" + insertUri);
		} else {
			int rows = getContentResolver().update(uri, values, null, null);
			Log.d(TAG, "updated rows:" + rows);
		}
	}

	public static class QuantityDialogFragment extends DialogFragment implements
			OnClickListener {

		private EditText editQuantity;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			editQuantity = new EditText(getActivity());
			editQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);

			return new AlertDialog.Builder(getActivity())
					.setTitle(R.string.app_name)
					.setMessage(R.string.dialog_input_thread_id)
					.setPositiveButton(
							getString(R.string.dialog_input_thread_ok), this)
					.setNegativeButton(
							getString(R.string.dialog_input_thread_cancel),
							null).setView(editQuantity).create();

		}

		@Override
		public void onClick(DialogInterface dialog, int position) {

			String value = editQuantity.getText().toString();
			Log.d("Quantity: ", value);
			MainActivity callingActivity = (MainActivity) getActivity();
			callingActivity.onUserSelectValue(value);
			dialog.dismiss();
		}
	}

	public void onUserSelectValue(String selectedValue) {
		if (!selectedValue.equals("")) {
			String threadId = selectedValue;
			int pageNo = 1;
			showThreadById(threadId, pageNo);
		}
	}

	/**
	 * show particular thread
	 * 
	 * @param threadId
	 * @param pageNo
	 */
	private void showThreadById(String threadId, int pageNo) {
		String user = null;
		int repliesCount = 0;
		String title = null;
		int rating = 0;
		int pageCount = 1;
		HKGThread thread = new HKGThread(threadId, user, repliesCount, title,
				rating, pageCount);
		thread.mSelectedPage = pageNo;
		onHKGThreadSelected(thread);
	}
}
