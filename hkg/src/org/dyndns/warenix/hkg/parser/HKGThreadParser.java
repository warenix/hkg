package org.dyndns.warenix.hkg.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HKGThreadParser extends HKGParser {

	enum Step {
		FIND_THERAD_TITLE, //
		FIND_THREAD_PAGE_COUNT, //
		FIND_THREAD_REPLY, FIND_THREAD_REPLY_USER, FIND_THREAD_REPLY_POST_DATE, FIND_THREAD_REPLY_CONTENT,
	}

	private Step mCurrentStep = Step.FIND_THERAD_TITLE;

	private Page mPage = new Page();

	int selectedPage = 0;
	int pageCount = 0;
	boolean pageSectionFound = false;

	private final Pattern mTitlePattern = Pattern
			.compile("<title>(.*?)</title>");

	final Pattern mUserPattern = Pattern
			.compile("<span class=\"(ViewNameMale|ViewNameFemale)\">(.*?)</span>");
	final Pattern mPostDatePattern = Pattern
			.compile("<div class=\"ViewDate\">(.*?)</div>");
	final static Pattern mImgPattern = Pattern
			.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
	final static Pattern mRealImgPattern = Pattern
			.compile("\\[(img|IMG)\\](.*?)\\[/(img|IMG)\\]");
	Reply reply;

	StringBuffer content = new StringBuffer();

	@Override
	public void feed(String inputLine) {
		if (inputLine.replace(" ", "").length() > 0) {
			// System.out.println(inputLine);

			switch (mCurrentStep) {
			case FIND_THERAD_TITLE:
				if (inputLine.startsWith("    <title>")) {
					Matcher matcher = mTitlePattern.matcher(inputLine);
					if (matcher.find()) {
						mPage.mTitle = matcher.group(1);
						mCurrentStep = Step.FIND_THREAD_PAGE_COUNT;
					}
				}
				break;
			case FIND_THREAD_PAGE_COUNT:
				if (pageSectionFound && inputLine.startsWith("    </select>")) {
					pageSectionFound = false;
					mPage.setPageCount(selectedPage - 2, pageCount - 3);
					mCurrentStep = Step.FIND_THREAD_REPLY;
				} else if (!pageSectionFound
						&& inputLine
								.startsWith("    <select class=\"View_PageSelect\" ")) {
					pageSectionFound = true;
				}
				if (pageSectionFound) {
					pageCount++;
					if (inputLine.contains("selected=\"selected\"")) {
						selectedPage = pageCount;
					}
				}
				break;
			case FIND_THREAD_REPLY:
				if ("   <div class=\"ReplyBox\">".equals(inputLine)) {
					mCurrentStep = Step.FIND_THREAD_REPLY_USER;
				}
				break;
			case FIND_THREAD_REPLY_USER:
				if (inputLine.startsWith("       <span")) {
					Matcher matcher = mUserPattern.matcher(inputLine);
					if (matcher.find()) {
						reply = new Reply();
						reply.mUser = matcher.group(2);
						mCurrentStep = Step.FIND_THREAD_REPLY_POST_DATE;
					}
				}
				break;
			case FIND_THREAD_REPLY_POST_DATE:
				if (inputLine.startsWith("       <div class=\"ViewDate\"")) {
					Matcher matcher = mPostDatePattern.matcher(inputLine);
					if (matcher.find()) {
						reply.mPostDate = matcher.group(1);
					}
				} else if ("     </div>".equals(inputLine)) {
					mCurrentStep = Step.FIND_THREAD_REPLY_CONTENT;
				}
				break;
			case FIND_THREAD_REPLY_CONTENT:
				if (inputLine.startsWith("     <div class=\"FloatsClearing\"")) {
					// reply.mContent = content.toString();
					reply.setContent(content.toString());
					mPage.addReply(reply);
					content.setLength(0);
					mCurrentStep = Step.FIND_THREAD_REPLY;
				} else {
					content.append(inputLine);
				}
				break;
			}

		}
	}

	public String toString() {
		StringBuffer s = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		s.append("\nTitle:" + mPage.mTitle);
		s.append("\nPages Count:" + mPage.mSelectedPage + "/"
				+ mPage.mPageCount);
		s.append("\nReplies Count:" + mPage.mReplyList.size());
		int count = 0;
		for (Reply reply : mPage.mReplyList) {
			s.append("\n<hr/>#" + count++);
			s.append(reply.toString());
		}
		return s.toString();
	}

	public static class PageRequest {
		static final String DOMAIN = "http://m.hkgolden.com/";
		static final String READ_THERAD_PATH = "view.aspx";
		static final String LIST_THERAD_PATH = "topics.aspx";

		public static String getReadThreadUrl(String threadId, int pageNo) {
			String url = DOMAIN + READ_THERAD_PATH + "?";
			// fill get parameters
			url += String.format("&message=%s", threadId);
			url += String.format("&page=%d", pageNo);
			return url;
		}

		public static String getListUrl(String type, int pageNo) {
			String url = DOMAIN + LIST_THERAD_PATH + "?";
			// fill get parameters
			url += String.format("&type=%s", type);
			url += String.format("&page=%d", pageNo);
			return url;
		}
	}

	public static class Page {
		String mTitle;
		int mSelectedPage;
		int mPageCount;

		ArrayList<Reply> mReplyList = new ArrayList<Reply>();

		public void setPageCount(int selectedPage, int pageCount) {
			mSelectedPage = selectedPage;
			mPageCount = pageCount;
		}

		public void addReply(Reply reply) {
			mReplyList.add(reply);
		}
	}

	public static class Reply {
		String mUser;
		String mPostDate;
		String mContent;

		public void setContent(String contentHtml) {
			contentHtml = replaceWithRealImage(contentHtml);
			contentHtml = replaceRelativeImage(contentHtml);

			mContent = contentHtml;
		}

		String replaceWithRealImage(String contentHtml) {
			ArrayList<String> realImgList = new ArrayList<String>();
			Matcher realImageMatcher = mRealImgPattern.matcher(contentHtml);
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
			Matcher srcMatcher = mImgPattern.matcher(contentHtml);
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
