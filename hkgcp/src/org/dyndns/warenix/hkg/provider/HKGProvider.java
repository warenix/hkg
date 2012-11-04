package org.dyndns.warenix.hkg.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dyndns.warenix.google.search.GoogleWebSearchMaster;
import org.dyndns.warenix.google.search.GoogleWebSearchMaster.WebSearchResult;
import org.dyndns.warenix.hkg.HKGThread;
import org.dyndns.warenix.hkg.HKGThread.HKGPage;
import org.dyndns.warenix.hkg.HKGThread.HKGReply;
import org.dyndns.warenix.hkg.parser.HKGListParser;
import org.dyndns.warenix.hkg.parser.HKGParser.PageRequest;
import org.dyndns.warenix.hkg.parser.HKGThreadParser;
import org.dyndns.warenix.hkgcp.R;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
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

		sUriMatcher.addURI(HKGMetaData.AUTHORITY,
				HKGMetaData.PATH_LIST_BOOKMARK, HKGMetaData.TYPE_LIST_BOOKMARK);
		sUriMatcher.addURI(HKGMetaData.AUTHORITY,
				HKGMetaData.PATH_SHOW_BOOKMARK_BY_ID,
				HKGMetaData.TYPE_SHOW_BOOKMARK_BY_ID);

		sUriMatcher.addURI(HKGMetaData.AUTHORITY,
				HKGMetaData.PATH_LIST_SEARCH_RESULT_BY_PAGE,
				HKGMetaData.TYPE_LIST_SEARCH_RESULT_BY_PAGE);
	}

	// database
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, "hkg.db", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + "bookmark"
					+ " ("
					+ HKGMetaData.BookmarkColumns.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"//
					+ HKGMetaData.BookmarkColumns.threadId
					+ " VARCHAR(255)," //
					+ HKGMetaData.BookmarkColumns.user
					+ " VARCHAR(255)," //
					+ HKGMetaData.BookmarkColumns.title
					+ " VARCHAR(255)," //
					+ HKGMetaData.BookmarkColumns.repliesCount
					+ " INTEGER," //
					+ HKGMetaData.BookmarkColumns.rating
					+ " INTEGER," //
					+ HKGMetaData.BookmarkColumns.pageCount
					+ " INTEGER," //
					+ HKGMetaData.BookmarkColumns.last_page_no_seen
					+ " INTEGER DEFAULT 1," //
					+ HKGMetaData.BookmarkColumns.last_modified
					// + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"//
					+ " NOT NULL DEFAULT CURRENT_TIMESTAMP" + //
					");");

			db.execSQL("CREATE TRIGGER UPDATE_FOOBAR BEFORE UPDATE ON "
					+ "bookmark" + " BEGIN UPDATE " + "bookmark" + " SET "
					+ HKGMetaData.BookmarkColumns.last_modified
					+ " = strftime('%s','now') WHERE rowid = new.rowid; END");

			db.execSQL("CREATE TRIGGER INSERT_FOOBAR AFTER INSERT ON "
					+ "bookmark" + " BEGIN UPDATE " + "bookmark" + " SET "
					+ HKGMetaData.BookmarkColumns.last_modified
					+ " = strftime('%s','now') WHERE rowid = new.rowid; END");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + "bookmark");
			onCreate(db);
		}
	}

	private DatabaseHelper mDBHelper;

	//

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		if (where == null) {
			where = "";
		}

		switch (sUriMatcher.match(uri)) {
		case HKGMetaData.TYPE_LIST_BOOKMARK:
			break;

		case HKGMetaData.TYPE_SHOW_BOOKMARK_BY_ID:
			where = where + HKGMetaData.BookmarkColumns.ID + "="
					+ uri.getLastPathSegment();
			break;
		}

		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		int count = db.delete("bookmark", where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
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

		case HKGMetaData.TYPE_LIST_BOOKMARK:
			return HKGMetaData.CONTENT_TYPE_HKG_BOOKMARK_LIST;

		case HKGMetaData.TYPE_SHOW_BOOKMARK_BY_ID:
			return HKGMetaData.CONTENT_TYPE_HKG_BOOKMARK_ONE;

		case HKGMetaData.TYPE_LIST_SEARCH_RESULT_BY_PAGE:
			return HKGMetaData.CONTENT_TYPE_HKG_SEARCH_RESULT_LIST;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != HKGMetaData.TYPE_LIST_BOOKMARK) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		long rowId = db.insert("bookmark", null, values);
		if (rowId > 0) {

			Uri insertedUri = HKGMetaData.getUriShowBookmarkById(rowId);
			getContext().getContentResolver().notifyChange(insertedUri, null);
			return insertedUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mDBHelper = new DatabaseHelper(getContext());
		return true;
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

		case HKGMetaData.TYPE_LIST_BOOKMARK:
		case HKGMetaData.TYPE_SHOW_BOOKMARK_BY_ID:
			return queryAllHKGBookmark(uri, projection, selection,
					selectionArgs, sortOrder);

		case HKGMetaData.TYPE_LIST_SEARCH_RESULT_BY_PAGE:
			return queryAllHKGSearchResult(uri, projection, selection,
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

	private Cursor queryAllHKGBookmark(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {

		if (selection == null) {
			selection = "";
		}
		switch (sUriMatcher.match(uri)) {
		case HKGMetaData.TYPE_LIST_BOOKMARK:
			break;
		case HKGMetaData.TYPE_SHOW_BOOKMARK_BY_ID:
			selection = selection + HKGMetaData.BookmarkColumns.ID + "="
					+ uri.getLastPathSegment();
			break;

		}

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables("bookmark");
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	private Cursor queryAllHKGSearchResult(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {

		List<String> pathSegments = uri.getPathSegments();
		String query = pathSegments.get(1);
		int pageNo = Integer.parseInt(pathSegments.get(2));
		String timeFilter = pathSegments.get(3);

		WebSearchResult result = GoogleWebSearchMaster.doSearch(
				String.format("%s site:m.hkgolden.com", query), pageNo,
				timeFilter);

		// MatrixCursor cursor = new MatrixCursor(new String[] { "result" });
		// cursor.addRow(new Object[] { result });

		MatrixCursor cursor = new MatrixCursor(
				HKGMetaData.MATRIX_SEARCH_RESULT_BY_PAGE_CURSOR_COLUMNS);
		long count = 0;
		if (result != null) {
			for (GoogleWebSearchMaster.Page page : result.mPageList) {
				cursor.addRow(new Object[] { ++count, page.url, page.title,
						page.content, result.currentPageIndex,
						result.resultCount });
			}
		}
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
