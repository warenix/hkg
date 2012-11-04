package org.dyndns.warenix.hkg.provider;

import android.net.Uri;

public class HKGMetaData {
	public static final String AUTHORITY = "org.dyndns.warenix.hkg.provider.HKGProvider";

	public static final String PATH_LIST_FORUM = "forum";
	public static final String PATH_LIST_FORUM_THREAD_BY_PAGE = "forum/*/#";
	public static final String PATH_SHOW_THREAD_BY_PAGE = "thread/*/#";
	public static final String PATH_LIST_BOOKMARK = "bookmark";
	public static final String PATH_SHOW_BOOKMARK_BY_ID = "bookmark/#";
	public static final String PATH_LIST_SEARCH_RESULT_BY_PAGE = "search/*/#/*"; // query/page_no/time_filter

	public static final int TYPE_LIST_FORUM = 10;
	public static final int TYPE_LIST_FORUM_THREAD_BY_PAGE = 11;
	public static final int TYPE_SHOW_THREAD_BY_PAGE = 20;
	public static final int TYPE_LIST_BOOKMARK = 30;
	public static final int TYPE_SHOW_BOOKMARK_BY_ID = 31;
	public static final int TYPE_LIST_SEARCH_RESULT_BY_PAGE = 40;

	// uri
	/**
	 * list all central beauty
	 */
	public static final Uri URI_LIST_FORUM = Uri.parse(String.format(
			"content://%s/%s", AUTHORITY, PATH_LIST_FORUM));
	public static final Uri URI_LIST_FORUM_THREAD_BY_PAGE = Uri.parse(String
			.format("content://%s/%s", AUTHORITY,
					PATH_LIST_FORUM_THREAD_BY_PAGE));
	public static final Uri URI_SHOW_THREAD_BY_PAGE = Uri.parse(String.format(
			"content://%s/%s", AUTHORITY, PATH_SHOW_THREAD_BY_PAGE));

	public static final Uri URI_SHOW_SEARCH_RESULT_BY_PAGE = Uri.parse(String
			.format("content://%s/%s", AUTHORITY,
					PATH_LIST_SEARCH_RESULT_BY_PAGE));

	// content type
	public static final String CONTENT_TYPE_HKG_FORUM_LIST = "vnd.android.cursor.dir/vnd.org.dyndns.warenix.hkg.HKGForm";
	public static final String CONTENT_TYPE_HKG_THREAD_LIST = "vnd.android.cursor.dir/vnd.org.dyndns.warenix.hkg.HKGThread";
	public static final String CONTENT_TYPE_HKG_THREAD_ONE = "vnd.android.cursor.item/vnd.org.dyndns.warenix.hkg.HKGThread";
	public static final String CONTENT_TYPE_HKG_BOOKMARK_LIST = "vnd.android.cursor.dir/vnd.org.dyndns.warenix.hkg.HKGBookmark";
	public static final String CONTENT_TYPE_HKG_BOOKMARK_ONE = "vnd.android.cursor.item/vnd.org.dyndns.warenix.hkg.HKGBookmark";
	public static final String CONTENT_TYPE_HKG_SEARCH_RESULT_LIST = "vnd.android.cursor.dir/vnd.org.dyndns.warenix.hkg.HKGSearchResult";

	// matrixcursor columns
	public static final String[] MATRIX_CURSOR_COLUMNS = new String[] {
			BaseColumns.ID, BaseColumns.user, BaseColumns.repliesCount,
			BaseColumns.title, BaseColumns.rating, BaseColumns.pageCount };

	public static final String[] MATRIX_THREAD_BY_PAGE_CURSOR_COLUMNS = new String[] {
			ThreadColumns.user, ThreadColumns.postDate, ThreadColumns.content };

	public static final String[] MATRIX_FORUM_CURSOR_COLUMNS = new String[] {
			ForumColumns.ID, ForumColumns.name, ForumColumns.type };

	public static final String[] MATRIX_SEARCH_RESULT_BY_PAGE_CURSOR_COLUMNS = new String[] {
			SearchResultColumns.ID, SearchResultColumns.url,
			SearchResultColumns.title, SearchResultColumns.content,
			SearchResultColumns.currentPageIndex,
			SearchResultColumns.resultCount };

	public static class BaseColumns {
		public static final String ID = "_id";
		public static final String threadId = "threadId";
		public static final String user = "user";
		public static final String repliesCount = "repliesCount";
		public static final String title = "title";
		public static final String rating = "rating";
		public static final String pageCount = "pageCount";
	}

	public static class ThreadColumns {
		public static final String user = "user";
		public static final String postDate = "postDate";
		public static final String content = "content";
	}

	public static class ForumColumns {
		public static final String ID = "_id";
		public static final String name = "name";
		public static final String type = "type";
	}

	public static class BookmarkColumns extends BaseColumns {
		public static final String last_page_no_seen = "last_page_no_seen";
		public static final String last_modified = "last_modified";
	}

	public static class SearchResultColumns {
		public static final String ID = "_id";

		// page columns
		public static final String url = "url";
		public static final String title = "title";
		public static final String content = "content";

		// search result colums
		public static final String currentPageIndex = "currentPageIndex";
		public static final String resultCount = "resultCount";
	}

	public static Uri getListForumThreadByPage(String forum, int pageNo) {
		return Uri.parse(String.format("content://%s%s", HKGMetaData.AUTHORITY,
				String.format("/forum/%s/%d", forum, pageNo)));
	}

	public static Uri getUriListThreadByPage(String threadId, int pageNo) {
		return Uri.parse(String.format("content://%s%s", HKGMetaData.AUTHORITY,
				String.format("/thread/%s/%d", threadId, pageNo)));
	}

	public static Uri getUriListForum() {
		return Uri.parse(String.format("content://%s%s", HKGMetaData.AUTHORITY,
				String.format("/forum")));
	}

	public static Uri getUriListBookmark() {
		return Uri.parse(String.format("content://%s%s", HKGMetaData.AUTHORITY,
				String.format("/bookmark")));
	}

	public static Uri getUriShowBookmarkById(long id) {
		return Uri.parse(String.format("content://%s%s", HKGMetaData.AUTHORITY,
				String.format("/bookmark/%d", id)));
	}

	public static Uri getUriListSearchResultByPage(String query, long page,
			String timeFilter) {
		return Uri.parse(String.format("content://%s%s", HKGMetaData.AUTHORITY,
				String.format("/search/%s/%d/%s", query, page, timeFilter)));
	}

}
