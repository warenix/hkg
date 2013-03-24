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

		sUriMatcher.addURI(HKGMetaData.AUTHORITY,
				HKGMetaData.PATH_SHOW_LAST_VISIT_THREAD_PAGE,
				HKGMetaData.TYPE_SHOW_LAST_VISIT_THREAD_PAGE);
	}

	// database
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, "hkg.db", null, 2);
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

			db.execSQL("CREATE TRIGGER UPDATE_BOOKMARK BEFORE UPDATE ON "
					+ "bookmark" + " BEGIN UPDATE " + "bookmark" + " SET "
					+ HKGMetaData.BookmarkColumns.last_modified
					+ " = strftime('%s','now') WHERE rowid = new.rowid; END");

			db.execSQL("CREATE TRIGGER INSERT_BOOKMARK AFTER INSERT ON "
					+ "bookmark" + " BEGIN UPDATE " + "bookmark" + " SET "
					+ HKGMetaData.BookmarkColumns.last_modified
					+ " = strftime('%s','now') WHERE rowid = new.rowid; END");

			db.execSQL("CREATE TABLE "
					+ "hkthread_last_visit"
					+ " ("
					+ HKGMetaData.HKGThreadLastVisitColumns.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"//
					+ HKGMetaData.HKGThreadLastVisitColumns.threadId
					+ " VARCHAR(255) UNIQUE," //
					+ HKGMetaData.HKGThreadLastVisitColumns.pageNo
					+ " INTEGER DEFAULT 1," //
					+ HKGMetaData.HKGThreadLastVisitColumns.last_modified
					+ " NOT NULL DEFAULT CURRENT_TIMESTAMP" //
					+ ");");

			db.execSQL("CREATE TRIGGER UPDATE_LAST_VISIT BEFORE UPDATE ON "
					+ "hkthread_last_visit" + " BEGIN UPDATE "
					+ "hkthread_last_visit" + " SET "
					+ HKGMetaData.HKGThreadLastVisitColumns.last_modified
					+ " = strftime('%s','now') WHERE rowid = new.rowid; END");

			db.execSQL("CREATE TRIGGER INSERT_LAST_VISIT AFTER INSERT ON "
					+ "hkthread_last_visit" + " BEGIN UPDATE "
					+ "hkthread_last_visit" + " SET "
					+ HKGMetaData.HKGThreadLastVisitColumns.last_modified
					+ " = strftime('%s','now') WHERE rowid = new.rowid; END");

			db.execSQL("CREATE INDEX INDEX_LAST_VISIT ON hkthread_last_visit("
					+ HKGMetaData.HKGThreadLastVisitColumns.threadId + ")");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + "bookmark");
			db.execSQL("DROP TABLE IF EXISTS " + "hkthread_last_visit");
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

		String table = null;

		switch (sUriMatcher.match(uri)) {
		case HKGMetaData.TYPE_LIST_BOOKMARK:
			table = "bookmark";
			break;

		case HKGMetaData.TYPE_SHOW_BOOKMARK_BY_ID:
			table = "bookmark";
			where = where + HKGMetaData.BookmarkColumns.ID + "="
					+ uri.getLastPathSegment();
			break;
		}

		if (table != null) {
			SQLiteDatabase db = mDBHelper.getReadableDatabase();
			int count = db.delete(table, where, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		} else {
			// return 0 as no right tablet is picked
			return 0;
		}
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

		case HKGMetaData.TYPE_SHOW_LAST_VISIT_THREAD_PAGE:
			return HKGMetaData.CONTENT_TYPE_LAST_VISIT_THREAD_PAGE;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		String table = null;
		Uri insertedUri = null;

		switch (sUriMatcher.match(uri)) {
		case HKGMetaData.TYPE_LIST_BOOKMARK:
			table = "bookmark";

			break;

		case HKGMetaData.TYPE_SHOW_LAST_VISIT_THREAD_PAGE:
			table = "hkthread_last_visit";
			break;
		}

		if (table == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		long rowId = db.insertWithOnConflict(table, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
		if (rowId > 0) {
			insertedUri = HKGMetaData.getUriShowBookmarkById(rowId);
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
		int uriType = sUriMatcher.match(uri);
		Log.d(TAG, String.format("query() uri[%s] type[%d]", uri, uriType));

		switch (uriType) {
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

		case HKGMetaData.TYPE_SHOW_LAST_VISIT_THREAD_PAGE:
			return queryOneLastVisitPage(uri, projection, selection,
					selectionArgs, sortOrder);
		}
		throw new IllegalArgumentException("Unknown URI: " + uri);
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

		MatrixCursor cursor = new MatrixCursor(
				HKGMetaData.MATRIX_SEARCH_RESULT_BY_PAGE_CURSOR_COLUMNS);
		WebSearchResult result = null;
		long count = 0;

		// search next page to get more result
		while (count <= 24) {
			result = GoogleWebSearchMaster.doSearch(
					String.format("%s site:m.hkgolden.com", query), pageNo++,
					timeFilter);
			if (result == null || result.mPageList.size() == 0) {
				break;
			}

			for (GoogleWebSearchMaster.Page page : result.mPageList) {
				// modify title XXXX - 香港高登 to XXXX
				page.title = page.title.replace(" - 香港高登", "");
				cursor.addRow(new Object[] { ++count, page.url, page.title,
						page.content, result.currentPageIndex,
						result.resultCount });
			}
		}
		return cursor;
	}

	// warenix
	private Cursor queryOneLastVisitPage(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		String table = null;

		if (selection == null) {
			selection = "";
		}

		switch (sUriMatcher.match(uri)) {
		case HKGMetaData.TYPE_SHOW_LAST_VISIT_THREAD_PAGE:
			table = "hkthread_last_visit";
			List<String> pathSegments = uri.getPathSegments();
			String threadId = pathSegments.get(1);
			int pageNo = Integer.parseInt(pathSegments.get(2));

			selection = selection
					+ HKGMetaData.HKGThreadLastVisitColumns.threadId + "="
					+ threadId;
			break;

		}

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(table);
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		String table = null;

		switch (sUriMatcher.match(uri)) {
		case HKGMetaData.TYPE_LIST_BOOKMARK:
			table = "bookmark";
			break;

		case HKGMetaData.TYPE_SHOW_LAST_VISIT_THREAD_PAGE:
			table = "hkthread_last_visit";
			break;
		}

		if (table == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		// int rows = db.update(table, values, selection, selectionArgs);
		int rows = db.updateWithOnConflict(table, values, selection,
				selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);

		return rows;
	}

}
