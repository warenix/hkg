package org.dyndns.warenix.hkg;

import java.io.IOException;
import java.util.ArrayList;

import org.dyndns.warenix.hkg.HKGThread.HKGPage;
import org.dyndns.warenix.hkg.parser.HKGListParser;
import org.dyndns.warenix.hkg.parser.HKGParser.PageRequest;
import org.dyndns.warenix.hkg.parser.HKGThreadParser;

public class HKGController {
	private static HKGController sHKGController;
	HKGListener mListener;

	public void setHKGListener(HKGListener listener) {
		mListener = listener;
	}

	public void readTopicByPage(String type, int pageNo) {
		try {
			HKGListParser parser = new HKGListParser();
			parser.parse(PageRequest.getListUrl(type, pageNo));
			ArrayList<HKGThread> threadList = parser.getHKGThreadList();
			if (mListener != null) {
				mListener.onTopicLoaded(type, pageNo, threadList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readThreadByPage(HKGThread thread, int pageNo) {
		HKGPage cachedPage = thread.mPageMap.get(pageNo);
		boolean hasNoCache = cachedPage == null;

		boolean mayHaveNewReplyInThisPage = !hasNoCache
				&& cachedPage.getReplyList().size() < HKGPage.MAX_REPLIES_PER_PAGE;
		boolean mayHaveNewReplyInNewLastPage = !hasNoCache
				&& thread.mSelectedPage == thread.mPageCount
				&& cachedPage.getReplyList().size() == HKGPage.MAX_REPLIES_PER_PAGE;
		boolean mayHaveNewReply = mayHaveNewReplyInThisPage
				|| mayHaveNewReplyInNewLastPage;
//		if (hasNoCache || mayHaveNewReply) {
			try {
				HKGThreadParser parser = new HKGThreadParser();
				parser.setHKGThread(thread);
				parser.parse(PageRequest.getReadThreadUrl(thread.mThreadId,
						pageNo));
			} catch (IOException e) {
				e.printStackTrace();
			}
//		}
		if (mListener != null) {
			mListener.onThreadLoaded(thread);
		}
	}

	public interface HKGListener {
		public void onTopicLoaded(String type, int pageNo,
				ArrayList<HKGThread> threadList);

		public void onThreadLoaded(HKGThread thread);
	}

	public static HKGController getController() {
		if (sHKGController == null) {
			sHKGController = new HKGController();
		}
		return sHKGController;
	}
}
