package org.dyndns.warenix.hkg.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HKGListParser extends HKGParser {

	enum Step {
		FIND_TOPIC, //
		FIND_TOPIC_ID, //
		FIND_TOPIC_REPLIES_COUNT, //
		FIND_TOPIC_TITLE, //
		FIND_TOPIC_USER, //
		FIND_TOPIC_PAGE_COUNT,
	}

	private Step mCurrentStep = Step.FIND_TOPIC_ID;

	int pageCount = 0;

	final Pattern mUserPattern = Pattern.compile("(.*?)\\(評分: ([0-9]+)");
	final static Pattern mHrefPattern = Pattern
			.compile("<a[^>]+href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
	final Pattern mThreadIdPattern = Pattern.compile("message=([0-9]+)");
	Topic topic;
	HKGList hkgList = new HKGList();
	StringBuffer content = new StringBuffer();

	@Override
	public void feed(String inputLine) {
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
					topic = new Topic();
					// topic.threadId = matcher.group(1);
					Matcher m = mThreadIdPattern.matcher(matcher.group(1));
					if (m.find()) {
						topic.threadId = m.group(1);
					}
				}

				if (inputLine
						.equals("         <div class=\"TopicBox_Replies\">")) {
					mCurrentStep = Step.FIND_TOPIC_REPLIES_COUNT;
				}
				break;
			case FIND_TOPIC_REPLIES_COUNT:
				topic.repliesCount = Integer.parseInt(inputLine
						.replace(" ", ""));
				mCurrentStep = Step.FIND_TOPIC_TITLE;
				break;
			case FIND_TOPIC_TITLE:
				if (!("         <div>".equals(inputLine) || "         </div>"
						.equals(inputLine))) {
					topic.title = inputLine.replace(" ", "").replace(
							"&nbsp;&nbsp;-&nbsp;", "");
					mCurrentStep = Step.FIND_TOPIC_USER;
				}
				break;
			case FIND_TOPIC_USER:
				Matcher m = mUserPattern.matcher(inputLine);
				if (m.find()) {
					topic.user = m.group(1).replace(" ", "")
							.replace("&nbsp;&nbsp;-&nbsp;", "");
					topic.rating = Integer.parseInt(m.group(2));
					mCurrentStep = Step.FIND_TOPIC_PAGE_COUNT;
				}

				break;
			case FIND_TOPIC_PAGE_COUNT:
				pageCount++;
				if ("</select>".equals(inputLine)) {
					topic.pageCount = pageCount - 2;
					hkgList.addTopic(topic);

					pageCount = 0;
					mCurrentStep = Step.FIND_TOPIC;
				}
				break;
			}
		}
	}

	public HKGList getHKGList() {
		return hkgList;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		for (Topic topic : hkgList.mTopicList) {
			s.append("\n<hr/>");
			s.append("\nthreadId:" + topic.threadId);
			s.append("\ntitle:" + topic.title);
			s.append("\nuser:" + topic.user);
			s.append("\npage count:" + topic.pageCount);
			s.append("\nrating:" + topic.rating);
		}

		return s.toString();
	}

	public static class HKGList {
		ArrayList<Topic> mTopicList = new ArrayList<Topic>();

		public void addTopic(Topic topic) {
			mTopicList.add(topic);
		}

		public ArrayList<Topic> getTopicList() {
			return mTopicList;
		}
	}

	public static class Topic {
		public String threadId;
		public String user;
		public int repliesCount;
		public String title;
		public int rating;
		public int pageCount;
	}

}
