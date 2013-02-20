package org.dyndns.warenix.hkg;

import org.dyndns.warenix.hkg.provider.HKGMetaData;
import org.dyndns.warenix.ui.utils.SwipeDismissListViewTouchListener;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class HKGSearchFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = "HKGSearchFragment";

	private static final int TOPIC_LIST_LOADER = 0x01;

	public static final String BUNDLE_FORUM = "forum";
	public static final String BUNDLE_PAGE_NO = "page_no";
	public static final String BUNDLE_TIME_FILTER = "time_filter";

	CursorAdapter mAdapter;
	HKGSearchResultListener mListener;

	private boolean mEnableEditListItem;

	/**
	 * Create a new instance and load topics from given parameters
	 * 
	 * @param query
	 *            forum type
	 * @param pageNo
	 *            page no, 1 based.
	 * @return
	 */
	public static HKGSearchFragment newInstance(String query, int pageNo,
			String timeFilter) {
		HKGSearchFragment f = new HKGSearchFragment();
		Bundle bundle = getShowTopicBundle(query, pageNo, timeFilter);
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
	public static Bundle getShowTopicBundle(String forum, int pageNo,
			String timeFilter) {
		Bundle bundle = new Bundle();
		bundle.putString(BUNDLE_FORUM, forum);
		bundle.putInt(BUNDLE_PAGE_NO, pageNo);
		bundle.putString(BUNDLE_TIME_FILTER, timeFilter);
		return bundle;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, "Item clicked position: " + position);
		if (mListener != null) {
			mListener.onHKGSearchResultSelected((HKGSearchResult) mAdapter
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

		setHasOptionsMenu(true);

		mEnableEditListItem = false;
		enableSwipeToDelete(mEnableEditListItem);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (HKGSearchResultListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement HKGSearchResultListener");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, String.format("onCreateOptionMenu"));

		// MenuItem bookmark = menu.add("Edit");
		// bookmark.setTitle(mEnableEditListItem ? "Done" : "Edit");
		// bookmark.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
		// | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		// bookmark.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		//
		// @Override
		// public boolean onMenuItemClick(MenuItem item) {
		// mEnableEditListItem = !mEnableEditListItem;
		// enableSwipeToDelete(mEnableEditListItem);
		// // update label
		// item.setTitle(mEnableEditListItem ? "Done" : "Edit");
		// return true;
		// }
		// });
	}

	public void refreshTopic(Bundle bundle) {
		setListShown(false);
		getLoaderManager().restartLoader(TOPIC_LIST_LOADER, bundle, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (bundle != null) {
			String query = bundle.getString(BUNDLE_FORUM);
			int pageNo = bundle.getInt(BUNDLE_PAGE_NO);
			String timeFilter = bundle.getString(BUNDLE_TIME_FILTER);
			Uri uri = HKGMetaData.getUriListSearchResultByPage(query, pageNo,
					timeFilter);
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
									getArguments(), HKGSearchFragment.this);
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

			HKGSearchResult topic = createHKGTopicFromCursor(cursor);

			viewHolder.line1.setTextColor(Color.LTGRAY);
			viewHolder.line1.setText(String.format("%s",
					Html.fromHtml(topic.mTitle)));

			viewHolder.line2.setText(String.format("%s",
					Html.fromHtml(topic.mContent)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View row = null;
			if (sInflater == null) {
				sInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			row = sInflater.inflate(R.layout.search_result_header, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.left = (TextView) row.findViewById(R.id.left);
			viewHolder.line1 = (TextView) row.findViewById(R.id.line1);
			viewHolder.line2 = (TextView) row.findViewById(R.id.line2);
			row.setTag(viewHolder);

			return row;
		}

		static HKGSearchResult createHKGTopicFromCursor(Cursor cursor) {
			String url = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.SearchResultColumns.url));
			String title = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.SearchResultColumns.title));
			String content = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.SearchResultColumns.content));
			String resultCount = cursor
					.getString(cursor
							.getColumnIndex(HKGMetaData.SearchResultColumns.resultCount));
			int currentPageIndex = cursor
					.getInt(cursor
							.getColumnIndex(HKGMetaData.SearchResultColumns.currentPageIndex));

			HKGSearchResult topic = new HKGSearchResult(url, title, content,
					resultCount, currentPageIndex);
			return topic;
		}
	};

	interface HKGSearchResultListener {
		public void onHKGSearchResultSelected(HKGSearchResult searchResult);
	}

}