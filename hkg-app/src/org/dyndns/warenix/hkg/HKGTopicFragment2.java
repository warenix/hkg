package org.dyndns.warenix.hkg;

import org.dyndns.warenix.hkg.provider.HKGMetaData;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

public class HKGTopicFragment2 extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int TOPIC_LIST_LOADER = 0x01;

	public static final String BUNDLE_FORUM = "forum";
	public static final String BUNDLE_PAGE_NO = "page_no";

	CursorAdapter mAdapter;
	HKGThreadListener mListener;

	public static HKGTopicFragment2 newInstance(String type, int pageNo) {
		HKGTopicFragment2 f = new HKGTopicFragment2();
		// f.mPageNo = pageNo;
		// f.mType = type;
		return f;
	}

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
			mListener.onHKGThreadSelected((HKGThread) mAdapter
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

	public void refreshTopic(Bundle bundle) {
		setListShown(false);
		getLoaderManager().restartLoader(TOPIC_LIST_LOADER, bundle, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (bundle != null) {
			String forum = bundle.getString(BUNDLE_FORUM);
			int pageNo = bundle.getInt(BUNDLE_PAGE_NO);
			Uri uri = HKGMetaData.getListForumThreadByPage(forum, pageNo);
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

	static class TopicCursorAdapter extends CursorAdapter {
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
			return createHKGTopicFromCursor(cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();

			HKGThread topic = createHKGTopicFromCursor(cursor);

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
			View row;
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.thread_header, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.left = (TextView) row.findViewById(R.id.left);
			viewHolder.line1 = (TextView) row.findViewById(R.id.line1);
			viewHolder.line2 = (TextView) row.findViewById(R.id.line2);
			row.setTag(viewHolder);
			return row;
		}

		static HKGThread createHKGTopicFromCursor(Cursor cursor) {
			String threadId = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.BaseColumns.ID));
			String user = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.BaseColumns.user));
			int repliesCount = cursor.getInt(cursor
					.getColumnIndex(HKGMetaData.BaseColumns.repliesCount));
			String title = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.BaseColumns.title));
			int rating = cursor.getInt(cursor
					.getColumnIndex(HKGMetaData.BaseColumns.rating));
			int pageCount = cursor.getInt(cursor
					.getColumnIndex(HKGMetaData.BaseColumns.pageCount));
			HKGThread topic = new HKGThread(threadId, user, repliesCount,
					title, rating, pageCount);
			return topic;
		}
	};

	public void setHKGThreadListener(HKGThreadListener listener) {
		mListener = listener;
	}

	interface HKGThreadListener {
		public void onHKGThreadSelected(HKGThread topic);
	}
}