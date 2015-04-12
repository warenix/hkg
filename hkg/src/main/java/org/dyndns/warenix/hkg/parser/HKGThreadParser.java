package org.dyndns.warenix.hkg.parser;

import java.util.regex.Matcher;

import org.dyndns.warenix.hkg.HKGThread;
import org.dyndns.warenix.hkg.HKGThread.HKGPage;
import org.dyndns.warenix.hkg.HKGThread.HKGReply;
import org.dyndns.warenix.hkg.parser.HKGThreadParser.Step;

/**
 * Parse a list of replies from a HKGThread
 * 
 * @author warenix
 * 
 */
public class HKGThreadParser extends HKGParser {

	enum Step {
		FIND_THERAD_TITLE, //
		FIND_THREAD_PAGE_COUNT, //
		FIND_THREAD_REPLY, FIND_THREAD_REPLY_USER, FIND_THREAD_REPLY_POST_DATE, FIND_THREAD_REPLY_CONTENT,
	}

	private Step mCurrentStep = Step.FIND_THERAD_TITLE;

	private HKGPage mPage;

	int selectedPage = -1;
	int pageCount = 0;
	boolean pageSectionFound = false;
	/**
	 * flag indicating it is parsing author reply date or not. because for the
	 * first reply, reply date is on the next line. for other replies, reply
	 * date is on the same line.
	 */
	boolean isParsingAuthorReplyDate = false;
	boolean isAuthorPost = true;

	HKGReply reply;
	HKGThread mThread;

	StringBuffer content = new StringBuffer();

	public HKGThreadParser(int pageNo) {
		mPage = new HKGPage(pageNo);
		isAuthorPost = pageNo == 1;
	}

	@Override
	public boolean feed(String inputLine) {
		inputLine = inputLine.trim();
		if (inputLine.replace(" ", "").length() > 0) {
//			 System.out.println(inputLine);

			if ("<div id=\"bottomFunc\" class=\"View_PageSelectPanel2\"></div>"
					.equals(inputLine)) {
				// all replies are parsed, lines below can be ignored

				// save page
				mThread.mPageMap.put(mThread.mSelectedPage, mPage);
				return false;
			}

			switch (mCurrentStep) {
			case FIND_THERAD_TITLE:
				if (inputLine.startsWith("<title>")) {
					Matcher matcher = HKGParser.mTitlePattern
							.matcher(inputLine);
					if (matcher.find()) {
						if (mThread == null) {
							mThread = new HKGThread();
						}

						mThread.mTitle = matcher.group(1)
								.replace(" - 香港高登", "");
						mCurrentStep = Step.FIND_THREAD_PAGE_COUNT;
					}
				}
				break;
			case FIND_THREAD_PAGE_COUNT:
				if (pageSectionFound && inputLine.startsWith("</select>")) {
					pageSectionFound = false;
					// mPage.setPageCount(selectedPage - 2, pageCount - 3);

					mThread.mPageCount = pageCount - 3;
					if (mThread.mPageCount == 1) {
						mThread.mSelectedPage = 1;
					} else {
						mThread.mSelectedPage = selectedPage - 2;
					}
					mCurrentStep = Step.FIND_THREAD_REPLY;
				} else if (!pageSectionFound
						&& inputLine
								.startsWith("<select class=\"View_PageSelect\" ")) {
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
				if ("<div class=\"ReplyBox\">".equals(inputLine)) {
					mCurrentStep = Step.FIND_THREAD_REPLY_USER;
				}
				break;
			case FIND_THREAD_REPLY_USER:
				if (inputLine.startsWith("<span")) {
					Matcher matcher = mReplyUserPattern.matcher(inputLine);
					if (matcher.find()) {
						reply = new HKGReply();
						reply.mUser = matcher.group(2);
						mCurrentStep = Step.FIND_THREAD_REPLY_POST_DATE;
					}
				}
				break;
			case FIND_THREAD_REPLY_POST_DATE:
				if (inputLine.startsWith("<div class=\"ViewDate\"")) {

					if (isAuthorPost) {
						isParsingAuthorReplyDate = true;
					} else {
						Matcher matcher = mPostDatePattern.matcher(inputLine);
						if (matcher.find()) {
							reply.mPostDate = matcher.group(1);
						}
					}
					break;
				} else if (isParsingAuthorReplyDate) {
					isParsingAuthorReplyDate = false;
					reply.mPostDate = inputLine;
					break;
				} else if (reply.mPostDate != null
						&& inputLine.startsWith("<div>"))
				// && "</div>".equals(inputLine))
				{
					mCurrentStep = Step.FIND_THREAD_REPLY_CONTENT;
				} else {
					break;
				}

			case FIND_THREAD_REPLY_CONTENT:
				if (inputLine.startsWith("<div class=\"FloatsClearing\"")) {
					// reply.mContent = content.toString();
					reply.setContent(content.toString());
					mPage.addReply(reply);
					content.setLength(0);
					mCurrentStep = Step.FIND_THREAD_REPLY;
				} else {
					content.append(inputLine);
					if (isAuthorPost) {
						isAuthorPost = false;
					}
				}
				break;
			}
		}
		return true;
	}

	public String toString() {
		StringBuffer s = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		s.append("\nTitle:" + mThread.mTitle);
		s.append("\nPages Count:" + mThread.mSelectedPage + "/"
				+ mThread.mPageCount);
		s.append("\nReplies Count:" + mPage.getReplyList().size());
		int count = 0;
		for (HKGReply reply : mPage.getReplyList()) {
			s.append("\n<hr/>#" + count++);
			s.append(reply.toString());
		}
		return s.toString();
	}

	/**
	 * Set a thread if you want to update HKGThread for latest info.
	 * 
	 * @param thread
	 */
	public void setHKGThread(HKGThread thread) {
		mThread = thread;
	}

	public HKGPage getHKGPage() {
		return mPage;
	}

}
