package org.dyndns.warenix.hkg;

public class HKGBookmark extends HKGThread {

	public long mRowId;

	public HKGBookmark(long rowId, String threadId, String user,
			int repliesCount, String title, int rating, int pageCount) {
		super(threadId, user, repliesCount, title, rating, pageCount);
		mRowId = rowId;
	}

}
