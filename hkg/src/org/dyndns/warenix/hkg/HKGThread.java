package org.dyndns.warenix.hkg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.dyndns.warenix.hkg.parser.HKGParser;

public class HKGThread {
	public String mThreadId;
	public String mUser;
	public int mRepliesCount;
	public String mTitle;
	public int mRating;
	public int mPageCount;

	/*
	 * obtained only if a thread is opened
	 */
	public int mSelectedPage;

	public HashMap<Integer, HKGPage> mPageMap = new HashMap<Integer, HKGThread.HKGPage>();

	public HKGThread() {

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

	public static class HKGPage {
		static final int MAX_REPLIES_PER_PAGE = 26;
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

}
