package org.dyndns.warenix.hkg.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dyndns.warenix.hkg.HKGThread;
import org.dyndns.warenix.hkg.HKGThread.HKGPage;
import org.dyndns.warenix.hkg.HKGThread.HKGReply;
import org.dyndns.warenix.hkg.parser.HKGListParser;
import org.dyndns.warenix.hkg.parser.HKGParser.PageRequest;
import org.dyndns.warenix.hkg.parser.HKGThreadParser;
import org.dyndns.warenix.hkgcp.R;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class HKGProvider extends ContentProvider {

	private static final String TAG = "HKGProvider";

	// Creates a UriMatcher object.
	private static UriMatcher sUriMatcher = null;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(HKGMetaData.AUTHORITY, HKGMetaData.PATH_LIST_FORUM,
				HKGMetaData.TYPE_LIST_FORUM);
		sUriMatcher.addURI(HKGMetaData.AUTHORITY,
				HKGMetaData.PATH_LIST_FORUM_THREAD_BY_PAGE,
				HKGMetaData.TYPE_LIST_FORUM_THREAD_BY_PAGE);

		sUriMatcher.addURI(HKGMetaData.AUTHORITY,
				HKGMetaData.PATH_SHOW_THREAD_BY_PAGE,
				HKGMetaData.TYPE_SHOW_THREAD_BY_PAGE);
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		Log.d(TAG, String.format("getType() uri[%s]", uri));
		switch (sUriMatcher.match(uri)) {
		case HKGMetaData.TYPE_LIST_FORUM:
			return HKGMetaData.CONTENT_TYPE_HKG_FORUM_LIST;

		case HKGMetaData.TYPE_LIST_FORUM_THREAD_BY_PAGE:
			return HKGMetaData.CONTENT_TYPE_HKG_THREAD_LIST;

		case HKGMetaData.TYPE_SHOW_THREAD_BY_PAGE:
			return HKGMetaData.CONTENT_TYPE_HKG_THREAD_ONE;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		return null;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, String.format("query() uri[%s]", uri));
		switch (sUriMatcher.match(uri)) {
		case HKGMetaData.TYPE_LIST_FORUM:
			return queryAllHKGForum(uri, projection, selection, selectionArgs,
					sortOrder);
		case HKGMetaData.TYPE_LIST_FORUM_THREAD_BY_PAGE:
			return queryAllCentralBeauty(uri, projection, selection,
					selectionArgs, sortOrder);
		case HKGMetaData.TYPE_SHOW_THREAD_BY_PAGE:
			return queryOneCentralBeuaty(uri, projection, selection,
					selectionArgs, sortOrder);
		}
		return null;
	}

	private Cursor queryAllCentralBeauty(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {

		List<String> pathSegments = uri.getPathSegments();
		String type = pathSegments.get(1);
		int pageNo = Integer.parseInt(pathSegments.get(2));

		MatrixCursor cursor = new MatrixCursor(
				HKGMetaData.MATRIX_CURSOR_COLUMNS);

		HKGListParser parser = new HKGListParser();
		try {
			parser.parse(PageRequest.getListUrl(type, pageNo));
			ArrayList<HKGThread> threadList = parser.getHKGThreadList();
			for (HKGThread thread : threadList) {
				cursor.addRow(new Object[] { thread.mThreadId, thread.mUser,
						thread.mRepliesCount, thread.mTitle, thread.mRating,
						thread.mPageCount });
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// CentralBeautyMaster master = new CentralBeautyMaster();
		// ArrayList<CentralBeauty> list;
		// try {
		// list = master.getCentralBeautyOfThisWeek();
		// for (CentralBeauty centralBeauty : list) {
		// cursor.addRow(new Object[] { centralBeauty.num,
		// centralBeauty.num, centralBeauty.fullPageUrl,
		// centralBeauty.previewImageUrl,
		// centralBeauty.previewImageUrlLarge,
		// centralBeauty.description });
		// }
		//
		// } catch (ClientProtocolException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		Log.d(TAG, "queryAllCentralBeauty count:" + cursor.getCount());

		return cursor;
	}

	private Cursor queryOneCentralBeuaty(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		List<String> pathSegments = uri.getPathSegments();
		String threadId = pathSegments.get(1);
		int pageNo = Integer.parseInt(pathSegments.get(2));

		MatrixCursor cursor = new MatrixCursor(
				HKGMetaData.MATRIX_THREAD_BY_PAGE_CURSOR_COLUMNS);

		try {
			HKGThread thread = new HKGThread(threadId, null, -1, null, -1, -1);
			thread.mSelectedPage = pageNo;
			HKGThreadParser parser = new HKGThreadParser(pageNo);
			parser.setHKGThread(thread);
			parser.parse(PageRequest.getReadThreadUrl(thread.mThreadId, pageNo));

			HKGPage page = thread.mPageMap.get(pageNo);
			ArrayList<HKGReply> replyList = page.getReplyList();
			for (HKGReply reply : replyList) {
				cursor.addRow(new Object[] { reply.mUser, reply.mPostDate,
						reply.mContent });
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	private Cursor queryAllHKGForum(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {

		MatrixCursor cursor = new MatrixCursor(
				HKGMetaData.MATRIX_FORUM_CURSOR_COLUMNS);

		String[] forumNames = getContext().getResources().getStringArray(
				R.array.forum_name_array);
		String[] forumTypes = getContext().getResources().getStringArray(
				R.array.forum_type_array);
		int l = forumNames.length;

		for (int i = 0; i < l; ++i) {
			cursor.addRow(new Object[] { i, forumNames[i], forumTypes[i] });
		}
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
