package org.dyndns.warenix.hkg.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.dyndns.warenix.hkg.HKGThread;

/**
 * Parse a list of HKGThreads (without replies) for a page.
 * 
 * @author warenix
 * 
 */
public class HKGListParser extends HKGParser {

	private enum Step {
		FIND_TOPIC, //
		FIND_TOPIC_ID, //
		FIND_TOPIC_REPLIES_COUNT, //
		FIND_TOPIC_TITLE, //
		FIND_TOPIC_USER, //
		FIND_TOPIC_PAGE_COUNT,
	}

	private Step mCurrentStep = Step.FIND_TOPIC_ID;

	private int pageCount = 0;

	private HKGThread mThread;
	private HKGList mHKGList = new HKGList();

	@Override
	public boolean feed(String inputLine) {
		if (inputLine.replace(" ", "").length() > 0) {
			// System.out.println(inputLine);
			switch (mCurrentStep) {
			case FIND_TOPIC:
				if (inputLine.equals("     <div class=\"TopicBox_Details\">")) {
					mCurrentStep = Step.FIND_TOPIC_ID;
				}
				break;
			case FIND_TOPIC_ID:
				Matcher matcher = mHrefPattern.matcher(inputLine);
				if (matcher.find()) {
					mThread = new HKGThread();
					Matcher m = mThreadIdPattern.matcher(matcher.group(1));
					if (m.find()) {
						mThread.mThreadId = m.group(1);
					}
				}

				if (inputLine
						.equals("         <div class=\"TopicBox_Replies\">")) {
					mCurrentStep = Step.FIND_TOPIC_REPLIES_COUNT;
				}
				break;
			case FIND_TOPIC_REPLIES_COUNT:
				mThread.mRepliesCount = Integer.parseInt(inputLine.replace(" ",
						""));
				mCurrentStep = Step.FIND_TOPIC_TITLE;
				break;
			case FIND_TOPIC_TITLE:
				if (!("         <div>".equals(inputLine) || "         </div>"
						.equals(inputLine))) {
					mThread.mTitle = inputLine.replace(" ", "").replace(
							"&nbsp;&nbsp;-&nbsp;", "");
					mCurrentStep = Step.FIND_TOPIC_USER;
				}
				break;
			case FIND_TOPIC_USER:
				Matcher m = mThreadUserPattern.matcher(inputLine);
				if (m.find()) {
					mThread.mUser = m.group(1).replace(" ", "")
							.replace("&nbsp;&nbsp;-&nbsp;", "");
					mThread.mRating = Integer.parseInt(m.group(2));
					mCurrentStep = Step.FIND_TOPIC_PAGE_COUNT;
				}

				break;
			case FIND_TOPIC_PAGE_COUNT:
				pageCount++;
				if ("</select>".equals(inputLine)) {
					mThread.mPageCount = pageCount - 2;
					mHKGList.addHKGThread(mThread);

					pageCount = 0;
					mCurrentStep = Step.FIND_TOPIC;
				}
				break;
			}
		}
		return true;
	}

	public HKGList getHKGList() {
		return mHKGList;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		for (HKGThread thread : mHKGList.mHKGThreadList) {
			s.append("\n<hr/>");
			s.append("\nthreadId:" + thread.mThreadId);
			s.append("\ntitle:" + thread.mTitle);
			s.append("\nuser:" + thread.mUser);
			s.append("\npage count:" + thread.mPageCount);
			s.append("\nrating:" + thread.mRating);
		}

		return s.toString();
	}

	public static class HKGList {
		ArrayList<HKGThread> mHKGThreadList = new ArrayList<HKGThread>();

		public void addHKGThread(HKGThread thread) {
			mHKGThreadList.add(thread);
		}

		public ArrayList<HKGThread> getHKGThreadList() {
			return mHKGThreadList;
		}
	}

	/**
	 * Get parser result.
	 * 
	 * @return A list of threads of the requested page.
	 */
	public ArrayList<HKGThread> getHKGThreadList() {
		return mHKGList.getHKGThreadList();
	}

}
