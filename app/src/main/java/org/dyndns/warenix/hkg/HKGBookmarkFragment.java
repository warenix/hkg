package org.dyndns.warenix.hkg;

import org.dyndns.warenix.hkg.provider.HKGMetaData;
import org.dyndns.warenix.ui.utils.SwipeDismissListViewTouchListener;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


public class HKGBookmarkFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = "HKGBookmarkFragment";

	private static final int TOPIC_LIST_LOADER = 0x01;

	public static final String BUNDLE_FORUM = "forum";
	public static final String BUNDLE_PAGE_NO = "page_no";

	CursorAdapter mAdapter;
	HKGBookmarkListener mListener;

	private boolean mEnableEditListItem;

	/**
	 * Create a new instance and load topics from given parameters
	 * 
	 * @param type
	 *            forum type
	 * @param pageNo
	 *            page no, 1 based.
	 * @return
	 */
	public static HKGBookmarkFragment newInstance(String type, int pageNo) {
		HKGBookmarkFragment f = new HKGBookmarkFragment();
		Bundle bundle = getShowTopicBundle(type, pageNo);
		f.setArguments(bundle);
		f.setHasOptionsMenu(true);
		return f;
	}

	/**
	 * This bundle control which forum and page no topics to be loaded
	 * 
	 * @param forum
	 *            forum type
	 * @param pageNo
	 *            page no, 1 based.
	 * @return
	 */
	public static Bundle getShowTopicBundle(String forum, int pageNo) {
		Bundle bundle = new Bundle();
		bundle.putString(BUNDLE_FORUM, forum);
		bundle.putInt(BUNDLE_PAGE_NO, pageNo);
		return bundle;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("FragmentList", "Item clicked: " + id);
		if (mListener != null) {
			mListener.onHKGBookmarkSelected((HKGBookmark) mAdapter
					.getItem(position));
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getLoaderManager().initLoader(TOPIC_LIST_LOADER, getArguments(), this);
		mAdapter = new TopicCursorAdapter(getActivity(), null);
		setListAdapter(mAdapter);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setCacheColorHint(Color.BLACK);
		setListShown(false);
		setEmptyText(getString(R.string.bookmark_list_empty));
		setHasOptionsMenu(true);

		mEnableEditListItem = false;
		enableSwipeToDelete(mEnableEditListItem);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, String.format("onCreateOptionMenu"));

		MenuItem bookmark = menu.add("Edit");
		bookmark.setTitle(mEnableEditListItem ? getString(R.string.menu_done)
				: getString(R.string.menu_edit));
		bookmark.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		bookmark.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				mEnableEditListItem = !mEnableEditListItem;
				enableSwipeToDelete(mEnableEditListItem);
				// update label
				item.setTitle(mEnableEditListItem ? getString(R.string.menu_done)
						: getString(R.string.menu_edit));
				return true;
			}
		});

	}

	public void refreshTopic(Bundle bundle) {
		setListShown(false);
		getLoaderManager().restartLoader(TOPIC_LIST_LOADER, bundle, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (bundle != null) {
			String forum = bundle.getString(BUNDLE_FORUM);
			int pageNo = bundle.getInt(BUNDLE_PAGE_NO);
			Uri uri = HKGMetaData.getUriListBookmark();
			CursorLoader cursorLoader = new CursorLoader(getActivity(), uri,
					null, null, null, null);
			return cursorLoader;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		setListShown(true);
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	void enableSwipeToDelete(boolean enable) {
		ListView listView = getListView();

		SwipeDismissListViewTouchListener touchListener = null;

		if (enable) {
			// Create a ListView-specific touch listener. ListViews are given
			// special treatment because
			// by default they handle touches for their list items... i.e.
			// they're
			// in charge of drawing
			// the pressed state (the list selector), handling list item clicks,
			// etc.
			touchListener = new SwipeDismissListViewTouchListener(listView,
					new SwipeDismissListViewTouchListener.OnDismissCallback() {
						@Override
						public void onDismiss(ListView listView,
								int[] reverseSortedPositions) {

							for (int position : reverseSortedPositions) {

								HKGBookmark bookmark = (HKGBookmark) mAdapter
										.getItem(position);
								Log.d(TAG,
										String.format(
												"onDismiss position[%d] rowId[%d] threadId[%s]",
												position, bookmark.mRowId,
												bookmark.mThreadId));

								// remove bookmark
								// mAdapter.remove(mAdapter.getItem(position));
								Uri uri = HKGMetaData
										.getUriShowBookmarkById(bookmark.mRowId);
								getActivity().getContentResolver().delete(uri,
										null, null);
							}
							// mAdapter.notifyDataSetChanged();

							// do not hide list to prevent ui blinking
							getLoaderManager().restartLoader(TOPIC_LIST_LOADER,
									getArguments(), HKGBookmarkFragment.this);
						}
					});

			listView.setOnTouchListener(touchListener);
			// Setting this scroll listener is required to ensure that during
			// ListView scrolling,
			// we don't look for swipes.
			listView.setOnScrollListener(touchListener.makeScrollListener());
		} else {
			listView.setOnTouchListener(null);
		}

	}

	static class TopicCursorAdapter extends CursorAdapter {
		private static LayoutInflater sInflater = null;

		public TopicCursorAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public TopicCursorAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}

		public TopicCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
		}

		static class ViewHolder {
			TextView line1;
			TextView line2;
			TextView left;
		}

		@Override
		public Object getItem(int position) {
			Cursor cursor = getCursor();
			cursor.moveToPosition(position);
			return createHKGTopicFromCursor(cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();

			HKGThread topic = createHKGTopicFromCursor(cursor);

			// temp fix when bookmark is not complete...
			if (topic.mTitle == null) {
				topic.mTitle = "";
			}
			viewHolder.line1.setText(String.format("%s",
					Html.fromHtml(topic.mTitle)));
			if (topic.mRating < 0) {
				viewHolder.line1.setTextColor(Color.GRAY);
			} else {
				viewHolder.line1.setTextColor(Color.LTGRAY);
			}
			viewHolder.line2.setText(String.format("%s \u2764 %d\t\t[%s]",
					topic.mUser, topic.mRating, topic.mThreadId));
			viewHolder.left.setText(String.format("%d", topic.mRepliesCount));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View row = null;
			if (sInflater == null) {
				sInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			row = sInflater.inflate(R.layout.thread_header, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.left = (TextView) row.findViewById(R.id.left);
			viewHolder.line1 = (TextView) row.findViewById(R.id.line1);
			viewHolder.line2 = (TextView) row.findViewById(R.id.line2);
			row.setTag(viewHolder);

			return row;
		}

		static HKGThread createHKGTopicFromCursor(Cursor cursor) {
			long rowId = cursor.getInt(cursor
					.getColumnIndex(HKGMetaData.BookmarkColumns.ID));
			String threadId = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.BookmarkColumns.threadId));
			String user = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.BookmarkColumns.user));
			int repliesCount = cursor.getInt(cursor
					.getColumnIndex(HKGMetaData.BookmarkColumns.repliesCount));
			String title = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.BookmarkColumns.title));
			int rating = cursor.getInt(cursor
					.getColumnIndex(HKGMetaData.BookmarkColumns.rating));
			int pageCount = cursor.getInt(cursor
					.getColumnIndex(HKGMetaData.BookmarkColumns.pageCount));
			int selectedPage = cursor
					.getInt(cursor
							.getColumnIndex(HKGMetaData.BookmarkColumns.last_page_no_seen));
			HKGBookmark topic = new HKGBookmark(rowId, threadId, user,
					repliesCount, title, rating, pageCount);
			topic.mSelectedPage = selectedPage;
			return topic;
		}
	};

	public void setHKGBookmarkListener(HKGBookmarkListener listener) {
		mListener = listener;
	}

	interface HKGBookmarkListener {
		public void onHKGBookmarkSelected(HKGBookmark bookmark);
	}

}