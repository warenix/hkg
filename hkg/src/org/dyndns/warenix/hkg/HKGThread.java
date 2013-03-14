package org.dyndns.warenix.hkg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.dyndns.warenix.hkg.parser.HKGParser;

public class HKGThread {
	public String mThreadId;
	/**
	 * user is only available on topic list
	 */
	public String mUser;
	/**
	 * replies count is only available on topic list
	 */
	public int mRepliesCount;
	public String mTitle;
	public int mRating;
	public int mPageCount;

	/*
	 * obtained only if a thread is opened
	 */
	public int mSelectedPage = -1;

	public HashMap<Integer, HKGPage> mPageMap = new HashMap<Integer, HKGThread.HKGPage>();

	public HKGThread() {

	}

	protected String updateAuthorIfNeeded() {
		if (mUser == null) {
			// treat the 1st reply on page 1 as the author
			HKGPage page = getPage(1);
			if (page != null) {
				ArrayList<HKGReply> replyList = page.getReplyList();
				if (replyList != null && replyList.size() > 0) {
					HKGReply reply = replyList.get(0);
					mUser = reply.mUser;
				}
			}
		}
		return mUser;
	}

	@Override
	public String toString() {
		return String.format(
				"[%s] [%s] by [%s]\n replies[%d] pages[%d] rating[%d]",
				mThreadId, mTitle, mUser, mRepliesCount, mPageCount, mRating);
	}

	/**
	 * 
	 * @param threadId
	 */
	public HKGThread(String threadId) {
		this(threadId, null, -1, null, -1, -1);
	}

	public HKGThread(String threadId, String user, int repliesCount,
			String title, int rating, int pageCount) {
		mThreadId = threadId;
		mUser = user;
		mRepliesCount = repliesCount;
		mTitle = title;
		mRating = rating;
		mPageCount = pageCount;
	}

	public HKGPage getPage(int pageNo) {
		return mPageMap.get(pageNo);
	}

	/**
	 * A page contains a list of replies
	 * 
	 * @author warenix
	 * 
	 */
	public static class HKGPage {
		static final int MAX_REPLIES_PER_PAGE = 25;
		int mPageNo;

		public HKGPage(int pageNo) {
			mPageNo = pageNo;
		}

		ArrayList<HKGReply> mReplyList = new ArrayList<HKGReply>();

		public void addReply(HKGReply reply) {
			mReplyList.add(reply);
		}

		public ArrayList<HKGReply> getReplyList() {
			return mReplyList;
		}
	}

	public static class HKGReply {
		public String mUser;
		public String mPostDate;
		public String mContent;

		public void setContent(String contentHtml) {
			contentHtml = replaceWithRealImage(contentHtml);
			contentHtml = replaceRelativeImage(contentHtml);

			mContent = contentHtml;
		}

		String replaceWithRealImage(String contentHtml) {
			ArrayList<String> realImgList = new ArrayList<String>();
			Matcher realImageMatcher = HKGParser.mRealImgPattern
					.matcher(contentHtml);
			while (realImageMatcher.find()) {
				realImgList.add(realImageMatcher.group(2));
			}

			for (String realImg : realImgList) {
				contentHtml = contentHtml.replaceFirst(
						"/images/mobile/camera.png", realImg);
			}

			return contentHtml;
		}

		String replaceRelativeImage(String contentHtml) {
			ArrayList<String> relativeImgList = new ArrayList<String>();
			Matcher srcMatcher = HKGParser.mImgPattern.matcher(contentHtml);
			String src = null;
			while (srcMatcher.find()) {
				src = srcMatcher.group(1);
				if (src.charAt(0) == '/' && !relativeImgList.contains(src)) {
					relativeImgList.add(src);
				}
			}
			final String domain = "http://m.hkgolden.com";
			for (String relatievImg : relativeImgList) {
				contentHtml = contentHtml.replace(relatievImg, domain
						+ relatievImg);
			}

			return contentHtml;
		}

		public String toString() {
			StringBuffer s = new StringBuffer();
			s.append("\n" + mUser + " on " + mPostDate);
			s.append("\n" + mContent);
			return s.toString();
		}
	}

	public static class HKGForum {
		public HKGForum(String name, String type) {
			mName = name;
			mType = type;
		}

		public String mName;
		public String mType;
	}

}
