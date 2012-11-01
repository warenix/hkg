package org.dyndns.warenix.hkg;

import org.dyndns.warenix.hkg.HKGThread.HKGForum;
import org.dyndns.warenix.hkg.provider.HKGMetaData;

import android.content.Context;
import android.database.Cursor;
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

public class HKGForumFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "HKGForumFragment";

	private static final int FORUM_LIST_LOADER = 0x01;

	CursorAdapter mAdapter;
	HKGForumListener mListener;

	public static HKGForumFragment newInstance() {
		HKGForumFragment f = new HKGForumFragment();
		return f;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		setSelection(position);

		Log.i(TAG, "Item clicked: " + id);
		if (mListener != null) {
			mListener.onHKGForumSelected((HKGForum) mAdapter.getItem(position));
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(FORUM_LIST_LOADER, getArguments(), this);
		mAdapter = new ForumCursorAdapter(getActivity(), null);
		setListAdapter(mAdapter);
	}

	public void onStart() {
		super.onStart();

	}

	public void refreshTopic(Bundle bundle) {
		setListShown(false);
		getLoaderManager().restartLoader(FORUM_LIST_LOADER, bundle, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Uri uri = HKGMetaData.getUriListForum();
		CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, null,
				null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);

		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	static class ForumCursorAdapter extends CursorAdapter {
		public ForumCursorAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public ForumCursorAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}

		public ForumCursorAdapter(Context context, Cursor c, int flags) {
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
			return createHKGForumFromCursor(cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();

			HKGForum forum = createHKGForumFromCursor(cursor);

			viewHolder.line1.setText(String.format("%s",
					Html.fromHtml(forum.mName)));
			viewHolder.line2.setText(String.format("%s",
					Html.fromHtml(forum.mType)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View row;
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.forum_header, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.left = (TextView) row.findViewById(R.id.left);
			viewHolder.line1 = (TextView) row.findViewById(R.id.line1);
			viewHolder.line2 = (TextView) row.findViewById(R.id.line2);
			row.setTag(viewHolder);
			return row;
		}

		static HKGForum createHKGForumFromCursor(Cursor cursor) {
			String name = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.ForumColumns.name));
			String type = cursor.getString(cursor
					.getColumnIndex(HKGMetaData.ForumColumns.type));
			HKGForum forum = new HKGForum(name, type);
			return forum;
		}
	};

	public void setHKGForumListener(HKGForumListener listener) {
		mListener = listener;
	}

	interface HKGForumListener {
		public void onHKGForumSelected(HKGForum forum);
	}
}
