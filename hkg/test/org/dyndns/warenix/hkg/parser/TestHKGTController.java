package org.dyndns.warenix.hkg.parser;

import java.util.ArrayList;

import org.dyndns.warenix.hkg.HKGController;
import org.dyndns.warenix.hkg.HKGController.HKGListener;
import org.dyndns.warenix.hkg.HKGThread;
import org.dyndns.warenix.hkg.HKGThread.HKGReply;

public class TestHKGTController {
	public static void main(String args[]) {
		MyHKGListener listener = new MyHKGListener();
		HKGController controller = HKGController.getController();
		controller.setHKGListener(listener);
		controller.readTopicByPage("BW", 4);

		// String threadId = "5117045";
		// int pageNo = 4;
		// HKGThread thread = new HKGThread(threadId);
		// controller.readThreadByPage(thread, pageNo);
		// // System.out.println(thread.getPage(2).getReplyList().size());
		// // controller.readThreadByPage(thread, 2);
		// System.out.println(thread.getPage(pageNo).getReplyList().size()
		// + " replies");
		// for (HKGReply reply : thread.getPage(pageNo).getReplyList()) {
		// System.out.println(reply);
		// }

	}

	static class MyHKGListener implements HKGListener {

		@Override
		public void onTopicLoaded(String type, int page,
				ArrayList<HKGThread> threadList) {
			System.out.println("Topic loaded");

			for (HKGThread thread : threadList) {
				System.out.println(thread.mThreadId + ":" + thread.mTitle);
			}
		}

		@Override
		public void onThreadLoaded(HKGThread thread) {
			System.out.println("Thread loaded");
			printThread(thread);
			// HKGPage page = thread.mPageMap.get(thread.mSelectedPage);
			// for (HKGReply reply : page.getReplyList()) {
			// System.out.println(reply);
			// }
		}
	}

	static void printThread(HKGThread thread) {
		System.out.println(thread);
		System.out.println();
	}
}
