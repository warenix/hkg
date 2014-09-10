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

	/**
	 * Read a thread by page.
	 * 
	 * @param thread
	 * @param pageNo
	 *            1 based.
	 */
	public void readThreadByPage(HKGThread thread, int pageNo) {
		boolean mayHaveNewReply = true;

		HKGPage cachedPage = thread.getPage(pageNo);
		if (cachedPage != null) {
			mayHaveNewReply = cachedPage.getReplyList().size() < (pageNo == 1 ? HKGPage.MAX_REPLIES_PER_PAGE + 1
					: HKGPage.MAX_REPLIES_PER_PAGE);
		}

		if (mayHaveNewReply) {
			System.out.println("call parser");
			try {
				HKGThreadParser parser = new HKGThreadParser(pageNo);
				parser.setHKGThread(thread);
				parser.parse(PageRequest.getReadThreadUrl(thread.mThreadId,
						pageNo));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (pageNo == 1) {
			thread.updateAuthorIfNeeded();
		}

		thread.mSelectedPage = pageNo;

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
